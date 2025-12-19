# Yarn マッピング移行計画

**作成日**: 2025-12-19  
**ステータス**: ✅ 完了 (2025-12-19)  
**対象**: KuRadialMenu Fabric Mod プロジェクト全バージョン（1.19.4, 1.20.1, 1.21.1）  
**完了レポート**: [2025-12-19_yarn_mappings_migration_completed.md](./2025-12-19_yarn_mappings_migration_completed.md)

---

## 📋 概要

現在、本プロジェクトは **Mojang 公式マッピング (Official Mojang Mappings)** を使用しているが、プロジェクト指示書では **Yarn マッピング** の使用が規定されている。本レポートでは、両マッピングの特性を比較し、Yarn への移行計画を提示する。

---

## 🔍 現状分析

### 使用中のマッピング設定

**`build.gradle.kts` L51:**
```kotlin
mappings(loom.officialMojangMappings())
```

**各バージョンの `gradle.properties` には Yarn バージョンが定義済み:**
- 1.19.4: `deps.yarn_mappings=1.19.4+build.65`
- 1.20.1: `deps.yarn_mappings=1.20.1+build.10`
- 1.21.1: `deps.yarn_mappings=1.21.1+build.3`

→ **設定は準備されているが、実際には使用されていない状態**

### 現在のコード規模
- Java ファイル: `KuRadialMenuClient.java` のみ（42行）
- Minecraft API 使用箇所: `ResourceLocation` のみ
- Mixin: なし
- 複雑な Minecraft 内部クラスへのアクセス: なし

**結論: 現時点では移行コストは極めて低い**

---

## 📊 公式マッピング vs Yarn マッピング 比較

### 1️⃣ Mojang 公式マッピング (Official Mappings)

#### ✅ メリット（このプロジェクトにおける）

| 項目 | 詳細 |
|------|------|
| **完全性** | Mojang が公式に提供するため、全てのクラス・メソッド・フィールドが網羅されている |
| **正式名称** | Mojang が実際に使用している名称で統一されており、デコンパイル後のコードが意図を反映 |
| **安定性** | Minecraft バージョンごとに確定した名称が提供され、変更がない |
| **ツールサポート** | Forge など他の Mod Loader との共通理解が得られやすい（将来的な移植性） |
| **学習曲線** | Vanilla の Decompile 結果と一致するため、デバッグ時の混乱が少ない |
| **Zero Configuration** | Loom が自動的にダウンロード・適用するため、追加設定不要 |

#### ❌ デメリット（このプロジェクトにおける）

| 項目 | 詳細 |
|------|------|
| **難読化名の混入** | 一部のローカル変数や内部クラスに `f_xxxxx_` や `m_xxxxx_` のような難読化済み名称が残る |
| **可読性の低下** | 特に複雑な内部処理（レンダリング、ネットワーク）で意味不明な変数名が出現する |
| **コミュニティとの乖離** | Fabric コミュニティの大半は Yarn を使用しているため、サンプルコードやドキュメントとの対応が取りにくい |
| **ライセンス制約** | Mojang の利用規約に縛られる（再配布・改変時の明記義務） |
| **バージョン間差異** | Minecraft バージョンアップ時に公式が名称を変更する場合がある（破壊的変更） |

---

### 2️⃣ Yarn マッピング

#### ✅ メリット（このプロジェクトにおける）

| 項目 | 詳細 |
|------|------|
| **完全な人間可読性** | コミュニティが全ての難読化名を手動で解読し、意味のある名前を付けている |
| **Fabric 標準** | Fabric API、Fabric Loader、全ての主要 Fabric Mod が Yarn を使用 |
| **ドキュメント・サンプルとの一致** | Fabric Wiki、GitHub の Mod サンプル、Discord コミュニティの全てが Yarn 前提 |
| **継続的改善** | コミュニティがバージョンごとに名称を洗練させており、バグ修正や改名提案が活発 |
| **オープンライセンス** | CC0（パブリックドメイン相当）で提供され、商用・非商用問わず自由に使用可能 |
| **デバッグ効率** | スタックトレースやデコンパイル結果が全て人間可読のため、問題の特定が迅速 |
| **Fabric Loom 最適化** | Fabric Loom は Yarn を第一級サポートしており、キャッシュや依存解決が高速 |

#### ❌ デメリット（このプロジェクトにおける）

| 項目 | 詳細 |
|------|------|
| **初期学習コスト** | Vanilla Decompile とは異なる名称のため、最初は混乱する可能性がある |
| **バージョン間の名称変更** | コミュニティの改善により、同じ要素が異なるバージョンで異なる名前になる場合がある |
| **完全性の遅延** | 新しい Minecraft バージョンがリリースされてから Yarn が完成するまで数週間かかる |
| **Forge との非互換** | Forge は MCP/Parchment を使用しているため、将来的な Forge 移植時に全面的な書き直しが必要 |
| **マイナーバージョンでの不完全性** | スナップショット対応版や古いバージョンでは一部の要素が未解決の場合がある |

---

## 🎯 このプロジェクトにおける推奨結論

### **Yarn マッピングへの移行を強く推奨**

#### 理由

1. **プロジェクト指示書の遵守**
   - `.github/instructions/project.instructions.md` で明示的に "Yarn mappings 使用" が規定されている
   - Client Mod の QoL 改善が目的であり、Fabric エコシステムとの統合が最優先

2. **開発効率の最大化**
   - Fabric コミュニティの全リソース（Wiki、GitHub、Discord）が Yarn 前提で書かれている
   - サンプルコードをコピー&ペーストして即座に動作する
   - Fabric API の javadoc も Yarn 名称で記載されている

3. **保守性の向上**
   - 将来的に Mixin を使用する際、Yarn のほうが対象メソッドの特定が容易
   - チーム開発やコミュニティからの PR を受け入れる際、標準に従うことで混乱を防ぐ

4. **移行コストの低さ**
   - 現在のコード量は極めて少なく（1ファイル、42行）
   - 使用している API は `ResourceLocation` のみで、Yarn でも同じクラス名
   - 破壊的変更のリスクがほぼゼロ

5. **長期的なメリット**
   - Stonecutter で複数バージョンを管理する際、Yarn のバージョン間整合性が高い
   - 将来的な機能拡張（インベントリ操作、HUD 改善、ネットワーク通信）で Yarn のメリットが顕著化

#### 公式マッピングを使い続けるべきケース（本プロジェクトには該当しない）

- Forge への移植を視野に入れている
- Mojang の内部実装を厳密にトレースする必要がある
- Yarn の更新を待てない最新スナップショット対応が必要

---

## 🛠️ 移行計画

### フェーズ 1: 設定変更（所要時間: 5分）

#### ステップ 1.1: `build.gradle.kts` の修正

**変更箇所:** L51

**変更前:**
```kotlin
mappings(loom.officialMojangMappings())
```

**変更後:**
```kotlin
mappings("net.fabricmc:yarn:${property("deps.yarn_mappings")}:v2")
```

**説明:**
- `yarn:バージョン:v2` の `:v2` は Yarn マッピングの最新フォーマット（intermediary mappings を含む）
- `property("deps.yarn_mappings")` は各バージョンの `gradle.properties` から自動取得される

---

### フェーズ 2: Gradle キャッシュのクリアと再構築（所要時間: 10-15分）

#### ステップ 2.1: Gradle キャッシュのクリア

**PowerShell コマンド:**
```powershell
.\gradlew clean cleanCache --refresh-dependencies
```

#### ステップ 2.2: 再ビルドと検証

```powershell
.\gradlew chiseledBuild
```

**期待される動作:**
- Yarn マッピングのダウンロードが開始される
- 既存の公式マッピングキャッシュが削除される
- 全バージョン（1.19.4, 1.20.1, 1.21.1）でビルドが成功する

---

### フェーズ 3: コードの検証と調整（所要時間: 5-10分）

#### ステップ 3.1: IDE の再インデックス

**IntelliJ IDEA 操作:**
1. `File` → `Invalidate Caches...`
2. `Invalidate and Restart` を選択
3. 再起動後、`File` → `Sync Project with Gradle Files`

#### ステップ 3.2: コードの検証

**対象ファイル:** `KuRadialMenuClient.java`

**検証ポイント:**
- `ResourceLocation` のインポートが正常か
- `ClientModInitializer` のインポートが正常か
- コンパイルエラーが発生していないか

**予想される結果:**
- **変更不要** - `ResourceLocation` は Yarn でも同じクラス名のため、既存コードがそのまま動作する

#### ステップ 3.3: 実行テスト

```powershell
.\gradlew :1.20.1:runClient
```

**検証項目:**
- Minecraft が正常に起動するか
- ログに `Hello Fabric world!` が出力されるか
- エラー・警告が発生していないか

---

### フェーズ 4: ドキュメント更新（所要時間: 5分）

#### ステップ 4.1: README.md への記載

**追加内容:**
```markdown
## 開発環境

- Minecraft: 1.19.4, 1.20.1, 1.21.1
- Mod Loader: Fabric (Stonecutter)
- Mappings: **Yarn** (Fabric 標準)
- Java: 17 (1.19.4, 1.20.1), 21 (1.21.1)
```

#### ステップ 4.2: このレポートのステータス更新

移行完了後、このファイルの先頭を以下に変更:
```markdown
**ステータス**: ✅ 完了 (2025-12-19)
```

---

## 🚀 次のアクション

### 承認いただきたい事項

1. **Yarn マッピングへの移行実施** - 上記フェーズ 1-4 の実行
2. **既存の `gradle.properties` の Yarn バージョン維持** - 現在定義されているバージョンをそのまま使用

### 承認後のタスク

1. `build.gradle.kts` の修正
2. Gradle キャッシュのクリアと再ビルド
3. 全バージョンでの動作確認
4. レポートステータスの更新

### 懸念事項・リスク評価

| リスク | 影響度 | 対策 |
|--------|--------|------|
| ビルド失敗 | 低 | 既存の公式マッピングに戻すだけで復旧可能（1行の変更） |
| コード変更が必要 | 極低 | 現在の API 使用状況から判断して、変更不要と予測 |
| 開発時間の増加 | なし | むしろ Fabric ドキュメントとの整合性で効率化 |

---

## 📚 参考リンク

- [Yarn Mappings 公式リポジトリ](https://github.com/FabricMC/yarn)
- [Fabric Loom ドキュメント](https://fabricmc.net/wiki/documentation:fabric_loom)
- [Mappings の選び方（Fabric Wiki）](https://fabricmc.net/wiki/tutorial:mappings)

---

## 結論

**本プロジェクトにおいて、Yarn マッピングへの移行は必須であり、かつ低リスク・高リターンの改善である。**

現在のコード規模と Fabric エコシステムへの依存度を考慮すると、公式マッピングを使い続ける理由は存在しない。移行を即座に実施することを推奨する。

**承認いただければ、直ちに変更を適用します。**

