pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Moodiary"
include(":app")
include(":core:common")
include(":core:designsystem")
include(":domain:mood")
include(":data:mood")
include(":feature:home")
include(":feature:statistics")
include(":feature:calendar")
include(":feature:settings")