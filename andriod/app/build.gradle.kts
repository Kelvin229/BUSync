plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.cosc3p97project.busync"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cosc3p97project.busync"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Apply the Google Services plugin only for the release build type
            apply(plugin = "com.google.gms.google-services")
        }
        getByName("debug") {
            // Enable debuggable mode
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Add this block to handle duplicate files during the build
    packagingOptions {
        pickFirst("META-INF/INDEX.LIST") // Picks the first occurrence and ignores the rest
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation ("androidx.activity:activity:1.1.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    implementation ("com.google.firebase:firebase-appcheck-debug:16.0.0-beta02")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    implementation ("com.google.firebase:firebase-firestore-ktx:24.0.1")
    implementation ("org.slf4j:slf4j-api:2.0.12")
    implementation ("ch.qos.logback:logback-classic:1.4.14")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.theartofdev.edmodo:android-image-cropper:2.8.0")
    implementation("com.firebaseui:firebase-ui-database:7.2.0")
    implementation("de.hdodenhof:circleimageview:2.2.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
