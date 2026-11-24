package com.tuxunbrowse.android

import android.webkit.WebViewClient
import com.tuxunbrowse.android.engine.geckoErrorReasonFor
import com.tuxunbrowse.android.engine.webViewErrorReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mozilla.geckoview.WebRequestError

/**
 * Load-error reason mapping (v0.103.0 audit): every named error
 * code on both engines must land on its own plain-language line,
 * never on the generic "check your network" fallback — several of
 * these failures (auth rejected, file missing, page crashed, site
 * blocked as dangerous) are not network problems, and telling the
 * user to check the network sends them the wrong way. The generic
 * line is reserved for the truly unknown codes.
 */
class ErrorReasonTest {

    private val generic = "网页没能打开，请检查网络后重试"

    @Test
    fun webViewEveryNamedCodeHasItsOwnReason() {
        val codes = listOf(
            WebViewClient.ERROR_HOST_LOOKUP,
            WebViewClient.ERROR_CONNECT,
            WebViewClient.ERROR_IO,
            WebViewClient.ERROR_TIMEOUT,
            WebViewClient.ERROR_REDIRECT_LOOP,
            WebViewClient.ERROR_FAILED_SSL_HANDSHAKE,
            WebViewClient.ERROR_BAD_URL,
            WebViewClient.ERROR_UNSUPPORTED_SCHEME,
            WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME,
            WebViewClient.ERROR_AUTHENTICATION,
            WebViewClient.ERROR_PROXY_AUTHENTICATION,
            WebViewClient.ERROR_FILE,
            WebViewClient.ERROR_FILE_NOT_FOUND,
            WebViewClient.ERROR_UNSAFE_RESOURCE,
            WebViewClient.ERROR_TOO_MANY_REQUESTS
        )
        for (code in codes) {
            val reason = webViewErrorReason(code)
            assertFalse("code $code fell through to the generic line", reason == generic)
            assertTrue("code $code reason is blank", reason.isNotBlank())
        }
    }

    @Test
    fun webViewUnknownCodesKeepTheGenericReason() {
        assertEquals(generic, webViewErrorReason(WebViewClient.ERROR_UNKNOWN))
        assertEquals(generic, webViewErrorReason(-999))
    }

    @Test
    fun webViewNonNetworkCodesDoNotBlameTheNetwork() {
        assertFalse(webViewErrorReason(WebViewClient.ERROR_AUTHENTICATION).contains("网络"))
        assertFalse(webViewErrorReason(WebViewClient.ERROR_FILE_NOT_FOUND).contains("网络"))
        assertFalse(webViewErrorReason(WebViewClient.ERROR_UNSAFE_RESOURCE).contains("网络"))
    }

    @Test
    fun geckoEveryNamedCodeHasItsOwnReason() {
        val codes = listOf(
            WebRequestError.ERROR_UNKNOWN_HOST,
            WebRequestError.ERROR_OFFLINE,
            WebRequestError.ERROR_NET_TIMEOUT,
            WebRequestError.ERROR_CONNECTION_REFUSED,
            WebRequestError.ERROR_NET_INTERRUPT,
            WebRequestError.ERROR_NET_RESET,
            WebRequestError.ERROR_UNKNOWN_SOCKET_TYPE,
            WebRequestError.ERROR_PORT_BLOCKED,
            WebRequestError.ERROR_SECURITY_SSL,
            WebRequestError.ERROR_SECURITY_BAD_CERT,
            WebRequestError.ERROR_BAD_HSTS_CERT,
            WebRequestError.ERROR_HTTPS_ONLY,
            WebRequestError.ERROR_MALFORMED_URI,
            WebRequestError.ERROR_UNKNOWN_PROTOCOL,
            WebRequestError.ERROR_FILE_NOT_FOUND,
            WebRequestError.ERROR_FILE_ACCESS_DENIED,
            WebRequestError.ERROR_DATA_URI_TOO_LONG,
            WebRequestError.ERROR_REDIRECT_LOOP,
            WebRequestError.ERROR_CONTENT_CRASHED,
            WebRequestError.ERROR_CORRUPTED_CONTENT,
            WebRequestError.ERROR_UNSAFE_CONTENT_TYPE,
            WebRequestError.ERROR_INVALID_CONTENT_ENCODING,
            WebRequestError.ERROR_PROXY_CONNECTION_REFUSED,
            WebRequestError.ERROR_UNKNOWN_PROXY_HOST,
            WebRequestError.ERROR_SAFEBROWSING_PHISHING_URI,
            WebRequestError.ERROR_SAFEBROWSING_MALWARE_URI,
            WebRequestError.ERROR_SAFEBROWSING_UNWANTED_URI,
            WebRequestError.ERROR_SAFEBROWSING_HARMFUL_URI
        )
        for (code in codes) {
            val reason = geckoErrorReasonFor(code, WebRequestError.ERROR_CATEGORY_UNKNOWN)
            assertFalse("code $code fell through to the generic line", reason == generic)
            assertTrue("code $code reason is blank", reason.isNotBlank())
        }
    }

    @Test
    fun geckoCategoryFallbacksCoverEveryCategory() {
        val unknownCode = 0
        assertTrue(
            geckoErrorReasonFor(unknownCode, WebRequestError.ERROR_CATEGORY_NETWORK)
                .contains("网络")
        )
        assertFalse(
            geckoErrorReasonFor(unknownCode, WebRequestError.ERROR_CATEGORY_CONTENT) == generic
        )
        assertFalse(
            geckoErrorReasonFor(unknownCode, WebRequestError.ERROR_CATEGORY_URI) == generic
        )
        assertFalse(
            geckoErrorReasonFor(unknownCode, WebRequestError.ERROR_CATEGORY_SECURITY) == generic
        )
        assertFalse(
            geckoErrorReasonFor(unknownCode, WebRequestError.ERROR_CATEGORY_PROXY) == generic
        )
        val safebrowsing = geckoErrorReasonFor(
            unknownCode, WebRequestError.ERROR_CATEGORY_SAFEBROWSING
        )
        assertFalse(safebrowsing == generic)
        assertFalse("safe-browsing fallback must not blame the network",
            safebrowsing.contains("检查网络"))
        assertEquals(
            generic,
            geckoErrorReasonFor(unknownCode, WebRequestError.ERROR_CATEGORY_UNKNOWN)
        )
    }

    @Test
    fun geckoSafeBrowsingCodesWarnAboutDangerNotNetwork() {
        val phishing = geckoErrorReasonFor(
            WebRequestError.ERROR_SAFEBROWSING_PHISHING_URI,
            WebRequestError.ERROR_CATEGORY_SAFEBROWSING
        )
        assertTrue(phishing.contains("不要打开"))
        assertFalse(phishing.contains("检查网络"))
    }

    @Test
    fun bothEnginesAgreeOnTheSharedFailures() {
        // Same failure, same words, whichever engine hit it.
        assertEquals(
            webViewErrorReason(WebViewClient.ERROR_HOST_LOOKUP),
            geckoErrorReasonFor(
                WebRequestError.ERROR_UNKNOWN_HOST,
                WebRequestError.ERROR_CATEGORY_NETWORK
            )
        )
        assertEquals(
            webViewErrorReason(WebViewClient.ERROR_TIMEOUT),
            geckoErrorReasonFor(
                WebRequestError.ERROR_NET_TIMEOUT,
                WebRequestError.ERROR_CATEGORY_NETWORK
            )
        )
        assertEquals(
            webViewErrorReason(WebViewClient.ERROR_FILE_NOT_FOUND),
            geckoErrorReasonFor(
                WebRequestError.ERROR_FILE_NOT_FOUND,
                WebRequestError.ERROR_CATEGORY_URI
            )
        )
        assertEquals(
            webViewErrorReason(WebViewClient.ERROR_UNSUPPORTED_SCHEME),
            geckoErrorReasonFor(
                WebRequestError.ERROR_UNKNOWN_PROTOCOL,
                WebRequestError.ERROR_CATEGORY_URI
            )
        )
    }
}
