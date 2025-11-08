import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Read the local.properties file to access the API keys
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

// Force the use of the compatible MySQL driver version across all configurations
configurations.all {
    resolutionStrategy {
        force("mysql:mysql-connector-java:5.1.49")
    }
}

android {
    namespace = "com.example.foodshelfscanner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.foodshelfscanner"
        minSdk = 24 // Reverted to 24, as the older driver is compatible
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Make the API key available in the BuildConfig
        val spoonacularApiKey = localProperties.getProperty("SPOONACULAR_API_KEY")?.trim('"')
        buildConfigField("String", "SPOONACULAR_API_KEY", "\"$spoonacularApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packagingOptions {
        // Exclude duplicate files from the old JDBC driver
        exclude("META-INF/services/java.sql.Driver")
    }
}

dependencies {
    // --- Core AndroidX ---
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.12.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")

    // --- Material Design ---
    implementation("com.google.android.material:material:1.11.0")

    // --- Layouts ---
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // --- RecyclerView and CardView ---
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // --- Networking (Retrofit & Gson) ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // --- Jetpack Compose ---
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.0")

    // --- Testing ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- ✅ JDBC Driver (MySQL) - Force older, compatible version ---
    implementation("mysql:mysql-connector-java:5.1.49")
}
