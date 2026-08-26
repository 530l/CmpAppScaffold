# CmpAppScaffold

一个面向 Android 与 iOS 的 Compose Multiplatform / Kotlin Multiplatform 商业项目脚手架。当前购物车示例不是内存假数据：选中状态由 Room 持久化，并通过 `Flow → StateFlow → Compose` 单向更新 UI。

## 模块边界

```text
androidApp / iosApp        平台壳、权限、签名和发布配置
          ↓
shared                    共用应用入口、初始化、DI/数据库与导航聚合
  ├── core                配置、网络、图片、日志、设计系统与 Navigation 3 封装
  ├── feature:cart        购物车 data / domain / presentation / EntryProvider
  └── feature:login       登录页面骨架、路由与 EntryProvider
```

- `core` 是独立 KMP 基础模块，只提供可复用能力，禁止依赖 `shared` 或任何 `feature` 包。
- `shared` 是 Android/iOS 共用的应用组合根，通过 `api(project(":core"))` 暴露基础接口；宿主仍只依赖 `shared`。
- `feature:cart`、`feature:login` 是独立 KMP Gradle 模块，只依赖 `core`，禁止 Feature 之间直接依赖。
- `AppDatabase` 留在 `shared` 的应用级 `com.lyf.cmp.database` 包，由它聚合各业务 Entity，避免出现 `core → feature` 反向依赖。
- `domain` 只放纯 Kotlin 模型和规则，不引用 Compose、Room、Ktor。
- `data` 负责 DAO、DTO、映射和 Repository 实现。
- `presentation` 使用不可变 `UiState` 和 `Intent`；Composable 子组件只接收状态与回调。
- 平台差异集中在 `androidMain` / `iosMain`，共享业务代码不接收 `Any?` 平台对象。
- 每个 Feature 通过自己的 `EntryProviderScope<NavKey>` 扩展注册页面，跨 Feature 跳转由 `shared` 连接回调。

## 已接入的基础能力

- Koin：核心能力、平台数据库和 feature 依赖注入。
- Ktor：平台引擎、JSON 容错、统一超时、HTTPS 基地址、可脱敏请求日志、`NetworkResult` 错误边界。
- Ktorfit：Retrofit 风格的声明式 API；使用 `ktorfit-lib-light`，复用并保留项目自己的 Ktor 客户端配置。
- Room + bundled SQLite：跨平台持久化、响应式查询、schema 导出目录。
- Coil：复用应用级 Ktor `HttpClient` 的单例图片加载器和统一 `AppImage` 入口。
- Kermit：统一日志门面，业务代码不绑定具体日志库。
- Compose Resources / Material 3：Feature 自有文案、亮色与深色主题、加载/错误/空状态。
- Navigation 3：可序列化跨平台 back stack、统一 `AppNavigator`、Entry 级状态保存及 ViewModel 生命周期。
- 发布基础：Android release 混淆与资源压缩、禁止明文流量和系统备份；iOS 已放置 Privacy Manifest。

业务 API 统一使用 Ktorfit 声明，并在 `RemoteDataSource` 中处理响应与异常：

```kotlin
interface ProductApi {
    @GET("v1/products")
    suspend fun getProducts(): HttpResponse
}

class ProductRemoteDataSource(
    private val api: ProductApi,
) {
    suspend fun getProducts(): NetworkResult<List<ProductDto>> = safeRequest {
        api.getProducts()
    }
}
```

Ktorfit 负责生成接口实现；`safeRequest` 会把 HTTP、连接、序列化及未知错误映射成 `NetworkError`，协程取消继续向上抛出。默认仅对幂等请求的 5xx 与传输异常最多退避重试两次。

## 环境配置

`AppConfig` 默认使用占位地址 `https://api.example.com/`。实际项目应在 Android product flavor / Gradle BuildConfig 与 iOS xcconfig 中分别提供环境值，再在调用 `initSharedApp` 时注入：

```kotlin
initSharedApp(
    context = this,
    config = AppConfig(
        environment = AppEnvironment.PRODUCTION,
        apiBaseUrl = "https://api.your-company.com/",
        enableNetworkLogging = false,
    ),
)
```

不要把 API token、证书密码、签名私钥或真实生产密钥提交到仓库。当前 iOS 签名配置按项目现状保留，团队化前建议把 Team ID 等环境差异迁移到不入库的本地 xcconfig。

Android release 支持通过用户级 `~/.gradle/gradle.properties` 注入签名；四项必须同时提供，否则配置阶段会明确失败：

```properties
CMP_RELEASE_STORE_FILE=/absolute/path/to/release.keystore
CMP_RELEASE_STORE_PASSWORD=replace_me
CMP_RELEASE_KEY_ALIAS=replace_me
CMP_RELEASE_KEY_PASSWORD=replace_me
```

## 数据库升级约定

1. 修改 Entity 后递增 `AppDatabase.version`。
2. 提交 `shared/schemas` 生成的新 schema JSON。
3. 提供显式 migration 并覆盖升级测试；生产环境禁止使用 destructive migration。

## 新增业务功能约定

当前业务按独立的 `feature/<feature-name>` KMP Gradle 模块组织：

1. 在 Feature 内建立 `data`、`domain`、`presentation`、`navigation` 分层；存在业务依赖时由独立 Koin module 组装。
2. API 接口及 Ktorfit 生成代码留在对应业务 `data/remote`，通用客户端、错误模型和安全请求边界复用 `core/network`。
3. Entity 与 DAO 留在业务 `data/local`，再由 `shared` 的 `AppDatabase` 统一登记。
4. 每个 Feature 提供自己的可序列化 `NavKey`、导航 `SerializersModule` 和 EntryProvider；`shared` 只聚合，不直接构造 Feature 页面。
5. 只有被多个业务稳定复用、且不依赖具体业务模型的能力才能下沉到 `core`。

## 本地运行

- Android：使用 IDE 的 `androidApp` 运行配置，或执行 `./gradlew :androidApp:assembleDebug`。
- iOS：用 Xcode 打开 `iosApp/iosApp.xcodeproj`，选择已有签名 Team 后运行。

## 商用前仍需按业务补齐

- 替换 application id / bundle id、图标、品牌主题和占位 API 地址。
- 接入密钥管理、服务端认证刷新、证书策略和真实 API DTO。
- 接入崩溃上报、性能监控、埋点、用户协议、隐私政策与账号注销流程。
- 根据真实采集行为更新 `PrivacyInfo.xcprivacy`、Android 数据安全表单和商店隐私声明。
- 增加 CI、静态检查、单元/集成/UI 测试、依赖漏洞扫描和签名发布流水线。
- 按发布地区完成第三方许可证、税务、支付、无障碍和合规审查。

第三方组件及许可证摘要见 [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md)。
