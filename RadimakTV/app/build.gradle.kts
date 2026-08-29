import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val bundledM3uProperties = Properties().run {
    val secretsFile = rootProject.file("secrets.properties")
    if (secretsFile.isFile) secretsFile.inputStream().use(::load)
    this
}

val signingProperties = Properties().run {
    val signingFile = rootProject.file("signing.properties")
    if (signingFile.isFile) signingFile.inputStream().use(::load)
    this
}

val bundledM3uUrl1 = bundledM3uProperties.getProperty(
    "BUNDLED_M3U_URL_1",
    "https://iptv-org.github.io/iptv/countries/br.m3u",
).trim()

val bundledM3uUrl2 = bundledM3uProperties.getProperty(
    "BUNDLED_M3U_URL_2",
    "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u8",
).trim()

val bundledM3uUrl3 = bundledM3uProperties.getProperty(
    "BUNDLED_M3U_URL_3",
    "https://iptv-org.github.io/iptv/index.m3u",
).trim()

val bundledM3uUrl4 = bundledM3uProperties.getProperty(
    "BUNDLED_M3U_URL_4",
    "https://raw.githubusercontent.com/joaoguidugli/FTA-IPTV-Brasil/master/playlist.m3u8",
).trim()

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.radimak.tv"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.radimak.r3"
        minSdk = 26
        targetSdk = 35
        versionCode = 16
        versionName = "0.7.2"

        buildConfigField("String", "BUNDLED_M3U_URL_1", bundledM3uUrl1.asBuildConfigString())
        buildConfigField("String", "BUNDLED_M3U_URL_2", bundledM3uUrl2.asBuildConfigString())
        buildConfigField("String", "BUNDLED_M3U_URL_3", bundledM3uUrl3.asBuildConfigString())
        buildConfigField("String", "BUNDLED_M3U_URL_4", bundledM3uUrl4.asBuildConfigString())
        buildConfigField("int", "BUNDLED_M3U_VERSION", "4")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        if (
            signingProperties.getProperty("storeFile").isNullOrBlank().not() &&
            signingProperties.getProperty("storePassword").isNullOrBlank().not() &&
            signingProperties.getProperty("keyAlias").isNullOrBlank().not() &&
            signingProperties.getProperty("keyPassword").isNullOrBlank().not()
        ) {
            create("release") {
                storeFile = rootProject.file(signingProperties.getProperty("storeFile"))
                storePassword = signingProperties.getProperty("storePassword")
                keyAlias = signingProperties.getProperty("keyAlias")
                keyPassword = signingProperties.getProperty("keyPassword")
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
    lint {
        // Mantém versões compatíveis com compileSdk 35 na primeira entrega.
        disable += "GradleDependency"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")

    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")

    testImplementation("junit:junit:4.13.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
