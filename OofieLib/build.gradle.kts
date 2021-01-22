plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version("6.1.0")
    id("java")
}

version = "0.1"

// disable default jar, as the only point for this module is to load dependencies.
// there is most definitely a better way to do this but I'm not good with java applications.
tasks {
    val jarTask = getByName("jar")
    jarTask.enabled = false
}

repositories {
    jcenter()
    mavenCentral()
    maven {
        name = "spigot-api"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }
    maven {
        name = "bungeecord-repo"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        name = "nukkitx-snapshot"
        url = uri("https://repo.nukkitx.com/maven-snapshots")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(group = "khttp", name = "khttp", version = "1.0.0")
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.12.2-R0.1-SNAPSHOT")
    compileOnly(group = "net.md-5", name = "bungeecord-api", version = "1.16-R0.4-SNAPSHOT")
}
