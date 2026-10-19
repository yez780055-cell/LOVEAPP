# Web Life Space 与 Native LOVEAPP 功能对照

> 核对基线：Web Life Space `main` `636574116ef24022bfc985b16059a6b3f73b1bc5`；Native 代码已推进至三 feature 架构分支 `refactor/native-three-in-one`。Web 与原生存储不同，数据不会自动共享。

状态含义：**已有** = Native 当前代码可用；**部分** = 有基础实现但行为/范围有限；**未实现** = 当前 Native 不包含；**需核对** = 参考侧当前代码或规则需在功能迁移前逐项确认。

| 功能 | Web Life Space | Native LOVEAPP | 状态 | 备注 |
|---|---|---|---|---|
| 应用壳 | Todo、Period、Anniversary 共用底部导航 | 单 Activity，Compose NavHost 与三 Tab | 部分 | 导航壳已实现；Todo/Period 业务仍是迁移骨架。 |
| Tab 独立状态 | 由应用壳状态和 History 导航维护 | nested graph 使用 saveState/restoreState | 部分 | 导航状态保存已配置；真实设备交互 smoke 尚未运行通过。 |
| Android 统一返回 | Web/Capacitor 共用导航 hook | Compose Navigation / 系统返回 | 部分 | Tab 子图、设置来源返回、周年详情/编辑返回由统一 NavController 管理；需设备 smoke 验收。 |
| 个人任务 | 个人 Todo 模块、按日期/未完成操作 | Todo feature route、ViewModel、Repository 接口、Room Entity/DAO 骨架 | 部分 | 当前仍显示迁移占位，不包含个人待办产品流程。 |
| 个人任务标签 | Todo 自身标签与筛选 | 无 Todo | 未实现 | 独立于纪念日分类。 |
| Todo 重复任务 | recurrence 与 occurrence 模型 | 无 Todo | 未实现 | 需保留实例覆盖、完成状态及撤销语义。 |
| 情侣任务 | couple 数据仓储和独立标签域 | 无 Couple 业务域 | 未实现 | 本架构阶段只建立 Todo/Period 骨架，尚未迁移情侣待办。 |
| 情侣跨设备同步 | 当前 Web 有情侣空间交互，不能等同完整服务端同步 | 无账号或同步 | 未实现/需核对 | Native 首期保持本地优先；同步需另行决策。 |
| Period 月历 | 实际记录与预测分色、按月浏览、日期详情 | Period feature route、ViewModel、Repository 接口、Room Entity/DAO 骨架 | 部分 | 当前仍是迁移占位，尚未实现月历业务流程。 |
| Period Toggle 与历史编辑 | 进行中记录、撤销、历史补录和冲突校验 | 无 Period | 未实现 | 需移植并测试边界，不能以 UI 校验代替仓储校验。 |
| Period 预测与统计 | 最近周期算法、低样本提示、只用真实记录 | 无 Period | 未实现 | 合成测试不包含用户实际敏感记录。 |
| Period PIN | 仅遮挡 Period 私密内容 | 无 Period | 未实现 | PIN 不应描述为设备级加密。 |
| Period 加密备份 | Web 支持加密 .xyl 导入/导出 | 无导入导出 | 未实现 | 需用户主动选文件；不自动访问旧站 IndexedDB。 |
| 纪念日 CRUD | 新建、编辑、查看、回收站 | 已有 | 已有 | 当前 Native 可在 Room 保存和查看纪念日。 |
| 纪念日分类 | 分类与事件关联 | Room CategoryEntity | 已有 | 当前仍属于独立周年模块。 |
| 纪念日正数/倒数 | 日期计数与当天计入选项 | DateEngine 支持基础计数 | 部分 | 以当前领域测试为准，迁移时固定语义。 |
| 年度重复 | 周年重复日期逻辑 | DateEngine 有基础年度重复 | 部分 | 需补充闰年/2月29日产品规则对照。 |
| 纪念日提醒 | Web 有模块提醒设置 | Anniversary 计算业务触发时刻；core 只接收通用 source 与绝对时间调度 | 部分 | 旧提醒通过显式迁移保留 sourceType/sourceId、时间参数和 delivery action；仍需设备验收。 |
| 开机/时区恢复提醒 | Web/PWA 限制后台提醒可靠性 | Core 按已持久化触发时间恢复提醒 | 部分 | core 不解释周年日规则；仍需 Android 设备验收。 |
| 首页期待周 | Web Anniversary 首页突出近期事件 | Native 首页事件列表 | 未实现 | 目前没有对应周视图汇总。 |
| 自定义图标 | Web 支持自定义/内置图标 | 无等价完整功能 | 未实现 | Native 需提供图标选择、校验和备份策略。 |
| 自定义背景图片 | Web Anniversary 可管理背景 | BackgroundImageEntity 已有基础数据模型 | 部分 | UI/文件持久化完整体验需核验；需迁移文件路径策略。 |
| 周年天数节点 | Web 可配置并展示自定义里程碑 | MilestoneEntity 已存在 | 部分 | 数据实体有基础；自定义编辑和首页展示仍需核验。 |
| 回收站 | Web Anniversary 支持删除恢复 | 当前可移入回收站 | 部分 | 确认恢复、永久删除和提醒清理边界。 |
| Widget | Web 无 Android 主屏 Widget | Anniversary 专用 RemoteViews，代码位于 `feature/anniversary/widget` | 部分 | 当前展示排序首项；点击使用兼容 Intent extra 进入周年详情。 |
| 系统设置 | Web 有模块与应用设置 | settings 路由为原型 | 部分 | DataStore 依赖预留，不能视为设置持久化已完成。 |
| 离线/本地存储 | IndexedDB 与 PWA 缓存 | Room 本地库 | 部分 | 两端数据不共享；备份文件是迁移桥梁。 |
| 全量备份 | Web 按域本地存储，Period 加密备份 | 文档中有 ZIP 提案，无已实现流程 | 未实现 | 需要确认每域数据清单、原子恢复和附件规则。 |
| 本地通知限制 | 浏览器后台能力受操作系统限制 | Android AlarmManager/Notification | 部分 | 权限与厂商省电机制会影响准时性。 |
| 账号/云同步 | 当前仓库代码不代表已有完整服务端同步 | 无 | 未实现 | 不在本阶段架构蓝图中启用。 |

## 核对注意

- Web README 仍有旧文字提到个人 Todo 子任务；当前实现与近期功能决策可能已经变化，迁移前应以源代码、测试和已接受的 PR 为准，不以过时 README 单独作为契约。
- Web 的 `life-space` IndexedDB 与 Android Room 是不同存储；不能在没有用户导出/选择的情况下自动导入。
- LOVEAPP `docs/BACKUP_FORMAT.md` 描述 ZIP/JSON 备份提案，并非已实现格式。
- 架构分支 CI 的 JVM tests（含 Robolectric Room v1→v3 migration）和 debug build 可独立验收；Android emulator smoke 由于托管 runner 的 ADB 离线/Instrumentation crash 标为实验性，不能据此声称交互通过。
