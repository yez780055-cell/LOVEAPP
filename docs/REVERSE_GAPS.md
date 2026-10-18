# Reverse-engineering gaps

| APK / feature | Evidence already available | Unknown behavior | Blocks current foundation? | Next targeted evidence |
|---|---|---|---|---|
| Days Matter — repeat rules | DEX call sites pass repeat type, interval and lunar flag to a shared date helper. | Full repeat enum mapping, lunar leap-month policy and edge behavior. | No; annual solar repeat is implemented independently. | Target the helper and enum callers; compare with user-entered test dates on an authorized device. |
| Days Matter — inclusion/count direction | DEX derives day delta and adjusts one side for `includeDueDate`. | Exact UI meaning for past/future and translated labels. | No; chosen semantics are tested and isolated in DateEngine. | Capture same-day, prior-day and future-day outputs from test events. |
| Days Matter — backup | Serializable backup model includes old/new event/category, relation and background-image lists plus timestamp/version. | Container format, compression/encryption, version migration and conflict policy. | No; new independent backup format can be introduced later. | Trace export/import Activity and serializer call chain in DEX. |
| Days Matter — milestones | Milestone fields and current-year progress formula are visible. | Generated milestone values and historical ordering. | No; current milestone schema is only a placeholder. | Trace milestone manager generation call sites. |
| Days Matter — photos | Crop activity/model and ratio options are visible. | Final bitmap write location, quality and Realm relation transaction. | No; media feature deferred. | Trace crop result save callback and image model links. |
| Meiyou — prediction | `ServiceOvulationModel` stores `predict_ovulation` with `predict_num`; phase range/result models exist. | Prediction formula vs service response and personalized inputs. | Not part of this phase. | Authorized input/output cases plus targeted service/model call-chain analysis. |
| TimeXu — protected business logic | 360/Qihoo Jiagu loader shell and sample SQLite schema/table rows. | Runtime business DEX and true DAO/UI call graph for todo, repeats, quadrants and widgets. | No; TimeXu module is explicitly out of phase. | Authorized instrumented device or vendor-provided unprotected test build; current environment has no Android runtime/Frida. |

## Environment limitation

No `gradle`, Android SDK, `adb`, emulator, UIAutomator/Appium or Frida is present in the current execution workspace. Java 17 exists. The Gradle distribution endpoint timed out; the project has not been compiled, unit tests have not been run, and no APK or dynamic validation is available. Install JDK/Android SDK/Gradle on a connected development host, then run the build and unit test commands in `README.md`.
