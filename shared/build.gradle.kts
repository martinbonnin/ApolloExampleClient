plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.apollographql.apollo.external")
}

apollo {
    service("default") {
        srcDir("src/commonMain/graphql/default")
        schemaFiles.from("src/commonMain/graphql/schema.graphqls")
        packageName.set("default")
    }
    service("catch") {
        srcDir("src/commonMain/graphql/catch")
        schemaFiles.from("src/commonMain/graphql/schema.graphqls", "src/commonMain/graphql/catch/extra.graphqls")
        packageName.set("catch")
    }
}

kotlin {
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.apollographql.apollo:apollo-runtime")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("com.apollographql.mockserver:apollo-mockserver:0.0.3")
            }
        }
        val androidMain by getting
        val androidUnitTest by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "com.example.apolloexampleclient"
    compileSdk = 33
    defaultConfig {
        minSdk = 24
        targetSdk = 33
    }
}