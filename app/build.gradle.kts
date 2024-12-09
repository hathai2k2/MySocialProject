import java.util.Properties
import java.io.FileInputStream
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("androidx.navigation.safeargs.kotlin")
    id ("com.google.dagger.hilt.android")
    id ("kotlin-parcelize")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.mysocialproject"
    compileSdk = 34
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        val inputStream = FileInputStream(localPropertiesFile)
        localProperties.load(inputStream)
    }
    defaultConfig {
        applicationId = "com.example.mysocialproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("apiKey")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    dataBinding {
        enable = true
    }
}
kapt {
    correctErrorTypes = true
}
dependencies {
    implementation("androidx.activity:activity:1.9.0")
    implementation(libs.androidx.camera.view)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.analytics)

    val camerax_version = "1.3.4"
    implementation ("androidx.camera:camera-core:${camerax_version}")
    implementation ("androidx.camera:camera-camera2:${camerax_version}")
    implementation ("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation ("androidx.camera:camera-video:${camerax_version}")

    implementation ("androidx.camera:camera-view:${camerax_version}")
    implementation ("androidx.camera:camera-extensions:${camerax_version}")
//    implementation(libs.firebase.database)
//    implementation(libs.firebase.auth)
//    implementation(libs.firebase.storage)
//    implementation(libs.firebase.firestore)
    val nav_version = "2.7.7"

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")


    implementation ("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation ("androidx.navigation:navigation-ui-ktx:$nav_version")

    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")

    //coroutines
    val coroutinesVer = "1.5.2"
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVer")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVer")


    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

    val room_version = "2.6.1"
    implementation ("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    annotationProcessor ("androidx.room:room-compiler:$room_version")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.datastore:datastore:1.1.1")

    implementation (libs.audiowave.progressbar)

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.11.0")
    implementation ("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-dynamic-links")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.github.bumptech.glide:recyclerview-integration:4.16.0")
    //paging3
    implementation ("androidx.paging:paging-runtime-ktx:3.3.0")

    //animartion
    implementation ("com.facebook.shimmer:shimmer:0.5.0")

    //
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")

    //kotlin serialization
    implementation ("com.sealwu.jsontokotlin:library:3.7.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    val multidex_version = "2.0.1"
    implementation("androidx.multidex:multidex:$multidex_version")
    //gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    //
    implementation ("com.google.guava:guava:32.1.3-android")
    //
    implementation ("org.ocpsoft.prettytime:prettytime:5.0.2.Final")
    implementation("com.github.marlonlom:timeago:4.0.3")


}