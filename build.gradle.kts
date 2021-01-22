plugins {
    kotlin("jvm") version "1.4.21"
}

repositories {
    jcenter()
    mavenCentral()
}

// remove dist folder on clean
tasks {
    getByName<Delete>("clean") {
        delete.add("dist")
    }
}

// remove unused build folder.
gradle.buildFinished {
    project.buildDir.deleteRecursively()
}

val v = System.getenv("OOFIE_VERSION") ?: "DEV"

subprojects {
    val subProject = this.project
    subProject.version = v

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