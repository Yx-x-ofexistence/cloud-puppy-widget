# 狗狗小组件视觉草稿 v1

日期：2026-10-27。使用内置 imagegen 生成；本轮仅制作视觉草稿，没有实施 App。

- cream-puppy-stickers.png：奶油粉狗狗与配件素材总图，16 个素材单元。
- blue-puppy-reading.png：浅蓝阅读狗狗与配件素材总图，16 个素材单元。
- puppy-widget-card-concepts.png：6 张卡片构图参考，文字区域留白；背景与角色尚未分层，不作为已完成的独立组件资产。

两张贴纸总图需要后续拆分、边缘清理和小尺寸检查；卡片图用于确认风格与留白。未写入 PRD 的最终视觉选择，待熙和☁️挑选。不把图中的装饰标记当作真实打卡状态。

原始生成文件保留在 Codex generated_images 中，本目录保存无修改副本。所有提示词均请求透明背景；实际文件透明情况以检查记录和图像自身为准，不因提示词而宣称结果透明。

## 最终提示词

### cream-puppy-stickers

transparent_background: true

```text
Use case: stylized-concept. Create a polished original cartoon puppy sticker ASSET SHEET for a personal Android home-screen widget app. Square high-resolution image, transparent alpha background. Cozy cream-and-blush-pink scrapbook aesthetic, warm dark-brown hand-drawn outlines, soft flat gouache fills with very subtle pencil texture, charming clean shapes, readable at small size. One consistent ORIGINAL cream puppy character with caramel floppy ears, rounded muzzle, tiny eyebrows, rosy cheeks, short paws, tiny blush scarf; cute gentle proportions, not any existing mascot. Exactly 16 fully separate elements arranged in an evenly spaced 4 by 4 invisible grid, generous transparent gutters, no overlapping, every element complete and uncropped. Cells 1-6: puppy reading an open book; puppy hugging a small heart; puppy holding a blank memo card; puppy waving cheerfully; puppy sleeping on a cushion; puppy proudly raising a paw beside a small completion checkmark. Cells 7-16: closed book stack; tiny paw-print cluster; pink pencil; cream blank memo slip with pink tape; small flower sprig; star trio; heart-shaped paper clip; curled ribbon bow; mug with steam; blank rounded scrapbook label. Slight cream sticker keylines around each element, actual transparency beyond keylines. No words, no letters, no numerals, no logos, no watermark, no page background, no checkerboard pattern, no photographic shadows. This is a reusable collection, not a finished app screenshot. Emphasize expressive adorable DOG anatomy and elegant restrained pastel illustration.
```

### blue-puppy-reading

transparent_background: true

```text
Use case: stylized-concept. Draw an original cartoon dog reading-companion sticker ASSET SHEET for Android home-screen widgets. Square high-resolution PNG-style image with TRUE transparent alpha background. Pale powder blue, muted slate blue, warm ivory, tiny peach accent palette. Refined hand-drawn stationery illustration, warm charcoal thin rounded outlines, soft opaque watercolor/gouache fills, subtle pencil texture, lovely airy storybook style, strong legibility at small size. Character: an ORIGINAL fluffy ivory puppy, floppy blue-gray-tipped ears, dot eyes, little oval nose, rounded paws, small powder-blue neckerchief, soft blush; clearly a dog, not a cat, no existing copyrighted mascot. Exactly 16 isolated assets in an evenly spaced 4x4 invisible grid with wide transparent gutters. First six cells: puppy sitting reading a book; puppy leaning thoughtfully on a book stack; puppy writing with a pencil in a notebook; puppy peeking over a blank rectangular memo; puppy carrying a tiny backpack and waving; puppy curled asleep beside an open book. Remaining ten cells: open book; small blue reading lamp; blank cream notebook with pale blue spine; two pieces of pale blue gingham washi tape; constellation of three stars; blue paw prints; teacup; small leafy sprig; folded paper corner; blank scalloped cream label outlined in blue. All pieces separated and intact, no edge cropping. Crisp silhouettes, restrained details, slight pale ivory sticker rim around assets. No text, no numbers, no clock faces, no logos, no watermark, no background scenery, no checkered transparency illustration.
```

### puppy-widget-card-concepts

transparent_background: true

```text
Use case: ui-mockup / illustration-story. Create a beautiful VISUAL CONCEPT SHEET of SIX original decorative dog-themed Android widget background cards, prepared for later live text overlay. Transparent alpha outside cards. Portrait or square high-resolution layout: 2 columns by 3 rows, equal-sized square cards, wide empty transparent gutters, no outer page frame, no captions. LEFT column three cream/blush pink scrapbook cards: (top) small adorable cream floppy-eared puppy holding an open book anchored lower right, subtle cream paper texture, hand-drawn cocoa rounded border, a tiny pink washi tab at top left; (middle) cream puppy peeking from bottom edge, two small heart and star decorations at corners, pale cream-pink background and softly sketched border; (bottom) cream puppy holding a pencil at lower left, very subtle peach notebook ruling only in background and a ribbon at top right. RIGHT column three powder-blue/ivory reading cards: (top) fluffy ivory floppy-eared puppy with blue-gray ear tips and blue neckerchief reading at lower right, pale blue-gray rounded frame and a tiny book at top left; (middle) small sleeping puppy resting along the bottom edge, faint ivory grid paper and restrained star corners; (bottom) puppy leaning on book stack at lower left, soft powder-blue corner tabs and cream blank memo area. Both sets use sophisticated cute hand-drawn gouache stationery art with warm dark rounded outlines, gentle flat pastel fills, original dog characters. In EACH card keep at least the central and upper 60 percent clean blank and unobstructed for future date/time/countdown/note content. Decorations must stay small along corners and bottom, do not dominate the card. Card interiors may be opaque pale colors; all space between cards truly transparent. NO words, no lettering, no numbers, no fake times, no calendar dates, no user-interface controls, no logos, no watermark. These are six coordinated illustration compositions, not screenshots or a smartphone mockup. Preserve generous breathing room and tasteful cozy charm.
```

