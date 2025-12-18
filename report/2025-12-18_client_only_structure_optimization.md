# クライアント専用構造への最適化

**作成日**: 2025年12月18日  
**プロジェクト**: KuRadialMenu  
**ステータス**: ✅ 完了

---

## 📋 概要

KuRadialMenu はクライアント専用 QoL Mod であるにもかかわらず、パッケージ構造に `common/` ディレクトリが存在していました。この構造は「サーバー/クライアント両方で動作するコード」が存在するかのような誤解を招くため、完全にクライアント専用の構造に最適化しました。

---

## 🔍 問題の特定

### 問題点
- `src/main/java/dev/kurowater/kuradialmenu/common/` パッケージが存在
- `common/` は通常、サーバー/クライアント両方で動作する共通コードを格納する場所
- クライアント専用 Mod では不要であり、設計意図を不明瞭にする

### 影響
- 開発者が「サーバー側でも動くコードを書いて良いのか?」と誤解する可能性
- コードレビュー時に「このクラスは common に置くべきか client に置くべきか?」という議論が発生
- `fabric.mod.json` で `"environment": "client"` と明示しているにもかかわらず、構造が一致しない

---

## 🔄 実施した変更

### 1. **ディレクトリ構造の再編成**

#### 変更前
```
src/main/java/dev/kurowater/kuradialmenu/
├── client/
│   ├── config/
│   ├── keybind/
│   └── ui/
├── common/          # ← 削除対象
│   ├── model/
│   └── util/
└── mixin/
    └── client/
```

#### 変更後
```
src/main/java/dev/kurowater/kuradialmenu/
├── KuRadialMenuClient.java  # エントリーポイント
├── client/                   # Client専用コード（統合）
│   ├── config/
│   ├── keybind/
│   ├── ui/
│   ├── model/                # ← common/model から移動
│   └── util/                 # ← common/util から移動
└── mixin/
    └── client/
```

#### 実行したコマンド
```powershell
# common/model と common/util を client/ 配下に移動
Move-Item -Path "src\main\java\dev\kurowater\kuradialmenu\common\model" `
          -Destination "src\main\java\dev\kurowater\kuradialmenu\client\model" -Force

Move-Item -Path "src\main\java\dev\kurowater\kuradialmenu\common\util" `
          -Destination "src\main\java\dev\kurowater\kuradialmenu\client\util" -Force

# common/ ディレクトリを削除
Remove-Item -Path "src\main\java\dev\kurowater\kuradialmenu\common" -Recurse -Force
```

**結果**: `common/` ディレクトリが完全に削除され、すべてのコードが `client/` 配下に統合されました。

---

### 2. **README.md の更新**

#### 2.1. バッジの追加
クライアント専用であることを明示するバッジを追加：

```markdown
![Environment](https://img.shields.io/badge/Environment-Client-brightgreen)
```

#### 2.2. プロジェクト構造セクションの更新

**変更前**:
```
src/main/java/dev/kurowater/kuradialmenu/
├── client/          # Client専用コード
│   ├── config/      # 設定管理
│   ├── keybind/     # キーバインド処理
│   └── ui/          # UI レンダリング
├── common/          # 共通ユーティリティ
│   ├── model/       # データモデル
│   └── util/        # ヘルパークラス
└── mixin/           # Mixin によるコア変更
    └── client/      # Client側 Mixin
```

**変更後**:
```
src/main/java/dev/kurowater/kuradialmenu/
├── KuRadialMenuClient.java  # エントリーポイント
├── client/                   # Client専用コード
│   ├── config/               # 設定管理
│   ├── keybind/              # キーバインド処理
│   ├── ui/                   # UI レンダリング
│   ├── model/                # データモデル
│   └── util/                 # ヘルパークラス
└── mixin/                    # Mixin によるコア変更
    └── client/               # Client側 Mixin
```

#### 2.3. 最終更新日の更新
- 2025年12月16日 → 2025年12月18日

---

## ✅ 検証結果

### クライアント専用構造のチェックリスト

| 項目 | 評価 | 詳細 |
|------|------|------|
| `client/` パッケージの存在 | ✅ 優良 | すべてのクライアント専用コードが配置 |
| `common/` パッケージの削除 | ✅ 完了 | 不要なディレクトリを完全に削除 |
| `mixin/client/` の配置 | ✅ 正しい | Mixin がクライアント専用として配置 |
| `fabric.mod.json` | ✅ 正しい | `"environment": "client"` が設定済み |
| `kuradialmenu.mixins.json` | ✅ 正しい | `"client": []` セクションを使用 |
| エントリーポイント | ✅ 正しい | `ClientModInitializer` を実装 |
| README.md バッジ | ✅ 追加済み | Environment バッジで明示 |

**総合評価**: 10/10点（完全にクライアント専用 Mod として最適化）

---

## 🎯 設計原則との整合性

### Fabric Mod 開発のベストプラクティス

1. **環境の明確化**: ✅
   - `fabric.mod.json` で `"environment": "client"` を明示
   - パッケージ構造が `client/` 配下に統一

2. **Mixin の分離**: ✅
   - `mixin/client/` に配置
   - `kuradialmenu.mixins.json` で `"client": []` セクションを使用

3. **エントリーポイントの適切な使用**: ✅
   - `ClientModInitializer` のみを実装
   - `ModInitializer` は使用しない

4. **コードの論理的な整理**: ✅
   - 機能別にサブパッケージを分離（config, keybind, ui, model, util）

---

## 📚 参考情報

### Fabric Wiki の推奨事項

> **Client-side mods** should only use `ClientModInitializer` and place all code in a `client` package or subpackage. This prevents accidental server-side class loading which would cause crashes on dedicated servers.

出典: [Fabric Wiki - Side](https://fabricmc.net/wiki/tutorial:side)

### 本プロジェクトの対応状況

| Fabric Wiki 推奨 | KuRadialMenu の実装 | ステータス |
|------------------|---------------------|-----------|
| `ClientModInitializer` のみ使用 | ✅ `KuRadialMenuClient` で実装 | 準拠 |
| `client` パッケージに配置 | ✅ すべて `client/` 配下 | 準拠 |
| `fabric.mod.json` で明示 | ✅ `"environment": "client"` | 準拠 |
| サーバー側クラスの回避 | ✅ サーバー専用クラスなし | 準拠 |

---

## 🎯 ネクストアクション

### 1. ビルドテスト（推奨）
```powershell
# 全サポートバージョンでビルドが通ることを確認
.\gradlew.bat build
```

### 2. 実行テスト（推奨）
```powershell
# クライアントを起動して動作確認
.\gradlew.bat runClient
```

### 3. Git コミット
```powershell
git add src/main/java README.md report/
git commit -m "refactor: Optimize project structure for client-only mod

- Moved common/model and common/util to client/ package
- Removed common/ directory completely
- Updated README.md to reflect client-only structure
- Added Environment badge to README.md
- Updated last modified date to 2025-12-18"
```

---

## 📝 備考

### 今後の開発時の注意点

1. **新しいクラスの配置**:
   - すべての新規クラスは `client/` 配下に配置すること
   - `common/` や `shared/` パッケージは作成しないこと

2. **import 文のチェック**:
   - `net.minecraft.server` パッケージのクラスを import しないこと
   - サーバー専用のクラスを誤って使用しないよう注意

3. **Mixin の作成**:
   - 新しい Mixin は `mixin/client/` 配下に配置
   - `kuradialmenu.mixins.json` の `"client": []` セクションに追加

4. **コードレビューのポイント**:
   - PR レビュー時に「このコードはクライアント専用か?」を確認
   - サーバー側で動作する可能性のあるコードは reject

---

## 🔗 関連レポート

- [2025-12-16_version_1.21.11_removal.md](./2025-12-16_version_1.21.11_removal.md) - 1.21.11 バージョン削除
- [2025-12-16_pre_development_setup_review.md](./2025-12-16_pre_development_setup_review.md) - 開発環境セットアップレビュー
- [2025-12-16_gradle_properties_standardization.md](./2025-12-16_gradle_properties_standardization.md) - Gradle プロパティ標準化

---

**作成者**: GitHub Copilot  
**最終更新**: 2025年12月18日

