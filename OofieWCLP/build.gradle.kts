plugins {
    kotlin("jvm")
}

version = "0.1"

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java) {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        name = "spigot-api"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }
    maven {
        name = "bungeecord-repo"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
    maven {
        name = "opencollab"
        url = uri("https://repo.nukkitx.com/maven-snapshots")
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.12.2-R0.1-SNAPSHOT")
    compileOnly(group = "net.md-5", name = "bungeecord-api", version = "1.16-R0.4-SNAPSHOT")
    compileOnly(group = "net.luckperms", name = "api", version = "5.2")
    compileOnly(group = "org.geysermc", name = "common", version = "1.2.0-SNAPSHOT")
    compileOnly(group = "org.geysermc", name = "floodgate-common", version = "1.0-SNAPSHOT")
    compileOnly(group = "org.geysermc", name = "floodgate-bungee", version = "1.0-SNAPSHOT")
    compileOnly(group = "khttp", name = "khttp", version = "1.0.0")
}
