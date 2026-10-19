# LOVEAPP 原生三合一架构蓝图

> 文档基线：LOVEAPP `main`，`40af636d95d0e0e222c7eac888a33a43f64a062e`（2026-10-18）。本文把“现状”与“目标”分开；目标是后续实施方案，不代表代码已具备这些能力。

## 1. 当前工程事实

LOVEAPP 当前是独立 Android 纪念日原生工程，不是 Life Space Web 的 Android 壳。主要技术为 Kotlin、Jetpack Compose、Navigation Compose、Room、Coroutines/Flow、ViewModel、AlarmManager、BroadcastReceiver 和 RemoteViews AppWidget。应用 ID/namespace 为 `dev.yezi.anniversary`。

| 能力 | 当前实现 |
|---|---|
| 入口与导航 | 单 Activity（`MainActivity.kt`），Compose NavHost 路由为 `home`、`edit/{id}`、`detail/{id}`、`settings`；设置是路由，不是底部 Tab。主要屏幕声明仍集中在 Activity 文件附近。没有声明外部 Deep Link。 |
| 数据 | Room 数据库 `anniversary.db`，版本 1；实体为 Category、Event、Reminder、BackgroundImage、Milestone。当前 DAO 与数据层属于纪念日原型。 |
| 业务 | `DateEngine` 负责日期计数；`EventRepository` 承担事件保存、提醒重排及 Widget 更新等协调；`EventViewModel` 暴露活动事件/分类流和 CRUD。暂未建立全应用的 feature/domain/use case 分层。 |
| 提醒 | AlarmManager；精确闹钟不可用时回退非精确调度。Receiver 处理通知、开机及日期/时间/时区变化。提醒保存和业务数据更新尚未统一为一个事务。 |
| Widget | 最小单事件 Widget：使用活动列表排序首项展示，点击通过 Intent extra 打开详情。 |
| 配置与备份 | DataStore 依赖已声明，但当前没有被用作完整设置实现。备份格式文档是提案；当前没有已实现的导入/导出链路。 |
| 产品范围 | 尚无 Todo 或 Period；当前 README 描述的是独立 Anniversary Native 原型。 |

源码入口索引：`app/src/main/java/dev/yezi/anniversary/MainActivity.kt`、`AnniversaryApp.kt`、`data/`、`domain/DateEngine.kt`、`notification/`、`ui/EventViewModel.kt`、`widget/`、`app/src/main/AndroidManifest.xml`、`app/build.gradle.kts`、`docs/BACKUP_FORMAT.md`。

仓库 CI 的最近基线 `39119507439` 对应上述 main：单元测试与 debug APK 构建通过；Android emulator smoke 因 runner 跳过 `emulator-5554`（报告 Unknown API Level / 无兼容设备）未能执行。该结果说明构建成功，但不构成模拟器交互验收。

## 2. 目标产品边界

目标是一个原生 Android 三合一应用，共用应用壳和基础设施，同时让 Todo、Period、Anniversary 各自拥有明确的数据域和业务规则。旧 Web Life Space 是需求与行为参考，不能假定新 App 能读取其浏览器存储；跨平台迁移只能走用户主动选择的备份文件。

- 底部主导航：待办、小月历、纪念日。全局设置从模块入口进入，保留来源路由并由统一导航返回。
- Todo 内明确区分个人待办与情侣待办数据域。情侣数据不会因为同处一个模块而与个人清单混表。
- Period 私密记录只由 Period 仓储读写；PIN 是应用内隐私遮挡，不等同于设备级加密或数据库加密。周期备份必须加密。
- Anniversary 延续当前 Room 业务数据，逐步迁入全局壳。其现有提醒、日期引擎和 Widget 在迁移前保持可用。
- 首期本地优先，不引入账号、服务器或云同步。

## 3. 建议的代码分层与目录

首轮继续使用单 Gradle 应用模块，通过包边界降低迁移风险；只有在边界稳定、依赖可验证后再考虑拆 Gradle 子模块。

```text
dev.yezi.lifespace
├── app/                 # Application、MainActivity、根 NavHost、全局主题
├── core/
│   ├── database/        # Room Database、迁移、共享 DAO 基础设施
│   ├── navigation/      # 路由、Tab 状态、Android Back、Intent 路由
│   ├── date/             # 无 UI 的本地日期工具
│   ├── notification/     # Android 通知权限与调度适配
│   ├── media/             # SAF/Photo Picker 与应用私有媒体文件管理
│   ├── backup/            # 文件封装、校验、加密接口
│   └── settings/          # 非业务设置存取
└── feature/
    ├── todo/              # personal 与 couple 子域、UI、domain、data
    ├── period/            # 私密周期域、导入导出、设置、UI
    └── anniversary/       # 当前纪念日功能迁入后按层拆分
```

依赖方向：Compose UI → ViewModel → Use Case / domain → Repository 接口；Room、AlarmManager、文件系统等实现放在 data/core 适配层。领域层不依赖 Android View 或具体 DAO。跨 feature 不直接访问对方 DAO；需要共用的能力由 core 接口提供，具体业务仍留在所属 feature。

目前包名是 `dev.yezi.anniversary`。未来包迁移应与应用身份升级分开评审：早期先保留 applicationId 以允许覆盖安装和保留 Android 系统数据，再在明确升级策略后搬迁 Kotlin 包。不要在引入三模块时顺手改变 applicationId、签名或安装身份。

## 4. 根导航与返回

建议单 Activity + Compose Navigation 根图，三个 Tab 对应三个嵌套导航图。切换 Tab 时使用 Navigation Compose 的保存/恢复状态能力，分别保留各 Tab 的返回栈和滚动/页面状态；返回键先消费当前子图的弹层/详情/编辑层，再回到该 Tab 首页，最后才退出应用。设置作为从当前模块发起的子路由，返回后回到原模块与页面。

- 路由只携带稳定 ID、月份等非敏感参数；详情由仓储按 ID 加载。
- Widget 点击改用显式内部深链路由到 `anniversary/detail/{id}`，保留旧 Intent extra 的兼容入口一段迁移期。
- 所有外部 Intent 先校验目标 ID 和路由参数，不将私密周期字段放在 URI、日志或通知文本里。
- 统一处理通知、Widget 与 Android Back，不让模块注册竞争性的全局返回监听。
- 配置变更/进程重建后恢复当前 Tab 和子路由；加入 Tab 切换、编辑返回、Widget 进入和系统 Back 的导航测试。

## 5. Room 数据域与兼容策略

### 数据域划分

使用一个应用级 `LifeSpaceDatabase`，数据库文件可在迁移评估后定名；数据库共享不表示业务表合并。每个 feature 拥有自己的实体、DAO、仓储与 schema 迁移。

| 域 | 建议表及约束 |
|---|---|
| Todo | `todo_tasks`、`todo_occurrences`、`todo_tags`、`todo_task_tags`；个人与情侣任务有明确 owner/scope 字段或独立关联域。周期任务实例与单次覆盖状态要能区分。 |
| Period | `period_records`、`period_settings`、必要的关怀备忘/提醒配置。只允许 Period feature 访问。导入、替换和恢复只触及 Period 表。 |
| Anniversary | 先沿用现有实体和表的物理名称/主键，避免首轮搬壳时全量重写；将现存 Event、Category、Reminder、BackgroundImage、Milestone 归属清楚。后续重命名必须用显式迁移。 |
| Shared infrastructure | 通知调度请求、应用级非敏感偏好、媒体索引可由 core 提供；每条资源需带 ownerType/ownerId 或 feature 专属键，并由仓储维护引用完整性。 |

不要创建一个混合 Todo、Period、Anniversary 的万能业务表。Period 的 PIN/私密状态不写入情侣域。通用通知调度器只负责平台调度；提醒的业务配置由各 feature 仓储拥有。媒体文件存放在应用私有目录，数据库仅记录 URI/相对路径和所有者，删除需确认无其他引用。

### Room 升级

当前 schema 为版本 1。引入三合一后必须按版本递增并提交 schema 导出文件。对安全的加列可用 AutoMigration；表拆分、主键映射、枚举重编码使用显式 `Migration` 和事务。禁止 `fallbackToDestructiveMigration`。

- 保留现有纪念日记录 ID、回收站状态、分类、背景、里程碑与提醒关系。
- 在真实升级测试中从版本 1 数据库快照迁到新版本，核对数据条数、字段、关联和提醒重建。
- 升级失败不得删除旧数据库；恢复路径需有明确错误与备份指引。
- 任何导入先完成文件校验，再以单事务应用；失败回滚且其他 feature 表不变。
- 数据库 schema 版本、备份格式版本和应用版本分别管理，不相互冒充。

## 6. 日期、提醒、备份和隐私共用能力

- 日期域统一使用 `java.time.LocalDate` 表达用户日历日期，时刻用带时区的明确类型；周期算法不能用 UTC 毫秒差换算天数。
- 通知平台适配统一处理通知权限、精确闹钟权限及不支持时的非精确回退；feature 提供业务触发时间和有效期。开机、时区、系统时间变化后由调度层恢复，用户能关闭各类提醒。
- 备份容器有版本、清单、校验和、媒体路径限制。Period 继续使用加密备份；全量备份需定义各域是否纳入及每域策略，绝不静默输出明文周期数据。
- 旧 .xyl、Web Anniversary/Todo 数据迁入需显式选择文件、摘要预览、确认和事务写入。旧站 origin 与原生 App 沙箱不同，App 不直接探测或删除浏览器数据。
- DataStore 当前只是预留依赖。建议仅在需求明确后用于非关键界面偏好；需要参与事务备份或数据恢复的业务设置放在 Room，避免同一设置出现两份真相。

## 7. 已知差异与设计决定

1. LOVEAPP 是当前纪念日原生原型，不是已完成的三模块壳；架构文档中的目标结构尚未实现。
2. Web Life Space 的 IndexedDB、Capacitor History 与 Kotlin/Room 不共享运行时和数据文件；行为需要按领域规则移植，不能复制 Web 层实现。
3. Native 当前没有真正的个人/情侣 Todo、Period、统一底部导航、Tab 状态保存或全量备份恢复。
4. 现有 Widget 只显示排序首项；从 Intent extra 转为路由深链时需兼容旧入口。
5. 当前 CI smoke 因设备配置未启动；修复 runner 配置并获得真实 smoke 结果后再把交互验证作为通过项。
6. 从单模块开始，以 package 约束与依赖测试稳住架构；避免初期多模块带来的 Gradle 迁移、应用身份变化与数据迁移风险。
