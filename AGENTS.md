# AGENTS.md — CmpAppScaffold 工程 Agent 工作守则

面向 Android/iOS 的 Compose Multiplatform 商业化脚手架（Kotlin 2.4.10 / CMP 1.11.1 / AGP 9.0.1 / compileSdk 36 / minSdk 24）。
模块边界、分层职责的完整说明见 `README.md`，本文件只补充 agent 操作层面的规则。

## 验证命令（改动代码后必须全绿才算完成）

```bash
# 完整链路：单测（Android host + iOS 模拟器双 target）+ Android APK + iOS framework 链接
./gradlew :feature:cart:allTests :core:design:allTests :share:allTests \
    :androidApp:assembleDebug :share:linkDebugFrameworkIosSimulatorArm64
```

- iOS 真机/模拟器实跑：用 Xcode 打开 `iosApp/iosApp.xcodeproj` 运行（Run Script 会自动执行
  `:share:embedAndSignAppleFrameworkForXcode`）。
- 本机若 `xcode-select -p` 指向 CommandLineTools，Konan 会报
  `xcrun returned non-zero exit code: 72`。临时绕过：命令前加
  `DEVELOPER_DIR=/Applications/Xcode-beta.app/Contents/Developer`；
  根治：`sudo xcode-select -s /Applications/Xcode-beta.app`。
- AGP 9 KMP 模块没有 `testDebugUnitTest` 这类任务名，用 `allTests`（按 target 还有
  `iosSimulatorArm64Test` 等）。
- 不要用 `| tail` 管道包住 gradlew 判断成败，退出码会被吞。

## 模块与依赖铁律

- 依赖方向只允许 `androidApp/iosApp → share → {core:common, core:data, core:design, feature:*}`；
  feature 只依赖 core 三模块。core 内部只允许 `core:data / core:design → core:common` 单向，
  禁止 core → share/feature（反向依赖）、feature 互相依赖、core:common 依赖任何兄弟模块。
- 底部 tab 的注册点是 `share/navigation/TopLevelTab.kt`（枚举持路由）+
  `AppNavigation.kt` 的 bottomBar；多返回栈机制在 `core:design/navigation/TabNavigation.kt`，
  切 tab 不清栈，各 tab 返回历史独立。登录、支付等全局全屏流程走 `AppNavigation.kt`
  的根栈，不塞进任一 tab 栈。
- 跨 feature 跳转只能在 `share/navigation/AppNavigation.kt` 用回调连接，feature 之间不互引页面。
- 新增数据库 Entity：Entity/Dao 放对应 feature 的 `data/local`，但必须到 `share` 的
  `AppDatabase` 注册；Room 的 KSP 处理器只挂在 `share/build.gradle.kts`
  （`kspAndroid` / `kspIosArm64` / `kspIosSimulatorArm64`）。
  注意：DAO 实现类（如 `CartDao_Impl`）只会在 `:share` 生成，feature 模块内无法独立
  构造 DAO（feature 的 commonTest 跑不了内存库 DAO 测试，属已知取舍）。
- 改数据库结构 = 新版本号 + 提交 `share/schemas/` 下新 JSON + 写迁移，三件事一起做。
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
- 键值存储只注入 `core/storage/KeyValueStore` 接口，key 用业务模块的常量对象集中声明，
  不在调用点写裸字符串；MMKV 是 native 实现，JVM host 单测跑不了真实现，测试用内存 Fake。
- 平台专属入口/工厂（无 expect 契约的独立平台函数对）一律用平台后缀 object 命名：
  `PlatformIos`/`PlatformAndroid`（启动初始化）、`DatabaseIos`/`DatabaseAndroid`（建库）、
  `StorageIos`/`StorageAndroid`（MMKV 初始化）；expect/actual 声明仍用同名声明+文件名后缀
  （`AppDatabaseConstructor`、`PlatformExitHandler`）。
- 共享代码不接收 `Any?` 平台对象，平台差异收在 `androidMain`/`iosMain`。
- 注释和命名不与鸿蒙端工程对标；这是独立演进的 Kotlin 工程。
- Nav3 路由 `data object` 必须覆写 `toString()` 返回 `接口名.对象名`（如 `"CartRoute.Main"`）：
  Nav3 用 `key.toString()` 作 contentKey，是 saveable 状态（含滚动位置）与 entry 级
  ViewModelStore 的存取键；裸 `data object Main` 跨 feature 全叫 "Main"，会互相覆盖、
  弹出时互相误删（症状：返回/切 tab 后列表回顶部）。
- 多返回栈 tab 必须走 `core:design` 的 `TabAppNavHost`：每个 tab 的栈各自调用
  `rememberDecoratedNavEntries`，并持有各自独立的 entry decorators，NavDisplay 按 entries 切换。
  不要把不同栈轮流塞给同一个 NavDisplay 的 backStack 参数——上一 tab 的 entry 会被
  误判为弹出并清掉状态。
- 底栏是常驻 overlay（share 的 Box 底层 + `TabAppNavHost` 的 entry 层让位），
  不要改回 Scaffold bottomBar + AnimatedVisibility 方案：底栏显隐动画会在页面进场后
  释放占位、引发内容二次跳动（垂直居中布局可见标题下坠）。tab 根页面让位高度 =
  `TabBarContentHeight` + 手势区 inset；换非标准高度底栏要把实际高度传给 TabAppNavHost。
  tab 内二级页覆盖底栏时必须同步禁用底栏交互与无障碍语义；全局页由根栈覆盖整个 tab shell。
- 应用窗口保持 edge-to-edge，根导航不统一添加 safeDrawing padding；页面背景铺满窗口，
  文字、按钮等交互内容由页面自己的 Scaffold/TopAppBar/WindowInsets 避让系统栏和刘海。

## 版本与依赖

- 版本号以 `gradle/libs.versions.toml` 为唯一来源。新增/升级依赖先到
  repo1.maven.org 或 dl.google.com 的 `maven-metadata.xml` 核实最新版，
  **search.maven.org 的 latestVersion 会滞后，不可信**。
- KSP 用独立版本号（当前 2.3.11），不跟 Kotlin 版本前缀绑定。
- Navigation 3 必须用 JetBrains 坐标 `org.jetbrains.androidx.navigation3:navigation3-ui`
  （androidx 原版仅 Android variant，CMP 下 iOS 解析失败）。
- 含 commonTest 的模块必须在 android 块显式 `withHostTest {}`，否则 AGP 9 KMP library
  不生成 Android 端单测任务，用例只在 native target 跑。
- MMKV（`com.tencent:mmkv-kmp`）：iOS 端 `MMKV.initialize` 必须在主线程、且先于一切读写
  （已由 `initSharedApp` 首行的 `initPlatformStorage` 保证，勿调整顺序）；iOS 侧不要再通过
  CocoaPods/SwiftPM 引入 MMKV 原生库，否则与 KMP 依赖重复链接。

## Git

- 远端为 `https://github.com/530l/CmpAppScaffold`（公开仓库），提交后正常 push。
- agent 代跑的提交署名 `git -c user.name="local-snapshot" -c user.email="snapshot@local"`，
  用户手动提交用本人全局身份。
- 构建产物、`local.properties`、keystore、`.env*` 均不入库（`.gitignore` 已覆盖）。
- release 签名参数走 `CMP_RELEASE_*` gradle 属性，绝不硬编码进仓库。
