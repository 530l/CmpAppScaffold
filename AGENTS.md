# AGENTS.md — CMPDemo 工程 Agent 工作守则

面向 Android/iOS 的 Compose Multiplatform 商业化脚手架（Kotlin 2.4.10 / CMP 1.11.1 / AGP 9.0.1 / compileSdk 36 / minSdk 24）。
模块边界、分层职责的完整说明见 `README.md`，本文件只补充 agent 操作层面的规则。

## 验证命令（改动代码后必须全绿才算完成）

```bash
# 完整链路：单测（Android host + iOS 模拟器双 target）+ Android APK + iOS framework 链接
./gradlew :feature:cart:allTests :shared:allTests \
    :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64
```

- iOS 真机/模拟器实跑：用 Xcode 打开 `iosApp/iosApp.xcodeproj` 运行（Run Script 会自动执行
  `:shared:embedAndSignAppleFrameworkForXcode`）。
- 本机若 `xcode-select -p` 指向 CommandLineTools，Konan 会报
  `xcrun returned non-zero exit code: 72`。临时绕过：命令前加
  `DEVELOPER_DIR=/Applications/Xcode-beta.app/Contents/Developer`；
  根治：`sudo xcode-select -s /Applications/Xcode-beta.app`。
- AGP 9 KMP 模块没有 `testDebugUnitTest` 这类任务名，用 `allTests`（按 target 还有
  `iosSimulatorArm64Test` 等）。
- 不要用 `| tail` 管道包住 gradlew 判断成败，退出码会被吞。

## 模块与依赖铁律

- 依赖方向只允许 `androidApp/iosApp → shared → {core, feature:*}`，feature 只依赖 `core`。
  禁止 `core → shared/feature`（反向依赖）与 feature 互相依赖。
- 跨 feature 跳转只能在 `shared/navigation/AppNavigation.kt` 用回调连接，feature 之间不互引页面。
- 新增数据库 Entity：Entity/Dao 放对应 feature 的 `data/local`，但必须到 `shared` 的
  `AppDatabase` 注册；Room 的 KSP 处理器只挂在 `shared/build.gradle.kts`
  （`kspAndroid` / `kspIosArm64` / `kspIosSimulatorArm64`）。
  注意：DAO 实现类（如 `CartDao_Impl`）只会在 `:shared` 生成，feature 模块内无法独立
  构造 DAO（feature 的 commonTest 跑不了内存库 DAO 测试，属已知取舍）。
- 改数据库结构 = 新版本号 + 提交 `shared/schemas/` 下新 JSON + 写迁移，三件事一起做。
- Ktorfit 接口放 feature 的 `data/remote`；KSP 由 `de.jensklingenberg.ktorfit` 插件
  （≥2.7.5）自动挂载，不要手动加 `ktorfit-ksp` 依赖（2.7.2 插件与 Kotlin 2.4 KMP 不兼容，
  勿回退）。
- UI 文案一律进各 feature 自己的 `composeResources/values/strings.xml`，不在 Composable 里写裸字符串。

## 代码约定

- presentation 层单向数据流：不可变 `UiState`（派生量用计算属性）+ sealed `Intent` +
  `onIntent()` 唯一入口；Composable 子组件只收状态与回调，不持有 ViewModel。
- 金额一律用 `core/model/Money`（最小货币单位 Long），展示用 `formatMoney`，禁止浮点。
- 日志走 `core/log/AppLogger`，不直接依赖 Kermit；网络错误走 `NetworkResult` 边界，
  `CancellationException` 必须原样重抛。
- 共享代码不接收 `Any?` 平台对象，平台差异收在 `androidMain`/`iosMain`。
- 注释和命名不与鸿蒙端工程对标；这是独立演进的 Kotlin 工程。

## 版本与依赖

- 版本号以 `gradle/libs.versions.toml` 为唯一来源。新增/升级依赖先到
  repo1.maven.org 或 dl.google.com 的 `maven-metadata.xml` 核实最新版，
  **search.maven.org 的 latestVersion 会滞后，不可信**。
- KSP 用独立版本号（当前 2.3.11），不跟 Kotlin 版本前缀绑定。
- Navigation 3 必须用 JetBrains 坐标 `org.jetbrains.androidx.navigation3:navigation3-ui`
  （androidx 原版仅 Android variant，CMP 下 iOS 解析失败）。
- 含 commonTest 的模块必须在 android 块显式 `withHostTest {}`，否则 AGP 9 KMP library
  不生成 Android 端单测任务，用例只在 native target 跑。

## Git

- 纯本地仓库，无远端，不推远程；提交署名用
  `git -c user.name="local-snapshot" -c user.email="snapshot@local"`。
- 构建产物、`local.properties`、keystore、`.env*` 均不入库（`.gitignore` 已覆盖）。
- release 签名参数走 `CMP_RELEASE_*` gradle 属性，绝不硬编码进仓库。
