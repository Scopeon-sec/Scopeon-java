plugins {
    `java-library`
}

dependencies {
    api(libs.jakarta.persistence.api)
    api(libs.jakarta.validation.api)
    implementation(libs.jackson.databind)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testImplementation(libs.junit.jupiter)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}