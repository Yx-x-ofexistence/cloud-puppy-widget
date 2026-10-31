# 第三方来源与许可

## Material Photo Widget

- Author: Filipe Belatti; Copyright 2023 Filipe Belatti.
- Source: https://github.com/fibelatti/photo-widget
- Baseline: v1.48.1, commit `68ca1e1059e3005ba7511311dbec9f7ebe4cf632`.
- License: Apache License 2.0, full text in `LICENSES/Apache-2.0.txt` and the application assets.
- Modified file: `android/app/src/main/java/cn/xi/cloudwidget/Renderer.java` ports and changes the `PhotoWidgetText.kt` TextPaint / StaticLayout / textToBitmap approach. It adds a bounded logical card frame and integrates the project renderer. The source file retains the attribution and modification notice.
- `docs/upstream-reference/PhotoWidgetText.kt` is an unchanged reference copy from the stated commit, retained under Apache-2.0. The original Compose editor and its runtime dependency graph are not bundled in the standalone app.

The project's MIT license covers its new work and modifications without removing the original copyright, attribution or Apache-2.0 terms applicable to the upstream material.

## Gradle Wrapper

`android/gradlew`, `android/gradlew.bat` and `android/gradle/wrapper/gradle-wrapper.jar` are Gradle-generated wrapper files, distributed under Apache-2.0. Original notices in the launch scripts remain. The pinned distribution checksum is recorded in `gradle-wrapper.properties`.

## Fonts

The following original, unmodified font files are bundled for offline use. They remain under SIL Open Font License 1.1, not MIT. Copyright statements and full license texts are included in `android/app/src/main/assets/licenses/`.

| File | Project / copyright source | License file |
| --- | --- | --- |
| wenkai.ttf | [LXGW WenKai](https://github.com/lxgw/LxgwWenKai), derived from Klee One by Fontworks | LXGWWenKai-OFL.txt |
| sans.otf | [Source Han Sans](https://github.com/adobe-fonts/source-han-sans), Adobe | SourceHanSans-LICENSE.txt |
| ChillRoundFRegular.ttf | [ChillRound](https://github.com/Warren2060/ChillRound), MOTOYA and project contributors | ChillRound-OFL.txt, NEW-FONTS-NOTICE.txt |
| XiaolaiSC-Regular.ttf | [kose-font / Xiaolai](https://github.com/lxgw/kose-font), LXGW; derived from SetoFont by Nozomi Seto | Xiaolai-OFL.txt, NEW-FONTS-NOTICE.txt |
| LXGWMarkerGothic-Regular.ttf | [LXGW Marker Gothic](https://github.com/lxgw/LxgwMarkerGothic), project authors | MarkerGothic-OFL.txt, NEW-FONTS-NOTICE.txt |

Source Han Serif is no longer bundled as of v0.3.4. Font ID 3 remains a compatibility tombstone and falls back to the system font. Its former file and license are preserved in private historical source archives and older APKs, not redistributed by this repository.

## Project artwork and UI reference

Bundled puppy/handbook illustration assets were generated for this project using OpenAI ImageGen and split into independent images; their source sheets are retained under `design/`. The cloud-hugging puppy icon uses `design/golden-cloud-icon/xu-golden-cloud.svg` as its vector source.

UI / interaction design reference: **iScreen**. It is listed as a design reference, not as a code or asset dependency.
