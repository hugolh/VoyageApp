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
        maven{
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication{
                create<BasicAuthentication>("basic")
            }
            credentials{
                username = "mapbox"
                password = "sk.eyJ1IjoiaHVnb2xoIiwiYSI6ImNsbmthOHp3ZjI4aDIyaXc1cThrNTJ2a2wifQ.cfkd-wMYePOPkQwn21y0aQ"
            }
        }
    }
}

rootProject.name = "voyage-kt"
include(":app")
