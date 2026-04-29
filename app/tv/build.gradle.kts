import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.dagger.hilt.android)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun resolveSecret(name: String): String? {
    return providers.gradleProperty(name).orNull
        ?: localProperties.getProperty(name)
        ?: System.getenv(name)
}

fun resolveReleaseKeystoreFile() = sequenceOf(
    resolveSecret("RELEASE_KEYSTORE_FILE")?.takeIf { it.isNotBlank() }?.let(::file),
    rootProject.file("app/keystore")
        .takeIf { it.exists() }
        ?.walkTopDown()
        ?.firstOrNull { candidate ->
            candidate.isFile && candidate.extension.lowercase() in setOf("jks", "keystore")
        },
).filterNotNull().firstOrNull()

val releaseKeystoreFile = resolveReleaseKeystoreFile()
val releaseStorePassword = resolveSecret("KEYSTORE_PASSWORD")
val releaseKeyAlias = resolveSecret("RELEASE_SIGN_KEY_ALIAS")
val releaseKeyPassword = resolveSecret("RELEASE_SIGN_KEY_PASSWORD")
val hasReleaseSigning = releaseKeystoreFile != null &&
        !releaseStorePassword.isNullOrBlank() &&
        !releaseKeyAlias.isNullOrBlank() &&
        !releaseKeyPassword.isNullOrBlank()
val isCiBuild = !System.getenv("GITHUB_ACTIONS").isNullOrBlank() || !System.getenv("CI").isNullOrBlank()

if (isCiBuild && !hasReleaseSigning) {
    error(
        "Release signing is not configured for app/tv. " +
            "Expected a keystore in app/keystore or RELEASE_KEYSTORE_FILE plus KEYSTORE_PASSWORD, " +
            "RELEASE_SIGN_KEY_ALIAS and RELEASE_SIGN_KEY_PASSWORD.",
    )
}

android {
    namespace = "io.github.posaydone.kinopub.tv"
    compileSdk = 37

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = releaseKeystoreFile
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "io.github.posaydone.kinopub.tv"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "0.2"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":app:shared"))
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(project(":core:data"))
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.ui)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material.icons.extended)

    //Media
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.palette.ktx)

    //Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)

    // nav3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)
    
    // For image loading in Compose
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.material.kolor)
    implementation(libs.androidx.core.splashscreen)
}
