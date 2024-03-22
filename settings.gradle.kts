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
                password = "secret_key"
            }
        }
    }
}

rootProject.name = "voyage-kt"
include(":app")
