plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.sonar)
    id("jacoco")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

jacoco {
    toolVersion = "0.8.12"
}

secrets {
    propertiesFileName = "secrets.properties"

    defaultPropertiesFileName = "local.defaults.properties"
}

android {
    namespace = "com.swentseekr.seekr"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.swentseekr.seekr"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            "MAPS_API_KEY",
            "\"${project.findProperty("MAPS_API_KEY") ?: System.getenv("MAPS_API_KEY") ?: ""}\""
        )
        manifestPlaceholders["MAPS_API_KEY"] =
            project.findProperty("MAPS_API_KEY") ?: System.getenv("MAPS_API_KEY") ?: ""
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.2"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }

    // Robolectric needs to be run only in debug. But its tests are placed in the shared source set (test)
    // The next lines transfers the src/test/* from shared to the testDebug one
    //
    // This prevent errors from occurring during unit tests
    sourceSets.getByName("testDebug") {
        val test = sourceSets.getByName("test")

        java.setSrcDirs(test.java.srcDirs)
        res.setSrcDirs(test.res.srcDirs)
        resources.setSrcDirs(test.resources.srcDirs)
    }

    sourceSets.getByName("test") {
        java.setSrcDirs(emptyList<File>())
        res.setSrcDirs(emptyList<File>())
        resources.setSrcDirs(emptyList<File>())
    }
}

sonar {
    //disable automatic analysis
    properties {
        property("sonar.projectKey", "SwentSeekr_seekr")
        property("sonar.projectName", "seekr")
        property("sonar.organization", "swentsonar")
        property("sonar.host.url", "https://sonarcloud.io")
        // Comma-separated paths to the various directories containing the *.xml JUnit report files. Each path may be absolute or relative to the project base directory.
        property("sonar.junit.reportPaths", "${project.layout.buildDirectory.get()}/test-results/testDebugunitTest/")
        // Paths to xml files with Android Lint issues. If the main flavor is changed, this file will have to be changed too.
        property("sonar.androidLint.reportPaths", "${project.layout.buildDirectory.get()}/reports/lint-results-debug.xml")
        // Paths to JaCoCo XML coverage report files.
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

// When a library is used both by robolectric and connected tests, use this function
fun DependencyHandlerScope.globalTestImplementation(dep: Any) {
    androidTestImplementation(dep)
    testImplementation(dep)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    globalTestImplementation(libs.androidx.junit)
    globalTestImplementation(libs.androidx.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")

    // MockK for mocking in tests
    testImplementation("io.mockk:mockk:1.13.5")

    // Cloud Firestore
    implementation("com.google.firebase:firebase-firestore")

    //Firebase storage
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation("com.google.protobuf:protobuf-javalite:3.21.12")
    testImplementation("com.google.protobuf:protobuf-javalite:3.21.12")
    androidTestImplementation("com.google.protobuf:protobuf-javalite:3.21.12")

    // Google Maps Compose library
    val mapsComposeVersion = "4.4.1"
    implementation("com.google.maps.android:maps-compose:$mapsComposeVersion")
    // Google Maps Compose utility library
    implementation("com.google.maps.android:maps-compose-utils:$mapsComposeVersion")
    // Google Maps Compose widgets library
    implementation("com.google.maps.android:maps-compose-widgets:$mapsComposeVersion")
    implementation("com.google.maps.android:android-maps-utils:3.8.2")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    //Coil Compose for loading images from camera/gallery
    implementation("io.coil-kt:coil-compose:2.4.0")

    // ------------- Jetpack Compose ------------------
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    globalTestImplementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    // Material Design 3
    implementation(libs.compose.material3)
    // Integration with activities
    implementation(libs.compose.activity)
    // Integration with ViewModels
    implementation(libs.compose.viewmodel)
    // Android Studio Preview support
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
    // UI Tests
    globalTestImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)
    // For createAndroidComposeRule in tests
    androidTestUtil("androidx.test:orchestrator:1.4.2")


    // --------- Kaspresso test framework ----------
    globalTestImplementation(libs.kaspresso)
    globalTestImplementation(libs.kaspresso.compose)

    // ----------       Robolectric     ------------
    testImplementation(libs.robolectric)
    testImplementation("org.robolectric:robolectric:4.12.1")
}

tasks.withType<Test> {
    // Configure Jacoco for each tests
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    mustRunAfter("testDebugUnitTest", "connectedDebugAndroidTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )

    val debugTree = fileTree("${buildDir}/intermediates/javac/debug") {
        exclude(fileFilter)
    }

    val kotlinDebugTree = fileTree("${buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree, kotlinDebugTree))
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "jacoco/testDebugUnitTest.exec",
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "outputs/code_coverage/debugAndroidTest/connected/**/*.ec"
            )
        }
    )

    doLast {
        println("✅ JaCoCo report generated at: ${reports.html.outputLocation.get().asFile.absolutePath}")
    }
}

tasks.withType<Test>().configureEach {
    jacoco {
        toolVersion = "0.8.12"
    }
}
// --- 🧹 Workaround: clean invalid JaCoCo XML lines (e.g., nr="65535") for SonarCloud ---
tasks.named<JacocoReport>("jacocoTestReport").configure {
    doLast {
        val reportFile = reports.xml.outputLocation.get().asFile
        if (reportFile.exists()) {
            val original = reportFile.readText()
            val cleaned = original.replace(Regex("""<line[^>]+nr="65535"[^>]*/>"""), "")
            if (original != cleaned) {
                reportFile.writeText(cleaned)
                println("🧹 Cleaned JaCoCo XML: removed invalid line entries (e.g., nr=65535)")
            } else {
                println("✅ JaCoCo XML is clean — no invalid line entries found.")
            }
        } else {
            println("⚠️ JaCoCo XML report not found: ${reportFile.absolutePath}")
        }
    }
}

configurations.matching { it.name.contains("test", ignoreCase = true) }.all {
    resolutionStrategy {
        force("org.jacoco:org.jacoco.agent:0.8.12")
        force("org.jacoco:org.jacoco.core:0.8.12")
        force("org.jacoco:org.jacoco.report:0.8.12")
        force("org.jacoco:org.jacoco.ant:0.8.12")
    }
}

configurations.configureEach {
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
}