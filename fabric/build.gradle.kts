plugins {
    id("java")
    id("idea")
    id("net.fabricmc.fabric-loom") version("1.8.13")
}

evaluationDependsOn(":common")

val MINECRAFT_VERSION: String by rootProject.extra
val PARCHMENT_VERSION: String? by rootProject.extra
val FABRIC_LOADER_VERSION: String by rootProject.extra
val FABRIC_API_VERSION: String by rootProject.extra
val SODIUM_DEPENDENCY_FABRIC: Any by rootProject.extra
val MOD_VERSION: String by rootProject.extra

repositories {
    mavenLocal()
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    maven {
        name = "caffeinemcRepositoryReleases"
        url = uri("https://maven.caffeinemc.net/releases")
    }
}

base {
    archivesName.set("quasar-fabric")
}

dependencies {
    minecraft("com.mojang:minecraft:${MINECRAFT_VERSION}")

    implementation("net.fabricmc:fabric-loader:$FABRIC_LOADER_VERSION")

    fun implementAndInclude(name: String) {
        implementation(name)
        include(name)
    }

    implementation("net.fabricmc.fabric-api:fabric-api:$FABRIC_API_VERSION")

    implementation(SODIUM_DEPENDENCY_FABRIC)
    implementAndInclude("org.antlr:antlr4-runtime:4.13.1")
    implementAndInclude("io.github.douira:glsl-transformer:3.0.0-pre3")
    implementAndInclude("org.anarres:jcpp:1.4.14")

    implementation(project(":common"))
    implementation(project(path = ":common", configuration = "vendoredJar"))
    implementation(project(path = ":common", configuration = "apiJar"))
    compileOnly(project(path = ":common", configuration = "headersJar"))

    compileOnly(files(rootDir.resolve("DHApi.jar")))
}

tasks.named("compileTestJava").configure {
    enabled = false
}

tasks.named("test").configure {
    enabled = false
}

loom {
    if (project(":common").file("src/main/resources/iris.accesswidener").exists())
        accessWidenerPath.set(project(":common").file("src/main/resources/iris.accesswidener"))

    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName.set("quasar-fabric.refmap.json")
        useLegacyMixinAp = false
    }

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("run")
        }
    }
}

tasks {
    processResources {
        from(project.project(":common").sourceSets.main.get().resources)
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(zipTree(project.project(":common").tasks.jar.get().archiveFile))

        manifest.attributes["Main-Class"] = "net.irisshaders.iris.LaunchWarn"
    }

    jar.get().destinationDirectory = rootDir.resolve("build").resolve("libs")
}
