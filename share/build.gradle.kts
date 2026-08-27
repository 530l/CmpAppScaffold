import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Share"
            isStatic = true
            // Swift 仅需要宿主配置模型；数据层和 Compose 设计实现不进入 framework API。
            export(project(":core:common"))
            transitiveExport = false
        }
    }

    android {
       namespace = "com.lyf.cmp.share"
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
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            // AppConfig 是宿主可配置 API，其余 core 实现不暴露给 Android/iOS 壳。

            api(project(":core:common"))
            implementation(project(":core:data"))
            implementation(project(":core:design"))

            implementation(project(":feature:home"))
            implementation(project(":feature:browse"))
            implementation(project(":feature:message"))
            implementation(project(":feature:cart"))
            implementation(project(":feature:login"))
            implementation(project(":feature:mine"))

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.material.icons.core)

            // 依赖注入
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.ktor.client.core)
            // 导航路由聚合与初始化
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.atomicfu)
            // 数据库
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    packageOfResClass = "com.lyf.cmp.share.resources"
}

dependencies {
    // Room 的 KSP 处理器必须为每个实际目标平台注册。
    listOf(
        "kspAndroid",
        "kspIosArm64",
        "kspIosSimulatorArm64",
    ).forEach { kspConfiguration ->
        add(kspConfiguration, libs.androidx.room.compiler)
    }
}
