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
        namespace = "com.lyf.cmp.core.design"
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
        commonMain.dependencies {
            api(libs.jetbrains.navigation3.ui)
            implementation(libs.kotlinx.serialization.json)

            // 共享界面基础能力
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            // 图片加载走共享 Ktor 实例
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.ktor.client.core)

            implementation(libs.kotlinx.coroutines.core)

            // 导航 Entry 的 ViewModel 生命周期隔离
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
            // ObserveAsEvents / LocalLifecycleOwner 等 common 端 lifecycle-compose API
            implementation(libs.androidx.lifecycle.runtimeCompose)
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
