pluginManagement {
    repositories {
        maven { 
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/") 
        }
        maven { 
            name = "NeoForge"
            url = uri("https://maven.neoforged.net/releases/") 
        }
        gradlePluginPortal()
    }
}

rootProject.name = "Quasar"

include("common", "fabric", "neoforge")
