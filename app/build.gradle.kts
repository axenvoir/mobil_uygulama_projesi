import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val roomTestSerializationVersion = libs.versions.serialization.get()

val keystoreProperties = Properties().apply {
    val keystoreFile = rootProject.file("keystore.properties")
    if (keystoreFile.exists()) {
        keystoreFile.inputStream().use { load(it) }
    }
}

val productionAdMobAppId = providers.gradleProperty("expiraAdMobAppId")
    .orElse(providers.environmentVariable("EXPIRA_ADMOB_APP_ID"))
    .getOrElse("")
val productionRewardedAdUnitId = providers.gradleProperty("expiraRewardedAdUnitId")
    .orElse(providers.environmentVariable("EXPIRA_REWARDED_AD_UNIT_ID"))
    .getOrElse("")
val validAdMobAppId = Regex("^ca-app-pub-\\d{16}~\\d{10}$")
val validRewardedAdUnitId = Regex("^ca-app-pub-\\d{16}/\\d{10}$")
val productionAdsConfigured = validAdMobAppId.matches(productionAdMobAppId) &&
    validRewardedAdUnitId.matches(productionRewardedAdUnitId) &&
    productionAdMobAppId != "ca-app-pub-3940256099942544~3347511713" &&
    productionRewardedAdUnitId != "ca-app-pub-3940256099942544/5224354917"
fun String.asBuildConfigString(): String = "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.axenvoir.expira"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.expira.app.tr"
        minSdk = 26
        targetSdk = 37
        versionCode = 101
        versionName = "2.1.0"
        manifestPlaceholders["admobAppId"] = ""
        buildConfigField("String", "ADMOB_APP_ID", "\"\"")
        buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"\"")
        buildConfigField("Boolean", "ADS_ENABLED", "false")
    }

    androidResources {
        localeFilters += listOf("tr", "en")
    }

    signingConfigs {
        if (keystoreProperties.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["admobAppId"] = productionAdMobAppId
            buildConfigField("String", "ADMOB_APP_ID", productionAdMobAppId.asBuildConfigString())
            buildConfigField("String", "REWARDED_AD_UNIT_ID", productionRewardedAdUnitId.asBuildConfigString())
            buildConfigField("Boolean", "ADS_ENABLED", productionAdsConfigured.toString())
            if (keystoreProperties.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
            buildConfigField("String", "REWARDED_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/5224354917\"")
            buildConfigField("Boolean", "ADS_ENABLED", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }



    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = true
        disable += setOf(
            "AndroidGradlePluginVersion",
            "GradleDependency",
            "PluralsCandidate",
            "IconLauncherShape",
        )
    }

    testOptions {
        animationsDisabled = true
    }

    sourceSets {
        getByName("androidTest") {
            assets.directories.add(projectDir.resolve("schemas").absolutePath)
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

configurations.configureEach {
    if (name in setOf(
            "debugCompileClasspath",
            "debugRuntimeClasspath",
            "debugAndroidTestCompileClasspath",
            "debugAndroidTestRuntimeClasspath",
        )
    ) {
        resolutionStrategy.force(
            "org.jetbrains.kotlinx:kotlinx-serialization-core:$roomTestSerializationVersion",
            "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:$roomTestSerializationVersion",
            "org.jetbrains.kotlinx:kotlinx-serialization-json:$roomTestSerializationVersion",
            "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:$roomTestSerializationVersion",
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.billing)
    implementation(libs.google.mobile.ads)
    implementation(libs.google.ump)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.androidx.work.runtime.ktx)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.truth)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
