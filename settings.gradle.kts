pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Mozilla Maven: hosts GeckoView (added for the dual-engine build).
        maven { url = uri("https://maven.mozilla.org/maven2") }
    }
}

rootProject.name = "TuxunBrowse"
include(":app")
