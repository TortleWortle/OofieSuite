plugins {
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version("6.1.0")
}

repositories {
    jcenter()
    mavenCentral()
}

subprojects {
    val subProject = this.project

    task("copyArtifacts") {
        doLast {
            val baseName = subProject.base.archivesBaseName
            val version = subProject.version
            val group = subProject.group
            val DIST_DIR = "${rootDir}/dist"
            val SRC_DIR = "${buildDir}/libs"

            mkdir(DIST_DIR)
            copy {
                from(SRC_DIR)
                into(DIST_DIR)
                include("**.jar")
            }
        }
    }

    afterEvaluate {
        subProject.tasks.findByName("build")?.finalizedBy("copyArtifacts")
        subProject.tasks.findByName("shadowJar")?.finalizedBy("copyArtifacts")
    }
}