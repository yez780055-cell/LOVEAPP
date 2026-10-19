# Web Life Space 与 Native LOVEAPP 功能对照

> 核对基线：LOVEAPP `main` `40af636d95d0e0e222c7eac888a33a43f64a062e`；Life Space Web `main` `636574116ef24022bfc985b16059a6b3f73b1bc5`。该文档描述核对到的能力，不代表 Native 已实现。Web 与原生存储不同，数据不会自动共享。

状态含义：**已有** = Native 当前代码可用；**部分** = 有基础实现但行为/范围有限；**未实现** = 当前 Native 不包含；**需核对** = 参考侧当前代码或规则需在功能迁移前逐项确认。

| 功能 | Web Life Space | Native LOVEAPP | 状态 | 备注 |
|---|---|---|---|---|
| 应用壳 | Todo、Period、Anniversary 共用底部导航 | 单 Activity，Compose NavHost | 部分 | 原生当前是 Anniversary 独立入口，没有三 Tab。 |
| Tab 独立状态 | 由应用壳状态和 History 导航维护 | 单一现有路由栈 | 未实现 | 目标采用每 Tab 可保存/恢复的子图状态。 |
| Android 统一返回 | Web/Capacitor 共用导航 hook | Compose Navigation / 系统返回 | 部分 | 现有路由可返回；Tab 子栈和模块子页统一策略未建立。 |
| 个人任务 | 个人 Todo 模块、按日期/未完成操作 | 无 Todo | 未实现 | 待办需重新实现 Kotlin 领域与仓储。 |
| 个人任务标签 | Todo 自身标签与筛选 | 无 Todo | 未实现 | 独立于纪念日分类。 |
| Todo 重复任务 | recurrence 与 occurrence 模型 | 无 Todo | 未实现 | 需保留实例覆盖、完成状态及撤销语义。 |
| 情侣任务 | couple 数据仓储和独立标签域 | 无 Todo/Couple | 未实现 | 同一 Todo Tab 中仍需隔离个人和情侣数据。 |
| 情侣跨设备同步 | 当前 Web 有情侣空间交互，不能等同完整服务端同步 | 无账号或同步 | 未实现/需核对 | Native 首期保持本地优先；同步需另行决策。 |
| Period 月历 | 实际记录与预测分色、按月浏览、日期详情 | 无 Period | 未实现 | 日期逻辑迁移到 LocalDate 并保持本地日期语义。 |
| Period Toggle 与历史编辑 | 进行中记录、撤销、历史补录和冲突校验 | 无 Period | 未实现 | 需移植并测试边界，不能以 UI 校验代替仓储校验。 |
| Period 预测与统计 | 最近周期算法、低样本提示、只用真实记录 | 无 Period | 未实现 | 合成测试不包含用户实际敏感记录。 |
| Period PIN | 仅遮挡 Period 私密内容 | 无 Period | 未实现 | PIN 不应描述为设备级加密。 |
| Period 加密备份 | Web 支持加密 .xyl 导入/导出 | 无导入导出 | 未实现 | 需用户主动选文件；不自动访问旧站 IndexedDB。 |
| 纪念日 CRUD | 新建、编辑、查看、回收站 | 已有 | 已有 | 当前 Native 可在 Room 保存和查看纪念日。 |
| 纪念日分类 | 分类与事件关联 | Room CategoryEntity | 已有 | 当前仍属于独立周年模块。 |
| 纪念日正数/倒数 | 日期计数与当天计入选项 | DateEngine 支持基础计数 | 部分 | 以当前领域测试为准，迁移时固定语义。 |
| 年度重复 | 周年重复日期逻辑 | DateEngine 有基础年度重复 | 部分 | 需补充闰年/2月29日产品规则对照。 |
| 纪念日提醒 | Web 有模块提醒设置 | AlarmManager、通知 Receiver | 部分 | 当前单事件当天 09:00 提醒为主；权限拒绝时回退非精确。 |
| 开机/时区恢复提醒 | Web/PWA 限制后台提醒可靠性 | Receiver 重排 | 已有 | 仍需真机覆盖 Android 系统限制和权限状态。 |
| 首页期待周 | Web Anniversary 首页突出近期事件 | Native 首页事件列表 | 未实现 | 目前没有对应周视图汇总。 |
| 自定义图标 | Web 支持自定义/内置图标 | 无等价完整功能 | 未实现 | Native 需提供图标选择、校验和备份策略。 |
| 自定义背景图片 | Web Anniversary 可管理背景 | BackgroundImageEntity 已有基础数据模型 | 部分 | UI/文件持久化完整体验需核验；需迁移文件路径策略。 |
| 周年天数节点 | Web 可配置并展示自定义里程碑 | MilestoneEntity 已存在 | 部分 | 数据实体有基础；自定义编辑和首页展示仍需核验。 |
| 回收站 | Web Anniversary 支持删除恢复 | 当前可移入回收站 | 部分 | 确认恢复、永久删除和提醒清理边界。 |
| Widget | Web 无 Android 主屏 Widget | 有单事件 RemoteViews | 部分 | 当前展示排序首项；后续完善配置、刷新和导航。 |
| 系统设置 | Web 有模块与应用设置 | settings 路由为原型 | 部分 | DataStore 依赖预留，不能视为设置持久化已完成。 |
| 离线/本地存储 | IndexedDB 与 PWA 缓存 | Room 本地库 | 部分 | 两端数据不共享；备份文件是迁移桥梁。 |
| 全量备份 | Web 按域本地存储，Period 加密备份 | 文档中有 ZIP 提案，无已实现流程 | 未实现 | 需要确认每域数据清单、原子恢复和附件规则。 |
| 本地通知限制 | 浏览器后台能力受操作系统限制 | Android AlarmManager/Notification | 部分 | 权限与厂商省电机制会影响准时性。 |
| 账号/云同步 | 当前仓库代码不代表已有完整服务端同步 | 无 | 未实现 | 不在本阶段架构蓝图中启用。 |

## 核对注意

- Web README 仍有旧文字提到个人 Todo 子任务；当前实现与近期功能决策可能已经变化，迁移前应以源代码、测试和已接受的 PR 为准，不以过时 README 单独作为契约。
- Web 的 `life-space` IndexedDB 与 Android Room 是不同存储；不能在没有用户导出/选择的情况下自动导入。
- LOVEAPP `docs/BACKUP_FORMAT.md` 描述 ZIP/JSON 备份提案，并非已实现格式。
- 当前 LOVEAPP CI 已通过单元测试与 debug 构建；最近一次 emulator smoke 因兼容设备未启动而无法验收。
