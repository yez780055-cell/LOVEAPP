# LOVEAPP 三合一迁移计划

> 计划基线：LOVEAPP main `40af636d95d0e0e222c7eac888a33a43f64a062e`。本文件是实施顺序与验收门槛，不表示下列功能已经开发。

## 原则

- 先锁定现有纪念日数据库和行为，再引入新模块。
- Room 使用显式、可验证迁移；不使用破坏性回退。
- 每一阶段可独立构建、升级真实旧库和回退到可运行版本。
- 迁移 Web 记录依靠用户选中的备份文件；不尝试跨 origin 读浏览器 IndexedDB。
- Todo 的个人与情侣数据、Period 私密数据、Anniversary 数据分别由自己的仓储负责。
- 每阶段 CI 和设备验证通过后再开始下一阶段。

## Phase 0：冻结当前基线

**工作**

1. 记录 `main` SHA、Room v1 schema、Manifest 权限、路由、通知 ID/request code、Widget Intent 和现有数据字段。
2. 启用并提交 Room schema 导出，增加版本 1 数据库合成快照及升级测试。
3. 更新 README：删去“当前环境没有 Gradle/SDK/模拟器”的过期交付说法；记录最新 CI 已构建 debug APK。
4. 修复 emulator smoke workflow 的设备 API/镜像配置。现有 run `39119507439` 单元测试与 APK 构建成功，但 smoke 因 Unknown API Level / 无兼容设备未执行；在 runner 真正启动应用前不标记 smoke 通过。
5. 记录旧纪念日数据库的备份/恢复方案，验证可从文件还原到隔离测试目录。

**验收**

- debug 构建、单元测试、lint 通过。
- CI emulator 能启动指定 API 级别的设备并运行启动/导航 smoke。
- v1 fixture 的表、主键、提醒关联和回收站状态已核对。

## Phase 1：只迁移应用壳与导航

**工作**

1. 创建根 `LifeSpaceApp`、统一主题和根 NavHost。
2. 建立待办、小月历、纪念日三个 Tab 路由；新 Tab 初期可显示明确的阶段性空态，不伪装为已实现。
3. 将现有纪念日首页、编辑、详情、设置纳入 Anniversary 子图；沿用当前 ViewModel/DAO/实体。
4. 引入每 Tab 独立 back stack 的 save/restore；设置为当前 Tab 的子路由。
5. 把 Widget 的现有 detail extra 解析到 Anniversary 详情路由，并为后续显式 Deep Link 建立单一入口。
6. 验证系统返回顺序、进程重建、Tab 切换后列表筛选/滚动恢复；Activity 仍保持单实例。

**验收**

- 既有纪念日 CRUD 和提醒行为通过回归。
- Tab 来回切换不会重置未保存编辑之外的合理页面状态。
- 深链参数不含私密数据；无重复全局返回监听。

## Phase 2：建立数据库边界和 Repository 接口

**工作**

1. 新建应用级 Room Database，并将现有 Anniversary 实体/DAO 接入；仅做必要版本升级。
2. 对 Todo、Period、Anniversary 定义 feature-owned Repository 接口与数据模型；不能用一个通用 Event 模型覆盖不同语义。
3. 将共享通知调度平台 API 抽到 core；提醒配置仍由各 feature 仓储拥有。
4. 明确任务 recurrence/occurrence 的唯一来源、情侣域字段、Period 本地私密数据边界和媒体所有权规则。
5. 每个 schema 变更都有升级测试。任何表迁移失败都回滚，禁止破坏性降级清库。

**验收**

- Room v1 至新 schema 升级 fixture 保留纪念日主键和所有关系。
- Todo 与 Period 操作不能读写 Anniversary DAO，反向同样成立。
- 删除某域记录只删除该域关联和无引用附件。
- repository transaction 与通知重排失败有明确一致性策略。

## Phase 3：实现 Todo 域

**顺序**

1. 先移植日期/重复规则为纯 Kotlin 函数，覆盖 LocalDate、跨月年、闰日、周起始、时区变化边界。
2. 建立个人任务 CRUD、标签、完成状态、日期浏览、未完成筛选及回收站（若与 Web 规则一致）。
3. 建立情侣任务独立数据域与共享空间交互；明确当前“本地双人共用”与未来跨设备同步不是同一能力。未授权不加入账号或云端同步。
4. 接入子任务/重复任务等仅在功能对照确认需保留时实现；以 Web 当前实际代码为准，不能照旧文档猜测。
5. 为快速操作、撤销和重复实例编辑定义事务边界并补迁移回归测试。

**验收**

- 合成数据下个人与情侣任务隔离，筛选/搜索/日期和重复任务结果正确。
- 重复实例编辑、完成覆盖与撤销经过跨月年测试。
- Android Back、Tab 状态及软键盘输入流可用。

## Phase 4：实现 Period 私密域

**顺序**

1. 纯 Kotlin 移植日期区间、预测、统计与校验逻辑；预测只用真实记录，不生成演示样例。
2. 创建独立 `period_records`、`period_settings` 表和 repository；任何写入都不经 Todo/情侣表。
3. 按目标产品要求实现历史记录编辑、进行中区间、撤销、参数、关怀备忘和本地提醒。
4. 实现仅遮挡 Period 的 PIN；明确 PIN 不加密 Room 数据库，不将密码/明文放入日志。
5. 实现旧 .xyl 用户主动选择、输密码、完整校验、摘要预览和确认后单事务导入；损坏/冲突/取消不得写入。
6. 加密导出 Period 数据；恢复/覆盖前二次确认并提示先导出现有数据。

**验收**

- 正确密码导入后刷新仍存在；错误密码、畸形文件、未来日期、重复 ID、区间冲突和事务中断均零写入。
- Todo、情侣、纪念日、App 设置表在导入前后内容不变。
- PIN 解锁/锁定与系统返回、进程重建和通知预览隐私均验证。

## Phase 5：完善 Anniversary 和共享能力

**工作**

1. 补齐 Web 对照确认需要的纪念日图标库/自定义图标、日期重复、周年节点、首页期待周等能力。
2. 先复用现有日期引擎、Room IDs、通知排程；逐项记录新旧行为差异。
3. 为图片选择使用 SAF/Photo Picker，复制需要长期保留的文件到应用私有存储；校验大小、类型和删除引用。
4. 设计共享备份容器，按 feature 拆分清单及加密策略；导入仅原子应用被用户选中的数据域。
5. 更新 Widget 行为与配置能力，避免破坏当前 `open_event_id` 兼容。

**验收**

- 已有纪念日用户升级后仍能看见原记录与附件。
- 日期/周年节点/提醒语义均有合成测试。
- 文件导入失败不会生成半张记录或孤立附件。

## Phase 6：发布前验收与文档

- Android API 级别覆盖最低支持版本及当前目标版本；真机测试通知、重启、时间变化、照片权限、返回栈与软键盘。
- 对 320dp 到大屏、系统字体放大、深色主题和无障碍标签进行人工检查。
- 完整执行 lint、unit test、Room migration test、debug build、instrumented smoke；报告每个失败项及其环境原因。
- 更新 README、ARCHITECTURE、FEATURE_PARITY、用户迁入指南、隐私与备份说明。
- 不把“安装成功”写作“全功能通过”；未执行的真机步骤标记待验收。

## 风险与应对

| 风险 | 应对 |
|---|---|
| Room 一次性换库导致用户纪念日丢失 | 先冻结 v1 schema、提交升级 fixture；使用显式迁移，保留稳定 ID。 |
| 新导航覆盖原路由造成 Widget 失效 | 旧 Intent extra 转接兼容；记录路由迁移测试。 |
| 多域误写或删除 | feature-owned repository/DAO；逐域交易；导入前后对比其他表。 |
| 旧 Web 数据无法自动发现 | 提供用户选文件迁入说明；不声称跨 origin 自动同步。 |
| 通知权限或精确闹钟差异 | 权限拒绝和非精确回退均有 UI 状态与测试。 |
| CI smoke 假绿 | 当前设备未启动属于未验证；先修正 runner 并确认实际设备启动。 |
| 初期模块拆分过重 | 第一版保留单 Gradle module，以包边界和架构测试建立依赖纪律。 |
