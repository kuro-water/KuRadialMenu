# ラジアルメニュー描画バージョン差異調査レポート

## メタ情報

| 項目 | 内容 |
|------|------|
| 記載者 | GitHub Copilot |
| 使用モデル | Claude 3.5 Sonnet |
| 作成日時 | 2025-12-23 |
| 更新日時 | 2025-12-23 |
| ステータス | ✅ 修正完了・検証待ち |

---

## 1. 概要

ラジアルメニューの描画が Minecraft バージョンによって異なる問題を調査。

| バージョン | 状態 | 問題 |
|-----------|------|------|
| 1.19.4 | ❌ 不具合 | 円弧が全く描画されず、ラベルのみ表示 |
| 1.20.1 | ✅ 理想 | 正しく円弧・ラベル・境界線が表示 |
| 1.21.1 | ⚠️ 部分的 | 円弧は描画されるが位置ずれあり |

---

## 2. 原因分析

### 2.1 根本原因: 描画アルゴリズムの不統一

`RadialMenuRenderer.java` で **2つの完全に異なる描画方式** が使用されている:

#### 1.20以降 (DrawContext API)
```java
// context.fill() を使用 - 矩形描画API
context.fill(x1Inner, y1Inner, x1Outer, y1Outer, color);
```

**問題点**: `DrawContext.fill()` は**軸平行の矩形**を描画するAPIであり、4点の座標から円弧を描くことは**不可能**。引数は `(x1, y1, x2, y2, color)` であり、対角2点を指定する矩形描画。

#### 1.19.4以前 (Tessellator API)
```java
// TRIANGLE_STRIP で頂点を直接指定
buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
buffer.vertex(...).color(...).next();
tessellator.draw();
```

**こちらが正しいアプローチ**だが、1.19.4ではラベルのみ表示されていることから、描画自体が実行されていない可能性がある。

### 2.2 各バージョンの詳細問題

| バージョン | 使用API | 問題の詳細 |
|-----------|---------|-----------|
| **1.19.4** | Tessellator + TRIANGLE_STRIP | 描画されない。RenderSystemの状態設定不足の可能性（深度テスト、ブレンドモード等） |
| **1.20.1** | DrawContext.fill() | 矩形描画APIだが、セグメント数が多いため**偶然うまく見える** |
| **1.21.1** | DrawContext.fill() | GUIスケーリング変更により座標計算にずれが発生 |

### 2.3 1.21.1の位置ずれ原因

Minecraft 1.21で GUI スケーリングの内部実装が変更された。`DrawContext` の座標系が異なる可能性がある。また、1.21.1のスクリーンショットを見ると**ぼやけている**ことから、スケーリング係数の不一致が疑われる。

---

## 3. 修正方針

### 3.1 統一描画アルゴリズムの採用

**全バージョンで Tessellator/BufferBuilder を使用**する。`DrawContext.fill()` は矩形専用のため、円弧描画には不適切。

### 3.2 具体的修正内容

#### Phase 1: 1.20以降の描画を Tessellator ベースに変更

```java
//? if >=1.20 {
private void drawSlotArc(DrawContext context, int centerX, int centerY,
        int innerRadius, int outerRadius, double startAngle, double endAngle, int color) {
    
    Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
    
    float a = ((color >> 24) & 0xFF) / 255f;
    float r = ((color >> 16) & 0xFF) / 255f;
    float g = ((color >> 8) & 0xFF) / 255f;
    float b = (color & 0xFF) / 255f;

    RenderSystem.enableBlend();
    RenderSystem.setShader(GameRenderer::getPositionColorProgram);
    
    BufferBuilder buffer = Tessellator.getInstance().begin(
        VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

    for (int i = 0; i <= SEGMENTS_PER_SLOT; i++) {
        double angle = Math.toRadians(startAngle + (endAngle - startAngle) * i / SEGMENTS_PER_SLOT - 90);
        float xInner = centerX + (float)(Math.cos(angle) * innerRadius);
        float yInner = centerY + (float)(Math.sin(angle) * innerRadius);
        float xOuter = centerX + (float)(Math.cos(angle) * outerRadius);
        float yOuter = centerY + (float)(Math.sin(angle) * outerRadius);

        buffer.vertex(matrix, xInner, yInner, 0).color(r, g, b, a);
        buffer.vertex(matrix, xOuter, yOuter, 0).color(r, g, b, a);
    }

    BufferRenderer.drawWithGlobalProgram(buffer.end());
    RenderSystem.disableBlend();
}
//?}
```

#### Phase 2: 1.19.4の描画修正

RenderSystem の状態設定を追加:

```java
RenderSystem.enableBlend();
RenderSystem.defaultBlendFunc();
RenderSystem.disableDepthTest();  // GUIでは深度テスト不要
RenderSystem.setShader(GameRenderer::getPositionColorProgram);
```

#### Phase 3: バージョン間API差異の吸収

| API | 1.19.4 | 1.20.1 | 1.21.1 |
|-----|--------|--------|--------|
| BufferBuilder.begin() | `buffer.begin(mode, format)` | `Tessellator.getInstance().begin(mode, format)` が BufferBuilder を返す | 同左 |
| 頂点追加 | `.vertex().color().next()` | `.vertex().color()` (next不要) | 同左 |
| 描画 | `tessellator.draw()` | `BufferRenderer.drawWithGlobalProgram(buffer.end())` | 同左 |

---

## 4. 修正コード案

### RadialMenuRenderer.java 修正版

1.19.4と1.20以降で共通の TRIANGLE_STRIP ロジックを使用し、API差異のみStonecutterで分岐:

```java
// 共通ロジック（アルゴリズムは同一）
// - 円弧を TRIANGLE_STRIP で描画
// - RenderSystem で適切な状態を設定
// - バージョン固有のAPIコールのみ分岐
```

---

## 5. 実装完了

### 5.1 修正内容

以下のファイルを修正しました:

#### `RadialMenuRenderer.java`
- **1.20以降**: `context.fill()` (矩形API) から `Tessellator` + `TRIANGLE_STRIP` に変更
- **1.19.4**: `RenderSystem` の状態設定を追加 (`defaultBlendFunc()`, `disableDepthTest()`)
- **ボーダー描画**: 全バージョンで `DEBUG_LINES` モードを使用した正しい線描画に変更
- **共通化**: 円弧描画アルゴリズムを全バージョンで統一
- **API差異対応**: 1.20と1.21でTessellator APIが異なるため、適切なバージョン分岐を追加
  - 1.21: `Tessellator.getInstance().begin()` → `BufferRenderer.drawWithGlobalProgram(buffer.end())`
  - 1.20: `tessellator.getBuffer()` → `buffer.begin()` → `tessellator.draw()` + `.next()`

### 5.2 技術的な改善点

| 項目 | 修正前 | 修正後 |
|------|--------|--------|
| **円弧描画** | 1.20+: 矩形API誤用<br>1.19.4: Tessellator (設定不足) | 全バージョン: Tessellator + TRIANGLE_STRIP (正しい設定) |
| **ボーダー描画** | 1.20+: 矩形の連続<br>1.19.4: なし | 全バージョン: DEBUG_LINES で正しい線描画 |
| **RenderSystem** | ブレンド設定のみ | ブレンド + 深度テスト無効化 |

### 5.3 検証方法

以下のコマンドで各バージョンをビルドして動作確認してください:

```powershell
# 1.19.4
.\gradlew.bat stonecutterSwitchTo1.19.4
.\gradlew.bat runClient

# 1.20.1
.\gradlew.bat stonecutterSwitchTo1.20.1
.\gradlew.bat runClient

# 1.21.1
.\gradlew.bat stonecutterSwitchTo1.21.1
.\gradlew.bat runClient
```

**確認項目**:
1. ラジアルメニューの円弧が正しく表示される
2. スロット間のボーダー線が正しく表示される
3. メニューが安定して開閉する

---

## 6. ネクストアクション

1. **✅ 完了**: 描画アルゴリズムの統一（Tessellatorベース）
2. **⏳ 検証待ち**: ユーザーによる各バージョンでの動作確認
3. **承認待ち**: 検証結果に基づくPhase 2完了判定

---

## 7. 補足: ラジアルメニューUIの描画方法

### Q: ラジアルメニューのUIってどうやってるの？画像？

**A: いいえ、画像は使っていません。プログラムで動的に描画しています。**

### 描画方法の詳細

#### 1. **円弧の描画** (Triangle Strip)
```java
// 内側と外側の頂点を交互に配置することで扇形を構成
for (int i = 0; i <= SEGMENTS_PER_SLOT; i++) {
    double angle = 計算式;
    buffer.vertex(内側の頂点).color(...);
    buffer.vertex(外側の頂点).color(...);
}
```

**仕組み**:
- `TRIANGLE_STRIP` モード: 連続する3頂点で三角形を形成
- 頂点順序: 内→外→内→外... と交互に配置
- 結果: 扇形（円弧）が三角形の連続で形成される

**視覚的イメージ**:
```
        外側 o----o----o----o
            /|   /|   /|   /|
           / |  / |  / | / |
          /  | /  | /  |/  |
内側 o----o----o----o----o
    (三角形が連続して扇形を形成)
```

#### 2. **セグメント数**
```java
private static final int SEGMENTS_PER_SLOT = 32;
```
- 1スロットあたり32分割
- 分割数が多いほど滑らかな円になる（計算量とのトレードオフ）

#### 3. **座標計算**
```java
double angle = Math.toRadians(startAngle + ... - 90);
float x = centerX + (float)(Math.cos(angle) * radius);
float y = centerY + (float)(Math.sin(angle) * radius);
```
- 三角関数（cos/sin）で円周上の座標を計算
- `-90` は座標系の調整（上方向を0度にするため）

### なぜ画像を使わないのか？

| 項目 | 画像使用 | プログラム描画 |
|------|---------|---------------|
| **柔軟性** | スロット数固定 | 任意のスロット数に対応 |
| **カスタマイズ** | 色変更には複数画像が必要 | 設定ファイルで自由に変更可能 |
| **解像度** | 拡大縮小で劣化 | どんなサイズでも綺麗 |
| **ファイルサイズ** | 画像ファイルが必要 | コードのみ（軽量） |

---

## 8. 補足: なぜ1.20.1では「うまく見えた」のか

`context.fill(x1Inner, y1Inner, x1Outer, y1Outer, color)` は本来矩形を描くAPIだが:

1. **SEGMENTS_PER_SLOT = 32** と細かく分割
2. 各セグメントが非常に小さい矩形として描画
3. 矩形が「偶然」円弧状に並んで見えた

これは**意図した動作ではなく副産物**であり、スケーリングやウィンドウサイズが変わると破綻する。1.21.1で見られる位置ずれはこの副産物の限界を示している。

---

**修正完了。検証結果をお待ちしています。**

