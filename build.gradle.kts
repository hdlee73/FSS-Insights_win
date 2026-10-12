plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

group = "com.hdlee73.fssinsights"
version = "1.0.0"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")

    // HTTP & Scraping
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jsoup:jsoup:1.18.3")

    // JSON handling
    implementation("org.json:json:20240303")

    // SQLite JDBC for Local Storage
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")
}

compose.desktop {
    application {
        mainClass = "com.hdlee73.fssinsights.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe
            )
            packageName = "FSSInsights"
            packageVersion = "1.0.0"
            description = "FSS Insights - Financial News and Report Radar"
            vendor = "hdlee73"
            copyright = "Copyright 2026 hdlee73"
        }
    }
}
