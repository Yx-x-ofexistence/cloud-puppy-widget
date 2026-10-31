# 云朵小卡 · Cloud Puppy Widget

一个离线 Android 桌面小组件应用。用时间、照片、便签、打卡和喝水记录，配上喜欢的贴纸，留一小块日常风景。

<img src="design/golden-cloud-icon/xu-golden-cloud.svg" alt="金毛抱云图标" width="144">

## 功能

- 2×2 / 4×2：日期与时间、照片、倒数日、便签、今日事项打卡、今日喝水。
- 1×1：透明素材组件，可放置一个小素材。
- 背景颜色、照片、透明度，内置离线字体、边框、圆角和可拖动贴纸。
- 导入单张 PNG 和图片素材 ZIP；不执行素材包代码。
- 喝水逐次记录整数 mL、撤销今天最后一次、保留跨日历史；同一张喝水卡的多个桌面实例共享记录。
- 本地 ZIP 备份与合并恢复：卡片、照片、导入素材、打卡及饮水历史；重复恢复跳过，冲突卡另存。
- 编辑器与桌面共享布局，按实际宿主尺寸校准比例。

应用不需要登录，不含广告、支付、云同步或网络权限。数据保存在应用私有目录；导入导出由系统文件选择器完成。备份 ZIP 未加密。

## 当前状态

当前源码基线：**0.3.4-candidate / versionCode 7**，Android 8.0（API 26）起。

这是持续维护中的个人项目，已完成 Android 12 模拟器原生组件宿主、备份和覆盖升级检查，等待真实手机反馈。厂商桌面的尺寸报告、省电策略、跨日刷新和系统文件提供器仍需各自真机验收。

本仓库发布源码。私人签名、个人数据、设备快照和本地候选 APK 不进入 Git；现有安装的升级签名由维护者另行保管。

## 构建

准备 JDK 17 或更新的兼容 JDK、Android SDK Platform 36 和对应 build-tools。在 `android/local.properties` 中填写 `sdk.dir`，或设置 `ANDROID_HOME`，然后：

```powershell
cd android
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --no-daemon
```

macOS / Linux 使用 `./gradlew`。Gradle Wrapper 固定 9.8.2 并校验发行包 SHA-256；Android Gradle Plugin 为 9.4.1。首次构建需要下载构建工具与依赖，应用运行保持离线。

普通克隆使用 Android 开发调试签名。维护者本地如存在 `.tools/signing/candidate.jks`，继续沿用原本候选签名；该文件始终被忽略。公开源码不包含可升级维护者既有安装的私钥。

也可在项目根目录运行 `./build.ps1 -Verify`；脚本优先使用项目内已有 Gradle，缺少时使用 Wrapper。设置 `JAVA_HOME` 指向自己的 JDK。

## 项目结构

```text
android/app/src/main/         应用、内置素材及字体许可
android/app/src/debug/        原生 QA 宿主（不进入 release）
android/app/src/androidTest/  仪器测试源码与公开示例素材
design/                      SVG 图标与项目生成的素材原稿
tools/                       素材打包、矢量转换工具
docs/                        构建/验收说明及上游引用
```

测试包含通用备份、喝水、样式和真实 RemoteViews 宿主检查。历史升级断言使用维护者本机的升级前快照；这些设备快照不公开，直接运行对应升级阶段需要自备基线数据。普通 debug / release 构建不依赖它们。测试宿主会创建 `qa034-*` 等 QA 卡，请使用测试设备。

格式说明：[喝水数据](WATER-DATA-v1.md)、[备份格式 2](BACKUP-FORMAT-v2.md)、[备份使用](BACKUP-GUIDE.md)、[PNG/ZIP 素材](ASSET-PACK-GUIDE.md)。本次基线检查见 [验证说明](docs/VALIDATION.md)。

## 许可

本项目新增代码、文档及项目自制素材采用 [MIT](LICENSE)。保留的上游移植部分和 Gradle Wrapper 采用 Apache-2.0；内置字体按各自 SIL OFL 1.1 分发，原版权及完整许可随文件保留。MIT 不替换这些第三方许可，详见 [第三方说明](THIRD_PARTY_NOTICES.md)。

## 来源与设计参考

- 原作者项目：[Material Photo Widget](https://github.com/fibelatti/photo-widget)，作者 **Filipe Belatti**。本项目参考基线为 [v1.48.1 / 68ca1e1](https://github.com/fibelatti/photo-widget/tree/68ca1e1059e3005ba7511311dbec9f7ebe4cf632)，沿用并修改文字排版转位图方案；独立 Android 模块提供当前界面、卡片与本地数据功能。原版权和 Apache-2.0 许可保留。
- UI 与交互设计参考：**iScreen**。
- 狗狗与手账插画：为本项目通过 OpenAI ImageGen 生成并整理；应用图标以本仓库 SVG 原稿转换为 Android VectorDrawable / adaptive icon。
