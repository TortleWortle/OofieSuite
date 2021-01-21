plugins {
    kotlin("jvm")
}

group = "wtf.duck.oofie"

repositories {
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
        name = "sk89q-repo"
        url = uri("https://maven.enginehub.org/repo")
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(group = "org.spigotmc", name = "spigot-api", version = "1.12.2-R0.1-SNAPSHOT")
    compileOnly(group = "com.sk89q.worldguard", name = "worldguard-legacy", version = "6.2")
}
