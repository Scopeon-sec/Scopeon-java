import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    // no top-level plugins here; modules define what they need
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