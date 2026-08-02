plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

// agent-api — provider-neutral SPI for the in-IDE coding agent (see docs/agentic-coding.md).
dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
}
