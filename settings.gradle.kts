rootProject.name = "CmpAppScaffold"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":share")
include(":core:common")
include(":core:data")
include(":core:design")
include(":feature:home")
include(":feature:browse")
include(":feature:message")
include(":feature:cart")
include(":feature:login")
include(":feature:mine")
