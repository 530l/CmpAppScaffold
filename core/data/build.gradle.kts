import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.lyf.cmp.core.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        androidMain.dependencies {
            // Ktor Android 端引擎。
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            // Ktor iOS 端引擎。
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            api(project(":core:common"))

            // 网络与序列化
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktorfit.lib.light)
            // feature 的 Ktorfit 接口可直接挂 ResponseConverter，随 data 层传递。
            api(libs.ktorfit.converters.response)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            // 依赖注入
            implementation(libs.koin.core)

            // 键值存储：实现细节不外泄，业务只依赖 KeyValueStore 接口
            implementation(libs.mmkv.kmp)
        }
    }
}
