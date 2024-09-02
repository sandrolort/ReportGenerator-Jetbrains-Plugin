plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.intellij.platform") version "2.0.0"
}

group = "org.kvisha"
version = "1.0"

val PublishToken: String by project

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

intellijPlatform {
    projectName = "report-generator"
}

dependencies {
    implementation(kotlin("stdlib"))
    intellijPlatform {
        rider("2024.2.3")
        instrumentationTools()
    }
}