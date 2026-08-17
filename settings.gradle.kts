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
        maven {
            name = "Fabric Snapshot"
            url = uri("https://maven.fabricmc.net/snapshots")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven { 
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/") 
        }
        maven { 
            name = "NeoForge"
            url = uri("https://maven.neoforged.net/releases/") 
        }
        maven {
            name = "Fabric Snapshot"
            url = uri("https://maven.fabricmc.net/snapshots")
        }
        mavenCentral()
    }
}

rootProject.name = "Quasar"

include("common", "fabric", "neoforge")
