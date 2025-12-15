# Stonecutter Fabric テンプレート
## セットアップ
1. `settings.gradle.kts` でサポートされている Minecraft バージョンを確認します。
   新しいエントリについては、他のバージョンと同じキーを持つ `versions/.../gradle.properties` を追加します。
2. `gradle.properties` の `mod.group`、`mod.id`、`mod.name` プロパティを変更します。
3. `src/main/java` の `com.example` パッケージの名前を変更します。
4. `src/main/resources/template.mixins.json` を mod の ID を使用するように名前変更します。
5. `LICENSE` ファイルを確認します。
   一般的なオプションについては、[ライセンス決定図](https://docs.codeberg.org/getting-started/licensing/#license-decision-diagram)を参照してください。
6. `src/main/resources/fabric.mod.json` を最新のプロパティを持つように確認します。

## 使用方法
- `"Set active project to ..."` Gradle タスクを使用して、`src/` クラスで利用可能な Minecraft バージョンを更新します。
- `buildAndCollect` Gradle タスクを使用して、mod リリースを `build/libs/` に保存します。
- `stonecutter.gradle.kts` と `build.gradle.kts` で `mod-publish-plugin` を有効にし、
  対応するコードブロックを使用して、Modrinth と Curseforge にリリースを公開します。
- `build.gradle.kts` で `maven-publish` を有効にし、対応するコードブロックを使用して、
  個人用 Maven リポジトリにリリースを公開します。

## 便利なリンク
- [Stonecutter 初心者ガイド](https://stonecutter.kikugie.dev/wiki/start/): *ネタバレ: 必要があります* ***理解する*** *それがどのように機能するかを！*
- [Fabric Discord サーバー](https://discord.gg/v6v4pMv): mod 開発ヘルプ用。
- [Stonecutter Discord サーバー](https://discord.kikugie.dev/): Stonecutter および Gradle ヘルプ用。
- [質問の仕方 - ガイド](http://www.catb.org/esr/faqs/smart-questions.html): [ビデオ形式](https://www.youtube.com/results?search_query=How+To+Ask+Questions+The+Smart+Way)もあります。
