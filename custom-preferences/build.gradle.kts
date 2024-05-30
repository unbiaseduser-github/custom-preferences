plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.sixtyninefourtwenty.custompreferences"
    compileSdk = 34

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.sixtyninefourtwenty"
                artifactId = "custom-preferences"
                version = "2.0.2"

                from(components["release"])

                pom {
                    name.set("custom-preferences")
                    description.set("Extension of the AndroidX preference library that adds more preferences")
                    url.set("https://github.com/unbiaseduser-github/custom-preferences")

                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("unbiaseduser-github")
                            name.set("Dang Quang Trung")
                            email.set("quangtrung02hn16@gmail.com")
                            url.set("https://github.com/unbiaseduser-github")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/unbiaseduser-github/custom-preferences.git")
                        developerConnection.set("scm:git:ssh://github.com:unbiaseduser-github/custom-preferences.git")
                        url.set("https://github.com/unbiaseduser-github/custom-preferences/tree/master")
                    }
                }
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    api("androidx.preference:preference-ktx:1.2.1")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation(project(":colorpicker"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}