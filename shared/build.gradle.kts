import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            export(project(":core"))
            transitiveExport = false
        }
    }
    
    android {
       namespace = "com.lyf.cmpappscaffold.shared"
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
        commonMain.dependencies {
            api(project(":core"))
            implementation(project(":feature:cart"))
            implementation(project(":feature:login"))

            implementation(libs.compose.runtime)

            // 依赖注入
            implementation(libs.koin.core)
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
