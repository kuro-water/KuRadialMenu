# KuRadialMenu 開発計画 v2

**作成日**: 2025-12-22  
**最終更新**: 2026-01-07  
**前回計画**: 2025-12-19_development_plan.md  
**ステータス**: Phase 2 完了 - Phase 3 着手待ち

**記載者**: GitHub Copilot (Claude 3.5 Sonnet)  
**更新履歴**:
- 2025-12-22: 初版作成
- 2026-01-07: Phase 1, 2 完了、フェーズステータス更新

---

## 1. 概要

KuRadialMenu は Minecraft に直感的なラジアルメニュー UI を追加するクライアントサイド QoL Mod です。
本レポートは、YACL（Yet Another Config Lib）の導入を踏まえた開発計画の更新版です。

### 1.1 目標

- キーバインド押下中にラジアルメニューを表示
- Minecraft の全キーバインドをメニュー項目として実行可能
- **YACL を使用した設定 UI**（更新）
- 設定の永続化（JSON 形式）

### 1.2 対応バージョン

| Minecraft | Java | Fabric Loader | Fabric API | YACL |
|-----------|------|---------------|------------|------|
| 1.19.4 | 17 | 0.18.2 | 0.87.2+1.19.4 | 3.0.3+1.19.4 |
| 1.20.1 | 17 | 0.18.2 | 0.92.6+1.20.1 | 3.6.6+1.20.1-fabric |
| 1.21.1 | 21 | 0.18.2 | 0.116.7+1.21.1 | 3.8.1+1.21.1-fabric |

### 1.3 前回計画からの変更点

| 項目 | 旧計画 | 新計画 |
|------|--------|--------|
| 設定ライブラリ | 自前 JSON 実装 | **YACL** |
| Fabric Loader | 0.16.10 | **0.18.2** |
| パッケージ構造 | `config/`, `keybind/`, `menu/`, `action/`, `render/` | `config/`, `keybind/`, `model/`, `ui/`, `util/` |
| ConfigManager | 自前実装 | YACL ベース |

---

## 2. 現状の実装状況

### 2.1 ディレクトリ構造（現在）

```
src/main/java/dev/kurowater/kuradialmenu/
├── KuRadialMenuClient.java          # エントリーポイント（実装済み）
├── client/
│   ├── config/                      # 空（.gitkeep のみ）
│   ├── keybind/                     # 空（.gitkeep のみ）
│   ├── model/                       # 空（.gitkeep のみ）
│   ├── ui/                          # 空（.gitkeep のみ）
│   └── util/                        # 空（.gitkeep のみ）
└── mixin/
    └── client/                      # 空（.gitkeep のみ）
```

### 2.2 リソース（現在）

```
src/main/resources/
├── fabric.mod.json                  # 設定済み
├── kuradialmenu.mixins.json         # 空のクライアント Mixin
└── assets/kuradialmenu/
    ├── icon.png                     # Mod アイコン
    └── lang/
        ├── en_us.json               # 翻訳（キーバインド名のみ）
        └── ja_jp.json               # 翻訳（キーバインド名のみ）
```

### 2.3 依存関係（現在）

- **Fabric API モジュール**: `fabric-lifecycle-events-v1`, `fabric-resource-loader-v0`, `fabric-content-registries-v0`
- **YACL**: 各バージョン対応済み

---

## 3. アーキテクチャ設計（更新版）

### 3.1 パッケージ構造

```
src/main/java/dev/kurowater/kuradialmenu/
├── KuRadialMenuClient.java          # エントリーポイント
├── client/
│   ├── config/
│   │   ├── ModConfig.java           # 設定データクラス（YACL 対応）
│   │   ├── ModConfigScreen.java     # YACL 設定画面
│   │   └── ConfigHandler.java       # 設定の読み書き
│   ├── keybind/
│   │   ├── ModKeybinds.java         # Mod のキーバインド登録
│   │   └── KeybindHandler.java      # キーバインド状態管理
│   ├── model/
│   │   ├── RadialMenu.java          # メニューのデータモデル
│   │   ├── MenuSlot.java            # 個別スロットのデータ
│   │   └── MenuAction.java          # アクションインターフェース
│   ├── ui/
│   │   ├── RadialMenuScreen.java    # Screen 実装
│   │   └── RadialMenuRenderer.java  # 描画処理
│   └── util/
│       └── MathHelper.java          # 角度計算等のユーティリティ
└── mixin/
    └── client/
        └── (必要に応じて追加)
```

### 3.2 YACL 統合設計

#### 設定構造

```java
// ModConfig.java - YACL 対応の設定クラス
public class ModConfig {
    // 一般設定
    private int slotCount = 8;
    
    // UI 設定
    private int innerRadius = 30;
    private int outerRadius = 100;
    private int centerDeadzone = 20;
    
    // スロット設定
    private List<SlotConfig> slots = new ArrayList<>();
    
    // YACL 3.x の自動シリアライズを活用
}
```

#### YACL バージョン差異対応

| 機能 | YACL 3.0.x (1.19.4) | YACL 3.6.x+ (1.20.1, 1.21.1) |
|------|---------------------|------------------------------|
| 基本 API | ほぼ同じ | ほぼ同じ |
| Config 定義 | `@ConfigEntry` | `@AutoGen` + `@SerialEntry` |
| Builder パターン | 同一 | 同一 |

**対応方針**: YACL 3.x 系共通の Builder API を使用し、バージョン分岐を最小化

### 3.3 クラス設計（更新版）

```
┌─────────────────────────────────────────────────────────────┐
│                    KuRadialMenuClient                       │
│  - エントリーポイント                                        │
│  - 各 Handler の初期化                                       │
└─────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  ConfigHandler  │ │ KeybindHandler  │ │   RadialMenu    │
│  - YACL 連携    │ │ - キー状態監視   │ │ - スロット管理   │
│  - JSON 永続化  │ │ - メニュー開閉   │ │ - 選択判定       │
└─────────────────┘ └─────────────────┘ └─────────────────┘
         │
         ▼
┌─────────────────┐
│ ModConfigScreen │
│  - YACL UI 構築 │
│  - 設定項目表示  │
└─────────────────┘
```

---

## 4. 実装計画（更新版）

### 4.1 フェーズ分割

| フェーズ | 内容 | 優先度 | ステータス |
|---------|------|--------|-----------|
| Phase 0 | プロジェクト基盤構築 | 高 | **完了** |
| Phase 1 | YACL 設定システム構築 | 高 | **完了** |
| Phase 2 | キーバインド・メニュー表示 | 高 | **完了** |
| Phase 3 | アクション実行機能 | 高 | 未着手 |
| Phase 4 | UI 改善・アニメーション | 中 | 未着手 |

### 4.2 Phase 1: YACL 設定システム構築

**目標**: YACL を使用した設定管理と設定画面を構築  
**ステータス**: ✅ **完了** (2025-12-22)

**タスク**:
1. `ModConfig.java` - 設定データクラス作成
2. `ConfigHandler.java` - YACL シリアライズ/デシリアライズ
3. `ModConfigScreen.java` - YACL 設定画面構築
4. `fabric.mod.json` に YACL 依存とカスタムメニューエントリーポイント追加

**成果物**:
- Mod Menu から設定画面を開ける
- 設定が `config/kuradialmenu.json` に保存される
- スロット数（4/8/12）、UI パラメータを設定可能

**YACL 設定画面の構成**:
```
カテゴリ: 一般設定
├─ スロット数 (IntegerSlider: 4, 8, 12)
└─ メニューキー (Keybind)

カテゴリ: UI 設定
├─ 内側半径 (IntegerSlider: 10-100)
├─ 外側半径 (IntegerSlider: 50-300)
└─ 中央デッドゾーン (IntegerSlider: 5-50)

カテゴリ: スロット設定
└─ (Phase 3 で実装)
```

### 4.3 Phase 2: キーバインド・メニュー表示

**目標**: キーバインドでラジアルメニューを表示  
**ステータス**: ✅ **完了** (2026-01-07)

**タスク**:
1. `ModKeybinds.java` - メニューキー登録
2. `KeybindHandler.java` - キー状態監視（ホールド検出）
3. `RadialMenu.java` - メニューロジック
4. `MenuSlot.java` - スロットデータ
5. `RadialMenuScreen.java` - Screen 実装
6. `RadialMenuRenderer.java` - 扇形描画

**成果物**:
- R キー（デフォルト）押下でメニュー表示
- マウス方向でハイライト変化
- 中央デッドゾーンで「選択なし」

**実装完了内容** (2026-01-07):
- `RadialMenuScreen.java` - Screen 実装（全バージョン対応）
- `RadialMenuRenderer.java` - 扇形描画（Tessellator ベース、バージョン別 API 対応）
- `KeybindHandler.java` - キーバインド押下でメニュー表示
- 背景ぼかし効果（`BLUR` シェーダー、1.21.1 では Stonecutter 分岐）
- 全バージョン（1.19.4, 1.20.1, 1.21.1）で動作確認済み

### 4.4 Phase 3: アクション実行機能

**目標**: キーバインドアクションの実行とスロット設定

**タスク**:
1. `MenuAction.java` - アクションインターフェース
2. キーバインドアクション実装
3. スロット設定 UI（YACL）
4. デフォルトスロット設定

**成果物**:
- メニューから任意のキーバインドを実行可能
- 設定画面でスロットにアクションを割り当て可能

---

## 5. 技術的考慮事項

### 5.1 YACL バージョン間差異

| 項目 | 1.19.4 (YACL 3.0.x) | 1.20.1+ (YACL 3.6.x+) |
|------|---------------------|----------------------|
| Maven 座標 | `dev.isxander.yacl:yet-another-config-lib-fabric` | `dev.isxander:yet-another-config-lib` |
| API | ほぼ共通 | ほぼ共通 |

**対応方針**: `build.gradle.kts` で分岐済み。コード側は共通 API を使用。

### 5.2 Stonecutter 分岐ポイント

| 機能 | 1.19.4 / 1.20.1 | 1.21.1 | 分岐方法 |
|------|-----------------|--------|----------|
| `Identifier` | `new Identifier()` | `Identifier.of()` | 既存の `KuRadialMenuClient.id()` |
| `Screen#render` | `MatrixStack` | `DrawContext` | Stonecutter 条件分岐 |
| `Text` | `Text.literal()` | 同左 | 分岐不要 |

### 5.3 Mixin の使用方針

現時点では Mixin は**不要**:
- キーバインド監視: Fabric API の `ClientTickEvents` で対応
- 描画: `Screen` 継承で対応
- 設定画面: YACL が提供

---

## 6. ファイル一覧

### 6.1 新規作成ファイル

```
src/main/java/dev/kurowater/kuradialmenu/client/
├── config/
│   ├── ModConfig.java
│   ├── ConfigHandler.java
│   └── ModConfigScreen.java
├── keybind/
│   ├── ModKeybinds.java
│   └── KeybindHandler.java
├── model/
│   ├── RadialMenu.java
│   ├── MenuSlot.java
│   └── MenuAction.java
├── ui/
│   ├── RadialMenuScreen.java
│   └── RadialMenuRenderer.java
└── util/
    └── MathHelper.java
```

### 6.2 修正ファイル

```
src/main/java/dev/kurowater/kuradialmenu/KuRadialMenuClient.java
  - ConfigHandler, KeybindHandler の初期化追加

src/main/resources/fabric.mod.json
  - YACL 依存関係追加
  - Mod Menu エントリーポイント追加（オプション）

src/main/resources/assets/kuradialmenu/lang/en_us.json
src/main/resources/assets/kuradialmenu/lang/ja_jp.json
  - 設定画面の翻訳追加
```

---

## 7. デフォルト設定

初回起動時のデフォルトメニュー構成（8 スロット）:

| Index | 方向 | アクション | ラベル |
|-------|------|-----------|--------|
| 0 | 上 | key.inventory | インベントリ |
| 1 | 右上 | key.swapOffhand | オフハンド切替 |
| 2 | 右 | key.drop | アイテムドロップ |
| 3 | 右下 | key.togglePerspective | 視点切替 |
| 4 | 下 | key.command | コマンド |
| 5 | 左下 | key.screenshot | スクリーンショット |
| 6 | 左 | key.advancements | 進捗 |
| 7 | 左上 | key.playerlist | プレイヤーリスト |

---

## 8. 次のステップ

1. ~~**承認**: 本計画の承認をお願いします~~ ✅ 承認済み
2. ~~**Phase 1 実装開始**: 承認後、YACL 設定システム構築から開始~~ ✅ 完了
3. ~~**Phase 2 実装**: キーバインド・メニュー表示機能~~ ✅ 完了 (2026-01-07)
4. **Phase 3 実装開始**: アクション実行機能の実装へ（承認待ち）
5. **動作確認**: 各フェーズ完了ごとに動作確認

---

## 9. 承認依頼

### Phase 1, 2 完了報告

Phase 1（YACL 設定システム構築）および Phase 2（キーバインド・メニュー表示）が完了しました。

**完了した機能**:
- ✅ YACL を使用した設定管理
- ✅ 設定画面（Mod Menu から開ける）
- ✅ 設定の永続化（JSON 形式）
- ✅ R キー長押しでラジアルメニュー表示
- ✅ マウス方向でスロットハイライト
- ✅ 中央デッドゾーンによる「選択なし」状態
- ✅ 背景ぼかし効果
- ✅ 全バージョン（1.19.4, 1.20.1, 1.21.1）で動作確認済み

### Phase 3 実装開始の承認依頼

Phase 3（アクション実行機能）の実装を開始してよろしいでしょうか？

- [ ] Phase 3 実装開始を承認
- [ ] 修正要望あり（コメント欄に記載）

**承認いただければ、Phase 3（アクション実行機能）の実装を開始します。**

