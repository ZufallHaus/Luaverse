import org.gradle.api.file.DuplicatesStrategy

application {
    mainClass.set("com.zufallhaus.luaverse.MainKt")
}

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.20"
    application
}

group = "com.zufallhaus"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("it.skrape:skrapeit:1.2.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.apache.commons:commons-compress:1.27.1")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.zufallhaus.luaverse.MainKt"
    }

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}