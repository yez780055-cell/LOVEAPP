# Anniversary Native — Foundation + Days Matter V1

独立原生 Android 工程，仅实现纪念日模块基础版。未复用旧项目代码；当前不包含美柚和时光序业务。

## 技术

Kotlin、Jetpack Compose、Navigation Compose、Room、Coroutines/Flow、ViewModel、DataStore 依赖预留、AlarmManager、BroadcastReceiver、RemoteViews AppWidget、Photo Picker/SAF 后续接入。

## 构建

需要 JDK 17、Gradle（AGP 8.7.3）和 Android SDK Platform 35：

```sh
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```

APK 预期位于 `app/build/outputs/apk/debug/app-debug.apk`。当前生成工作区没有 Gradle/Android SDK/ADB/模拟器，且下载 Gradle 发行包超时，因此本交付为源码工程，未声称 APK 已生成。

## 当前可用范围

- 新建、编辑、查看、移入回收站
- 标题、日期、备注、正数/倒数、当天计入、年度重复
- Room 本地持久化
- 可选当天 09:00 提醒；允许精确闹钟时用精确调度，否则回退非精确；开机及日期/时间/时区变化后恢复
- 单事件 Widget 最小 provider：暂显示活动列表排序首项，点击进入详情
- 日期引擎测试源码覆盖同日、前后一天、跨月/年、闰年、重复和 DST 日期语义

高级功能和已知边界见 `docs/ARCHITECTURE.md`、`docs/REVERSE_GAPS.md`。
