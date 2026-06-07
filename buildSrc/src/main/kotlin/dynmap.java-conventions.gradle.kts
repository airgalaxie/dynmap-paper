import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    java
    `maven-publish`
}

group = "us.dynmap"
version = "3.8-SNAPSHOT-26_1_2"

//https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

repositories {
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.mikeprimm.com/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    mavenCentral()
    mavenLocal()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks {
    compileJava {
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
        options.compilerArgs.remove("-Werror")
        options.encoding = "UTF-8"
    }

    clean {
      delete("target")
    }
}
