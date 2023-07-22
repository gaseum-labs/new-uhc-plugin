import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.1.0"
}

group = "new-uhc-plugin"
/* IGNORE this will not be kept in sync with real version */
version = "1.0-SNAPSHOT"

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    runServer {
        minecraftVersion("1.20.1")
    }
    shadowJar {
        archiveFileName = "uhc-plugin.jar"
    }
    jar {
        enabled = false
    }
}


bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    main = "org.gaseumlabs.uhcplugin.Plugin"
    apiVersion = "1.20"
    authors = listOf("balduvian", "sumdumdude")
}
