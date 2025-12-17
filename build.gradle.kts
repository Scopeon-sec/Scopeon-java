import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
  id("com.diffplug.spotless") version "8.1.0"
}

spotless {
  java {
    googleJavaFormat("1.33.0")
    target("**/src/main/java/**/*.java", "**/src/test/java/**/*.java")
  }
}

repositories {
    mavenCentral()
}

subprojects {
    plugins.apply("java")

    group = property("group") as String
    version = property("version") as String

    configure<org.gradle.api.plugins.JavaPluginExtension> {
        toolchain {
            // use JDK 25 toolchain when available
            languageVersion.set(JavaLanguageVersion.of((property("java.version") as String).toInt()))
        }
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}