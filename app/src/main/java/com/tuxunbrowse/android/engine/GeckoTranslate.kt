package com.tuxunbrowse.android.engine

import android.os.Handler
import android.os.Looper
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.TranslationsController

/**
 * Page translation through the Gecko engine's own translator
 * (v0.226.0). Every API shape used here was read out of the
 * pinned GeckoView 132 AAR with javap before a line was
 * written (the feasibility record is in the checkpoint):
 *
 * - TranslationsController.RuntimeTranslation is all-static:
 *   isTranslationsEngineSupported, listSupportedLanguages,
 *   listModelDownloadStates, manageLanguageModel,
 *   checkPairDownloadSize.
 * - GeckoSession.getSessionTranslation() carries translate /
 *   restoreOriginalPage, and the session's translations
 *   delegate reports detected languages and state changes.
 * - TranslationsException carries the error codes mapped by
 *   [translateErrorText].
 *
 * The translation itself runs on-device against downloaded
 * language models; page content never leaves the device. The
 * only network traffic is a model download from Mozilla's
 * service when a needed model is missing — the engine's own
 * behaviour, not a third-party translation API bolted on.
 *
 * Two honest limits of the API surface, designed around rather
 * than papered over: there is NO download-progress callback
 * anywhere (manageLanguageModel and translate both answer one
 * GeckoResult at the end), so progress UI is indeterminate and
 * the pair's download size is quoted up front instead; and
 * GeckoResult.cancel() detaches the wait without the API
 * promising the engine aborts an in-flight model fetch, so a
 * cancel is presented as exactly that — the wait is cancelled.
 */

/** Where a Gecko tab's page translation currently stands. */
enum class TranslatePhase {
    IDLE, WORKING, TRANSLATED, FAILED
}

/**
 * Plain-language reason for a [TranslationsController
 * .TranslationsException] code. The numeric codes are the
 * AAR's own constants (javap-verified): -1 unknown, -2 engine
 * not supported, -3 could not translate, -4 could not restore,
 * -5 could not load languages, -6 language not supported,
 * -7 model could not retrieve, -8 model could not delete,
 * -9 model could not download, -10 model language required,
 * -11 model download required. Anything unrecognised gets the
 * generic retry line rather than a made-up cause.
 */
fun translateErrorText(code: Int): String = when (code) {
    -2 -> "这台设备的火狐内核不支持翻译"
    -3 -> "翻译没成功，请重试"
    -4 -> "没能切回原文，请重新加载页面"
    -5 -> "支持的语言列表没加载出来，请重试"
    -6 -> "这个语言暂时翻不了"
    -7 -> "语言包读取失败，请重试"
    -8 -> "语言包删除失败，请重试"
    -9 -> "语言包没下载下来，请检查网络后重试"
    -10, -11 -> "语言包还没下载好"
    else -> "翻译出了问题，请重试"
}

/** The exception's code, or -1 when it is not a translations error. */
fun translateErrorCodeOf(t: Throwable?): Int =
    (t as? TranslationsController.TranslationsException)?.code ?: -1

/** Primary language subtag of a BCP 47 tag ("zh-Hant" -> "zh"). */
fun primaryLanguageOf(tag: String): String =
    tag.trim().substringBefore('-').substringBefore('_').lowercase()

/**
 * Resolve the effective translation target language.
 *
 * [stored] is the user's settings choice ("" / null = follow
 * the system language). [systemTag] is the device locale's
 * language tag. [supportedTo] is the engine's to-language code
 * list. Resolution order: an explicit stored choice that the
 * engine still supports; the system tag exactly; the first
 * supported code sharing the system tag's primary subtag (so
 * zh-CN lands on zh-Hans and en-GB on en); English; the first
 * supported code. Null only when the engine supports nothing.
 */
fun resolveTranslateTarget(
    stored: String?,
    systemTag: String,
    supportedTo: List<String>
): String? {
    if (supportedTo.isEmpty()) return null
    val choice = stored?.trim().orEmpty()
    if (choice.isNotEmpty() && choice in supportedTo) return choice
    val system = systemTag.trim()
    if (system in supportedTo) return system
    val primary = primaryLanguageOf(system)
    if (primary.isNotEmpty()) {
        supportedTo.firstOrNull {
            primaryLanguageOf(it) == primary
        }?.let { return it }
    }
    if ("en" in supportedTo) return "en"
    return supportedTo.first()
}

/**
 * Runtime-level translation calls for the settings page and
 * the translate sheet. Every call answers on the main thread,
 * exactly once, with null / an error for failure — the static
 * API needs a live Gecko runtime, so each entry point fails
 * fast (instead of hanging forever on a result that can never
 * complete) when GeckoHolder has none. Callers that can start
 * the runtime do so first (GeckoHolder.ensure).
 */
object GeckoTranslateBridge {

    private fun mainHandler() = Handler(Looper.getMainLooper())

    private fun <T> attach(
        result: GeckoResult<T>,
        onOk: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val handler = mainHandler()
        try {
            result.accept(
                { value -> handler.post { onOk(value) } },
                { e -> handler.post { onError(e ?: Exception("unknown")) } }
            )
        } catch (t: Throwable) {
            handler.post { onError(t) }
        }
    }

    fun fetchEngineSupported(onResult: (Boolean) -> Unit) {
        if (!GeckoHolder.hasRuntime) {
            onResult(false)
            return
        }
        try {
            attach(
                TranslationsController.RuntimeTranslation
                    .isTranslationsEngineSupported(),
                { onResult(it == true) },
                { onResult(false) }
            )
        } catch (t: Throwable) {
            onResult(false)
        }
    }

    fun fetchSupportedLanguages(
        onResult: (TranslationsController.RuntimeTranslation.TranslationSupport?) -> Unit
    ) {
        if (!GeckoHolder.hasRuntime) {
            onResult(null)
            return
        }
        try {
            attach(
                TranslationsController.RuntimeTranslation
                    .listSupportedLanguages(),
                { onResult(it) },
                { onResult(null) }
            )
        } catch (t: Throwable) {
            onResult(null)
        }
    }

    fun fetchModelStates(
        onResult: (List<TranslationsController.RuntimeTranslation.LanguageModel>?) -> Unit
    ) {
        if (!GeckoHolder.hasRuntime) {
            onResult(null)
            return
        }
        try {
            attach(
                TranslationsController.RuntimeTranslation
                    .listModelDownloadStates(),
                { onResult(it ?: emptyList()) },
                { onResult(null) }
            )
        } catch (t: Throwable) {
            onResult(null)
        }
    }

    fun fetchPairDownloadSize(
        fromLanguage: String,
        toLanguage: String,
        onResult: (Long?) -> Unit
    ) {
        if (!GeckoHolder.hasRuntime) {
            onResult(null)
            return
        }
        try {
            attach(
                TranslationsController.RuntimeTranslation
                    .checkPairDownloadSize(fromLanguage, toLanguage),
                { onResult(it) },
                { onResult(null) }
            )
        } catch (t: Throwable) {
            onResult(null)
        }
    }

    /**
     * Download one language's models. Returns the pending
     * result so the caller can cancel() the wait; the callback
     * answers null on success or the failure. A null return
     * means the call never started (no runtime / threw) and
     * the callback has already answered with the failure.
     */
    fun downloadLanguageModel(
        languageCode: String,
        onDone: (Throwable?) -> Unit
    ): GeckoResult<Void>? = manageModel(
        languageCode,
        TranslationsController.RuntimeTranslation.DOWNLOAD,
        onDone
    )

    /** Delete one language's downloaded models. See [downloadLanguageModel]. */
    fun deleteLanguageModel(
        languageCode: String,
        onDone: (Throwable?) -> Unit
    ): GeckoResult<Void>? = manageModel(
        languageCode,
        TranslationsController.RuntimeTranslation.DELETE,
        onDone
    )

    private fun manageModel(
        languageCode: String,
        operation: String,
        onDone: (Throwable?) -> Unit
    ): GeckoResult<Void>? {
        if (!GeckoHolder.hasRuntime) {
            onDone(IllegalStateException("no gecko runtime"))
            return null
        }
        return try {
            val options = TranslationsController.RuntimeTranslation
                .ModelManagementOptions.Builder()
                .languageToManage(languageCode)
                .operation(operation)
                .operationLevel(
                    TranslationsController.RuntimeTranslation.LANGUAGE
                )
                .build()
            val result = TranslationsController.RuntimeTranslation
                .manageLanguageModel(options)
            attach(result, { onDone(null) }, { onDone(it) })
            result
        } catch (t: Throwable) {
            onDone(t)
            null
        }
    }
}
