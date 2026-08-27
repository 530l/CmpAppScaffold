# CmpAppScaffold

一个面向 Android 与 iOS 的 Compose Multiplatform / Kotlin Multiplatform 商业项目脚手架。应用壳提供首页、逛、消息、购物车、我的五个独立返回栈，登录等全局流程由根导航全屏覆盖。购物车示例接入了 wanandroid 文章分页接口（`article/list/{page}/json`），演示下拉刷新、触底加载、勾选与结算底栏的完整链路；勾选状态与演示金额只存内存，远端数据不落库。

## 模块边界

```text
androidApp / iosApp        平台壳、权限、签名和发布配置
          ↓
share                      共用应用入口、根导航、五 Tab 壳、初始化、DI/数据库与导航聚合
  ├── core:common          纯 Kotlin 底座：日志（AppLogger）、运行配置（AppConfig）
  ├── core:data            通用模型（Money/NetworkResult）、Ktor/Ktorfit 工厂、
  │                        KeyValueStore 接口与 MMKV 实现、核心 DI 模块
  ├── core:design          Compose 工具箱：主题、图片、刷新/加载更多组件族、
  │                        状态页与 Navigation 3 容器（AppNavHost/TabNavHost）
  ├── feature:home         「首页」独立 tab 与 EntryProvider
  ├── feature:browse       「逛」独立 tab 与 EntryProvider
  ├── feature:message      「消息」独立 tab 与 EntryProvider
  ├── feature:cart         「购物车」data / domain / presentation / EntryProvider
  ├── feature:mine         「我的」独立 tab 与 EntryProvider
  └── feature:login        根级全屏登录骨架、路由与 EntryProvider
```

- `core:common` 零 Compose 依赖；`core:data / core:design` 只依赖 `core:common`，core 禁止依赖 `share` 或任何 `feature` 包。
- `share` 是 Android/iOS 共用的应用组合根，只把宿主配置所需的 `core:common` 暴露为 API；数据层和 Compose 设计实现不进入 iOS framework 公共接口，宿主仍只依赖 `share`。
- `feature:*` 是独立 KMP Gradle 模块，只按实际需要依赖 core 模块，禁止 Feature 之间直接依赖。
- `AppDatabase` 留在 `share` 的应用级 `com.lyf.cmp.database` 包，由它聚合各业务 Entity，避免出现 `core → feature` 反向依赖。
- `domain` 只放纯 Kotlin 模型和规则，不引用 Compose、Room、Ktor。
- `data` 负责 DAO、DTO、映射和 Repository 实现。
- `presentation` 使用不可变 `UiState` 和 `Intent`；Composable 子组件只接收状态与回调。
- 平台差异集中在 `androidMain` / `iosMain`，共享业务代码不接收 `Any?` 平台对象。
- 每个 Feature 通过自己的 `EntryProviderScope<NavKey>` 扩展注册页面，跨 Feature 跳转由 `share` 连接回调。

## 已接入的基础能力

- Koin：核心能力、平台数据库和 feature 依赖注入。
- Ktor：平台引擎、JSON 容错、统一超时、HTTPS 基地址、可脱敏请求日志、`NetworkResult` 错误边界。
- Ktorfit：Retrofit 风格的声明式 API；使用 `ktorfit-lib-light`，复用并保留项目自己的 Ktor 客户端配置。
- Room + bundled SQLite：跨平台持久化、响应式查询、schema 导出目录。
- Coil：应用组合根把共享 Ktor `HttpClient` 显式交给图片加载器，设计层不感知 Koin，并提供统一 `AppImage` 入口。
- Kermit：统一日志门面，业务代码不绑定具体日志库。
- Compose Resources / Material 3：Feature 自有文案、亮色与深色主题、加载/错误/空状态。
- 刷新与加载更多：基于 Material3 官方 `PullToRefreshBox`（Android 保持官方交互、iOS 使用 `48.dp` 阈值、快速复位、内容跟手位移与末端橡胶带回弹，仅关闭与下拉手势竞争的顶端回弹）+ `core/ui/loadmore` 分页组件族（`LoadableLazyColumn` 容器、`LoadableController` 状态机、等高 `LoadMoreFooter`、单次 `LoadMoreTrigger`），默认距底部 5 项预加载，互斥去重与结束判定带 JVM/iOS 双端单测。
- Navigation 3：根级全屏流程 + 五个互相隔离的 tab back stack，可序列化跨平台状态、Entry 级状态保存及 ViewModel 生命周期；重复点击当前 tab 发出 reselect 事件供页面滚动到顶部或刷新。Android 返回键从其他 tab 根页面先回首页，在首页首次返回提示、2 秒内再次返回退出应用。
- Edge-to-edge：Android 与 iOS 容器均铺满系统窗口，页面背景绘制到系统栏后方，交互内容通过 Compose `WindowInsets` 避让状态栏、刘海、手势区与键盘。
- 发布基础：Android release 混淆与资源压缩、禁止明文流量和系统备份；iOS 已放置 Privacy Manifest。

业务 API 统一使用 Ktorfit 声明，并在 `RemoteDataSource` 中处理响应与异常：

```kotlin
interface ProductApi {
    @GET("v1/products")
    suspend fun getProducts(): Response<List<ProductDto>>
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

分页列表统一走 `core/ui/loadmore`：ViewModel 的 UiState 实现 `LoadableUiState` 嵌入刷新/分页状态，`LoadableController` 负责页码、互斥去重与结束判定（`Page(items, hasMore)` 由调用方按后端 cursor/总数信号显式给出，不要用「返回条数 < pageSize」推断）：

```kotlin
data class ProductsUiState(
    val keyword: String = "",
    override val dataList: List<Product> = emptyList(),
    override val isRefreshing: Boolean = false,
    override val isInitializing: Boolean = true,
    override val loadMoreState: LoadMoreState = LoadMoreState.Idle,
) : LoadableUiState<Product, ProductsUiState> {
    override fun copyState(...) = copy(...)
}

// ViewModel：Intent 分发到 controller，onIntent 仍是唯一入口
private val loadable = LoadableController(
    scope = viewModelScope,
    initialUiState = ProductsUiState(),
    loadPage = { page -> repository.loadPage(page) }, // suspend (Int) -> Result<Page<Product>>
    onError = { error, isListEmpty -> /* 整页错误 or 非阻断提示 */ },
)

// Composable：容器自动处理下拉刷新、footer 追加与触底检测
LoadableLazyColumn(
    isRefreshing = uiState.isRefreshing,
    loadMoreState = uiState.loadMoreState,
    onRefresh = { onIntent(ProductsIntent.Refresh) },
    onLoadMore = { onIntent(ProductsIntent.LoadMore) },
) {
    items(uiState.dataList, key = Product::id) { ProductRow(it) }
}
```

本地全量列表（Room 响应式 Flow）不需要分页组件，直接用 `PullToRefreshBox` 包住列表，刷新走「远端整单拉取写库 → Flow 自动回流」。

## 环境配置

`AppConfig` 默认指向公开演示服务 `https://www.wanandroid.com/`（仅供脚手架演示）。实际项目应在 Android product flavor / Gradle BuildConfig 与 iOS xcconfig 中分别提供环境值，再在调用 `PlatformAndroid.initSharedApp` / `PlatformIos.initSharedApp` 时注入：

```kotlin
PlatformAndroid.initSharedApp(
    context = this,
    config = AppConfig(
        environment = AppEnvironment.PRODUCTION,
        apiBaseUrl = "https://api.your-company.com/",
        enableNetworkLogging = false,
    ),
)
```

不要把 API token、证书密码、签名私钥或真实生产密钥提交到仓库。iOS 首次运行时把
`iosApp/Configuration/Local.xcconfig.example` 复制为 `Local.xcconfig` 并填写 `TEAM_ID`；
该文件已被忽略，个人签名不会污染仓库，CI 可用自己的 xcconfig 覆盖同名字段。

Android release 支持通过用户级 `~/.gradle/gradle.properties` 注入签名；四项必须同时提供，否则配置阶段会明确失败：

```properties
CMP_RELEASE_STORE_FILE=/absolute/path/to/release.keystore
CMP_RELEASE_STORE_PASSWORD=replace_me
CMP_RELEASE_KEY_ALIAS=replace_me
CMP_RELEASE_KEY_PASSWORD=replace_me
```

## 数据库升级约定

1. 修改 Entity 后递增 `AppDatabase.version`。
2. 提交 `share/schemas` 生成的新 schema JSON。
3. 提供显式 migration 并覆盖升级测试；生产环境禁止使用 destructive migration。

当前购物车演示已改远端分页，无本地业务表；Room 不允许空实体列表，v2 起 `AppDatabase` 以 `SchemaPlaceholderEntity` 占位保持管线可用（v1 的 `cart_items` 表由 `MIGRATION_1_2` 显式 DROP）。接入首个业务 Entity 时删除占位表：递增版本号 + 迁移中 DROP `schema_placeholder` + 提交新 schema JSON，三件事一起做。

Room 与 MMKV 是脚手架预置能力，不为当前演示业务继续增加仓储抽象。Room 数据库通过 Koin `single` 在首次注入时创建；MMKV 实例同样按首次注入创建，但平台初始化必须在应用启动主线程完成，以保证后续可能发生在任意线程的首次读取安全。正式项目确定不需要其中一项时，应连同依赖、初始化和占位结构完整删除，不保留半接入状态。

## 导航与生命周期升级回归

`core:design` 对外只暴露 `TabAppNavHost` 与刷新/分页容器；iOS 下拉手势细节和 Tab 的 `ViewModelStore` 保活实现都留在模块内部，不向业务 Feature 泄漏。它们用于解决 iOS 手势冲突、Tab 状态丢失和返回后重建等已验证问题，暂不再增加策略接口或第二套导航抽象。

升级 Navigation 3、Lifecycle 或 Compose 时不要只看编译通过，至少人工回归以下链路：

1. 购物车勾选商品后切到“我的”，再切回购物车，勾选和滚动位置仍保留。
2. 购物车进入登录页再返回，购物车状态不刷新、不重建。
3. 每个 Tab 的二级页历史互相隔离，Android 从其他 Tab 根页面返回时直接切回首页且无页面转场。
4. iOS 侧滑返回完成和取消时页面、底栏、遮罩均连续，不露出错误 Tab。
5. 应用进入后台再恢复，以及系统可恢复状态重建后，当前路由和可保存页面状态正确。

依赖版本以稳定版为默认选择，不为追新升级到 alpha/beta；若上游稳定版改变 `ViewModelStore` 或 entry 装饰器语义，应先在 `core:design` 适配并完成上述回归，再向业务模块放开升级。

## iOS 隐私清单

当前 `PrivacyInfo.xcprivacy` 只声明演示应用自身不跟踪用户、未登记数据采集类别；没有实际使用证据时，不预填 Required Reason API 理由。新增分析、广告、崩溃上报、存储或其他第三方 SDK 后，必须按真实代码和数据流更新清单，不能沿用脚手架默认值。

每次正式上架前使用最终 Release Archive 生成并检查 Xcode Privacy Report，核对应用与所有内嵌 framework 的隐私清单、Required Reason API 和签名情况；再同步更新 App Store Connect 隐私标签。发现报告缺项后，应填写 Apple 允许且与实际用途一致的 reason code，禁止为通过校验随意选择理由。

## 新增业务功能约定

当前业务按独立的 `feature/<feature-name>` KMP Gradle 模块组织：

1. 在 Feature 内建立 `data`、`domain`、`presentation`、`navigation` 分层；存在业务依赖时由独立 Koin module 组装。
2. API 接口及 Ktorfit 生成代码留在对应业务 `data/remote`，通用客户端、错误模型和安全请求边界复用 `core/network`。
3. Entity 与 DAO 留在业务 `data/local`，再由 `share` 的 `AppDatabase` 统一登记。
4. 每个 Feature 提供自己的可序列化 `NavKey`、导航 `SerializersModule` 和 EntryProvider；`share` 只聚合，不直接构造 Feature 页面。
5. 只有被多个业务稳定复用、且不依赖具体业务模型的能力才能下沉到 `core`。

## 本地运行

- Android：使用 IDE 的 `androidApp` 运行配置，或执行 `./gradlew :androidApp:assembleDebug`。
- iOS：用 Xcode 打开 `iosApp/iosApp.xcodeproj`，选择已有签名 Team 后运行。

## 商用前仍需按业务补齐

- 替换 application id / bundle id、图标、品牌主题和演示 API 地址（wanandroid）。
- 接入密钥管理、服务端认证刷新、证书策略和真实 API DTO。
- 接入崩溃上报、性能监控、埋点、用户协议、隐私政策与账号注销流程。
- 根据真实采集行为更新 `PrivacyInfo.xcprivacy`、Android 数据安全表单和商店隐私声明。
- 增加 CI、静态检查、单元/集成/UI 测试、依赖漏洞扫描和签名发布流水线。
- 按发布地区完成第三方许可证、税务、支付、无障碍和合规审查。

第三方组件及许可证摘要见 [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md)。
