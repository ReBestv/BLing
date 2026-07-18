import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun projectConfig(name: String, fallback: String = ""): String {
    return localProperties.getProperty(name)
        ?: providers.gradleProperty(name).orNull
        ?: providers.environmentVariable(name).orNull
        ?: fallback
}

fun String.asBuildConfigString(): String {
    return "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

val releaseSigningKeys = listOf(
    "RELEASE_STORE_FILE",
    "RELEASE_STORE_PASSWORD",
    "RELEASE_KEY_ALIAS",
    "RELEASE_KEY_PASSWORD"
)
val hasReleaseSigning = releaseSigningKeys.all { projectConfig(it).isNotBlank() }

android {
    namespace = "com.standbyus.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.standbyus.app"
        minSdk = 31
        targetSdk = 35
        versionCode = 4
        versionName = "1.3"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            projectConfig("SUPABASE_URL", "https://example.supabase.co").trimEnd('/').asBuildConfigString()
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            projectConfig("SUPABASE_ANON_KEY").asBuildConfigString()
        )
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(projectConfig("RELEASE_STORE_FILE"))
                storePassword = projectConfig("RELEASE_STORE_PASSWORD")
                keyAlias = projectConfig("RELEASE_KEY_ALIAS")
                keyPassword = projectConfig("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.compose.navigation)
    implementation(libs.compose.activity)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.okhttp)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)
    implementation(libs.coroutines.android)
    implementation(libs.coil)
    implementation(libs.coil.gif)
    testImplementation(libs.junit)
    testImplementation(libs.json)
}
