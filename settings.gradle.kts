pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex(
                    "com\\.android.*"
                )
                includeGroupByRegex(
                    "com\\.google.*"
                )
                includeGroupByRegex(
                    "androidx.*"
                )
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(
        RepositoriesMode.FAIL_ON_PROJECT_REPOS
    )
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name =
    "EBookReader"
include(
    ":app"
)
include(
    ":data"
)
include(
    ":data:ebook"
)
include(
    ":features"
)
include(
    ":features:main"
)
include(
    ":features:bookdetail"
)
