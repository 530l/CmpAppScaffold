import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    android {
       namespace = "com.lyf.cmpdemo.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            // Ktor Android 端引擎（KMP 里 engine 是少数必须按平台声明的依赖）
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            // Ktor iOS 端引擎
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // === 商业化技术栈（2026-08-26 定稿）===
            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // 网络 + 序列化
            implementation(libs.ktor.client.core)
            implementation(libs.ktorfit.lib)
            implementation(libs.kotlinx.serialization.json)
            // 数据库 / 键值存储
            implementation(libs.androidx.room.runtime)
            implementation(libs.mmkv.kmp)
            // 图片
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            // 日志 / 时间 / 导航
            implementation(libs.kermit)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.navigation3.runtime)
            implementation(libs.jetbrains.navigation3.ui)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.turbine)
            implementation(libs.mockative)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    // KSP 处理器按 target 各挂一次（AGP 9 KMP 插件下 Android 的配置名为 kspAndroid，
    // 若 Gradle 版本演进导致名称变化，用 `gradlew tasks --all | grep -i ksp` 实测）
    listOf(
        "kspAndroid",
        "kspIosArm64",
        "kspIosSimulatorArm64",
    ).forEach { kspConfiguration ->
        add(kspConfiguration, libs.androidx.room.compiler)
        add(kspConfiguration, libs.ktorfit.ksp)
    }
}