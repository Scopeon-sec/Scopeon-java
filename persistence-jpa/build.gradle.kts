plugins {
    `java-library`
}

dependencies {
    api(project(":core"))
    api(libs.jakarta.persistence.api)
    api(libs.jakarta.validation.api)
    implementation(libs.jakarta.transaction.api)
    implementation(libs.hibernate.core)
    // Optional datasource/runtime helpers
    implementation(libs.hikaricp)
    runtimeOnly(libs.postgresql)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

    // Integration test dependencies
    testImplementation(libs.h2)
    testRuntimeOnly(libs.hibernate.core)
}

tasks.test {
    useJUnitPlatform()
}