# KuRadialMenu 開発計画

**作成日**: 2025-12-19  
**ステータス**: 計画立案完了 - 承認待ち

---

## 1. 概要

KuRadialMenu は Minecraft に直感的なラジアルメニュー UI を追加するクライアントサイド QoL Mod です。
本レポートでは、初期実装（v0.1.0）の設計と実装計画を定義します。

### 1.1 目標

- キーバインド押下中にラジアルメニューを表示
- Minecraft の全キーバインドをメニュー項目として実行可能
- 設定の永続化（JSON 形式）

### 1.2 対応バージョン

| Minecraft | Java | Fabric Loader | Fabric API |
|-----------|------|---------------|------------|
| 1.19.4 | 17 | 0.16.10 | 0.87.2+1.19.4 |
| 1.20.1 | 17 | 0.16.10 | 0.92.6+1.20.1 |
| 1.21.1 | 21 | 0.16.10 | 0.116.7+1.21.1 |

---

## 2. 機能仕様

### 2.1 ラジアルメニュー表示

| 項目 | 仕様 |
|------|------|
| 表示トリガー | キーバインド押下中のみ表示（ホールド方式） |
| デフォルトキー | `R` キー（設定で変更可能） |
| 閉じる | キーを離すと閉じる |
| 項目選択 | マウスカーソルの方向で選択 |
| 実行タイミング | キーを離した瞬間に選択項目を実行 |

### 2.2 メニュー項目数

ユーザーが 3 種類から選択可能:

| 項目数 | 1セクターの角度 | 用途 |
|--------|----------------|------|
| 4 | 90° | シンプル操作向け |
| 8 | 45° | 標準（デフォルト） |
| 12 | 30° | 多機能向け |

### 2.3 実行可能アクション

**Phase 1（初期実装）**:
- Minecraft 標準キーバインドの実行
  - 移動系: 前進、後退、左、右、ジャンプ、スニーク、スプリント
  - アクション系: 攻撃、使用、アイテムドロップ、インベントリ開閉
  - その他: チャット、コマンド入力、視点切り替え、スクリーンショット等

**Phase 2（将来実装）**:
- カスタムコマンド実行
- マクロ機能

### 2.4 設定項目

```json
{
  "version": 1,
  "menuKey": "key.keyboard.r",
  "slotCount": 8,
  "slots": [
    {
      "index": 0,
      "action": {
        "type": "keybind",
        "keybindId": "key.inventory"
      },
      "label": "インベントリ",
      "icon": "minecraft:chest"
    }
  ],
  "ui": {
    "innerRadius": 30,
    "outerRadius": 100,
    "centerDeadzone": 20
  }
}
```

---

## 3. アーキテクチャ設計

### 3.1 パッケージ構造

```
src/main/java/dev/kurowater/kuradialmenu/
├── KuRadialMenuClient.java          # エントリーポイント
├── client/
│   ├── config/
│   │   ├── ModConfig.java           # 設定データクラス
│   │   ├── ConfigManager.java       # 設定の読み書き
│   │   └── SlotConfig.java          # スロット設定
│   ├── keybind/
│   │   ├── ModKeybinds.java         # Mod のキーバインド登録
│   │   └── KeybindManager.java      # キーバインド状態管理
│   ├── menu/
│   │   ├── RadialMenu.java          # メニューのロジック
│   │   ├── RadialMenuScreen.java    # Screen 実装
│   │   └── MenuSlot.java            # 個別スロットの状態
│   ├── action/
│   │   ├── MenuAction.java          # アクションの基底インターフェース
│   │   ├── KeybindAction.java       # キーバインド実行アクション
│   │   └── ActionRegistry.java      # アクション種別の登録
│   └── render/
│       └── RadialMenuRenderer.java  # 描画処理
└── mixin/
    └── client/
        └── (必要に応じて追加)
```

### 3.2 クラス設計

#### 3.2.1 コア部分

```
┌─────────────────────────────────────────────────────────────┐
│                    KuRadialMenuClient                       │
│  - エントリーポイント                                        │
│  - 各 Manager の初期化                                       │
└─────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│  ConfigManager  │ │ KeybindManager  │ │  ActionRegistry │
│  - load/save    │ │ - キー状態監視   │ │ - アクション登録 │
│  - JSON 永続化  │ │ - メニュー開閉   │ │ - 実行ディスパッチ│
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

#### 3.2.2 メニュー部分

```
┌─────────────────────────────────────────────────────────────┐
│                    RadialMenuScreen                         │
│  - Screen を継承                                            │
│  - マウス入力処理                                            │
│  - 描画委譲                                                  │
└─────────────────────────────────────────────────────────────┘
          │                                     │
          ▼                                     ▼
┌─────────────────────┐           ┌─────────────────────────┐
│     RadialMenu      │           │   RadialMenuRenderer    │
│  - スロット管理      │           │  - 扇形描画             │
│  - 選択判定          │           │  - ハイライト           │
│  - アクション実行    │           │  - ラベル/アイコン描画   │
└─────────────────────┘           └─────────────────────────┘
          │
          ▼
┌─────────────────────┐
│     MenuSlot        │
│  - index            │
│  - action           │
│  - label/icon       │
└─────────────────────┘
```

### 3.3 シーケンス図

#### メニュー表示〜アクション実行

```
User        KeybindManager    RadialMenuScreen    RadialMenu    Action
 │                │                  │                │           │
 │──[R押下]──────▶│                  │                │           │
 │                │──[openMenu()]───▶│                │           │
 │                │                  │──[create()]───▶│           │
 │                │                  │                │           │
 │~~[マウス移動]~~▶│                  │                │           │
 │                │                  │──[updateSelection()]──▶│   │
 │                │                  │◀─[selectedIndex]──────│   │
 │                │                  │                │           │
 │──[R離す]──────▶│                  │                │           │
 │                │──[closeMenu()]──▶│                │           │
 │                │                  │──[executeSelected()]─▶│   │
 │                │                  │                │──[exec()]▶│
 │                │                  │                │           │
```

---

## 4. 実装計画

### 4.1 フェーズ分割

| フェーズ | 内容 | 優先度 |
|---------|------|--------|
| Phase 1 | 基盤構築（設定、キーバインド） | 高 |
| Phase 2 | メニュー UI 基本実装 | 高 |
| Phase 3 | アクション実行機能 | 高 |
| Phase 4 | UI 改善・アニメーション | 中 |
| Phase 5 | コマンド実行機能 | 低 |

### 4.2 Phase 1: 基盤構築

**目標**: 設定管理とキーバインドの基盤を構築

**タスク**:
1. `ModConfig` データクラス作成
2. `ConfigManager` 実装（JSON 読み書き）
3. `ModKeybinds` でメニューキー登録
4. `KeybindManager` でキー状態監視

**成果物**:
- `config/kuradialmenu.json` が生成される
- R キー押下を検出できる

### 4.3 Phase 2: メニュー UI 基本実装

**目標**: ラジアルメニューの表示と選択判定

**タスク**:
1. `RadialMenuScreen` 実装
2. `RadialMenu` ロジック実装
3. `RadialMenuRenderer` 基本描画
4. マウス位置からの選択判定

**成果物**:
- R キー押下でメニュー表示
- マウス方向でハイライト変化
- 中央デッドゾーンで「選択なし」

### 4.4 Phase 3: アクション実行機能

**目標**: キーバインドアクションの実行

**タスク**:
1. `MenuAction` インターフェース定義
2. `KeybindAction` 実装
3. `ActionRegistry` 実装
4. 全 Minecraft キーバインドの列挙と登録

**成果物**:
- メニューから任意のキーバインドを実行可能
- デフォルト設定でよく使う項目が配置済み

---

## 5. 技術的考慮事項

### 5.1 バージョン間差異への対応

| 機能 | 1.19.4 | 1.20.1 | 1.21.1 | 対応方法 |
|------|--------|--------|--------|----------|
| ResourceLocation | `new ResourceLocation()` | 同左 | `ResourceLocation.fromNamespaceAndPath()` | Stonecutter 分岐 |
| Screen#render | `render(MatrixStack, ...)` | 同左 | `render(DrawContext, ...)` | Stonecutter 分岐 |
| KeyBinding | 同一 API | 同一 API | 同一 API | 分岐不要 |

### 5.2 Mixin の使用方針

現時点では Mixin は**最小限**に抑える:
- キーバインド監視: Fabric API の `ClientTickEvents` で対応可能
- 描画: `Screen` 継承で対応可能

将来的に必要になる可能性:
- HUD への常時表示（`HudRenderCallback` で対応可能）

### 5.3 パフォーマンス考慮

- **Tick 処理**: キー状態チェックは `ClientTickEvents.END_CLIENT_TICK` で軽量に
- **描画**: 不要な再計算を避け、選択状態変更時のみ更新
- **設定保存**: 即座に保存せず、画面を閉じたタイミングでバッチ保存

---

## 6. デフォルト設定

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

## 7. ファイル一覧

### 7.1 新規作成ファイル

```
src/main/java/dev/kurowater/kuradialmenu/
├── client/
│   ├── config/
│   │   ├── ModConfig.java
│   │   ├── ConfigManager.java
│   │   └── SlotConfig.java
│   ├── keybind/
│   │   ├── ModKeybinds.java
│   │   └── KeybindManager.java
│   ├── menu/
│   │   ├── RadialMenu.java
│   │   ├── RadialMenuScreen.java
│   │   └── MenuSlot.java
│   ├── action/
│   │   ├── MenuAction.java
│   │   ├── KeybindAction.java
│   │   └── ActionRegistry.java
│   └── render/
│       └── RadialMenuRenderer.java
```

### 7.2 修正ファイル

```
src/main/java/dev/kurowater/kuradialmenu/KuRadialMenuClient.java
  - ConfigManager, KeybindManager, ActionRegistry の初期化追加

src/main/resources/assets/kuradialmenu/lang/en_us.json (新規)
src/main/resources/assets/kuradialmenu/lang/ja_jp.json (新規)
  - キーバインド名、メニューラベルの翻訳
```

---

## 8. 次のステップ

1. **承認**: 本計画の承認をお願いします
2. **Phase 1 実装開始**: 承認後、基盤構築から開始
3. **動作確認**: 各フェーズ完了ごとに動作確認

---

## 9. 承認依頼

上記の開発計画について承認をお願いします。

- [ ] 計画承認
- [ ] 修正要望あり（コメント欄に記載）

**承認いただければ、Phase 1 の実装を開始します。**

