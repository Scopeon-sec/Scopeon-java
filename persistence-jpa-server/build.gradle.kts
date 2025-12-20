plugins {
    `java-library`
}

dependencies {
    // Depend on core and base persistence layer
    api(project(":core"))
    api(project(":persistence-jpa"))
    
    // JPA and validation APIs
    api(libs.jakarta.persistence.api)
    api(libs.jakarta.validation.api)
    implementation(libs.jakarta.transaction.api)
    implementation(libs.hibernate.core)
    
    // Lombok for cleaner code
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    // Testing
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    
    // Database support for tests
    testImplementation(libs.h2)
    testRuntimeOnly(libs.hibernate.core)
}

tasks.test {
    useJUnitPlatform()
}
