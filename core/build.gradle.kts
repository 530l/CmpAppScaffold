import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.lyf.cmp.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        // 不开启 withHostTest 时 AGP 9 KMP library 不会生成 Android 端单测任务，
        // commonTest 里的用例将只在 native target 执行。
        withHostTest {
            isIncludeAndroidResources = true
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
            api(libs.jetbrains.navigation3.ui)
            api(libs.kotlinx.serialization.json)

            // 共享界面基础能力
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            // 依赖注入
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // 网络与序列化
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktorfit.lib.light)
            implementation(libs.kotlinx.coroutines.core)

            // 导航 Entry 的 ViewModel 生命周期隔离
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)

            // 图片与日志
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.kermit)

            // 键值存储：实现细节不外泄，业务只依赖 core/storage 的 KeyValueStore 接口
            implementation(libs.mmkv.kmp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

compose.resources {
    packageOfResClass = "com.lyf.cmp.core.resources"
}
