# KuRadialMenu

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.19.4%20|%201.20.1%20|%201.21.1-green)
![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

**KuRadialMenu** は Minecraft に直感的なラジアルメニューUIを追加するクライアントサイド QoL Mod です。

## ✨ 特徴

- 🎯 **直感的な操作**: マウス操作による円形メニューで素早くアクションを実行
- 🎨 **カスタマイズ可能**: メニュー項目やキーバインドを自由に設定
- 🌐 **マルチバージョン対応**: 1.19.4 から 1.21.1 まで幅広いバージョンをサポート
- 🪶 **軽量設計**: Client 専用で動作し、サーバーへの影響なし

## 📦 インストール方法

### 前提条件
- [Fabric Loader](https://fabricmc.net/use/) 0.16.0 以上
- [Fabric API](https://modrinth.com/mod/fabric-api)

### 手順
1. [Releases](https://github.com/kuro-water/KuRadialMenu/releases) から対応バージョンの `.jar` ファイルをダウンロード
2. ダウンロードしたファイルを `.minecraft/mods/` フォルダに配置
3. Minecraft を起動してプレイ

## 🎮 使用方法

### 基本操作
1. **メニューを開く**: デフォルトのキーバインド（設定可能）を押す
2. **項目を選択**: マウスカーソルを目的の項目に合わせる
3. **実行**: クリックまたはキーを離す

### 設定
- Minecraft の設定画面から **Options > Controls > Key Binds** で変更可能
- 詳細な設定は `.minecraft/config/kuradialmenu.json` で調整

## 🛠️ 開発環境のセットアップ

### 必要な環境
- **Java**: 21 (1.21.1) または 17 (1.19.4, 1.20.1)
- **IDE**: IntelliJ IDEA 推奨
- **ビルドツール**: Gradle 9.2.1+

### セットアップ手順

```powershell
# 1. リポジトリをクローン
git clone https://github.com/kuro-water/KuRadialMenu.git
cd KuRadialMenu

# 2. Java 21 がインストールされていることを確認
java -version  # "21.x.x" が表示されること

# 3. 依存関係をダウンロードしてビルド
.\gradlew.bat build

# 4. 開発環境でクライアントを起動
.\gradlew.bat runClient
```

### Stonecutter によるバージョン切り替え

```powershell
# 特定のバージョンをアクティブにする
.\gradlew.bat "Set active project to 1.21.1"

# 全バージョンをビルドして libs/ に収集
.\gradlew.bat buildAndCollect
```

### プロジェクト構造

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

## 🤝 コントリビューション

バグ報告や機能提案は [Issues](https://github.com/kuro-water/KuRadialMenu/issues) へお願いします。

プルリクエストも歓迎します！以下の手順でお願いします:

1. このリポジトリをフォーク
2. 機能ブランチを作成 (`git checkout -b feature/amazing-feature`)
3. 変更をコミット (`git commit -m 'Add amazing feature'`)
4. ブランチをプッシュ (`git push origin feature/amazing-feature`)
5. プルリクエストを開く

### コーディング規約
- `.editorconfig` の設定に従う
- Java コードは 4 スペースインデント
- コミットメッセージは日本語または英語で明確に記述

## 📄 ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。詳細は [LICENSE](LICENSE) ファイルを参照してください。

## 🔗 リンク

- **GitHub**: [https://github.com/kuro-water/KuRadialMenu](https://github.com/kuro-water/KuRadialMenu)
- **Issues**: [https://github.com/kuro-water/KuRadialMenu/issues](https://github.com/kuro-water/KuRadialMenu/issues)
- **Fabric Discord**: [https://discord.gg/v6v4pMv](https://discord.gg/v6v4pMv)

## 📚 参考資料

- [Fabric Wiki](https://fabricmc.net/wiki/)
- [Stonecutter Documentation](https://stonecutter.kikugie.dev/wiki/)
- [Minecraft Development Plugin](https://plugins.jetbrains.com/plugin/8327-minecraft-development)

---

**開発者**: kuro-water  
**バージョン**: 0.1.0  
**最終更新**: 2025年12月16日

