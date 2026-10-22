# 仕様と設計判断

この文書は継続開発に必要な仕様と判断理由をまとめる。進捗は[PROGRESS](../PROGRESS.md)、実施済みの検証は[VALIDATION](VALIDATION.md)、利用手順と依存バージョン表は[README](../README.md)を参照する。

## 対象と完成条件

Fabricクライアント専用のラジアルメニューMOD。対象はMinecraft **1.19.4 / 1.20.1 / 1.21.1**の3版で、連続したバージョン範囲ではない。1.21.11は対象外。`versions/1.21.11/` に古いプロパティが残るが、settingsのビルド対象に含まれておらず、対応済みの根拠にはならない。

1. Rホールドでメニューを開き、マウス方向で選択し、解除時に1回実行する。中央・Esc・フォーカス喪失・切断時は誤実行しない。クリック確定は仕様に含めない。
2. 上をスロット1として時計回りに4/8/12項目を並べる。中央には選択無効範囲を設ける。メニューキーはMinecraftの操作設定でキーボード/マウスへ再割当できる。
3. メニュー中も移動・ジャンプ・スニーク・スプリントを使用でき、閉じた後に合成入力が残らない。
4. Kまたは任意導入のMod MenuからYACL設定を開ける。操作名・カテゴリ・IDで割当先を検索でき、保存と取消が正しく機能する。日英表示を確認する。
5. JSON永続化で不正値・破損・保存失敗に対応し、元データを黙って失わない。
6. 3版でビルドと回帰テストが成功し、生成jarでVALIDATIONの操作チェックを完了する。利用方法・制約を説明し、実際に導入可能な成果物を用意する。

コマンド文字列実行・マクロは将来機能。アニメーション・効果音は任意で、今回の完成条件を増やす理由にはしない。

## 構成

Javaの基点は `src/main/java/dev/kurowater/kuradialmenu/`。

| 場所 | 役割 |
|---|---|
| KuRadialMenuClient | 設定とキー処理を初期化するクライアント入口 |
| client/config | ModConfigの検証、ConfigFileStoreのGson/ファイルI/O、ConfigHandlerの回復方針、YACL設定画面 |
| client/keybind | キー登録、物理状態の取得、状態遷移、移動同期、アクションの合成入力 |
| client/model | スロット・選択状態・アクション |
| client/ui | メニュー描画、検索付き割当画面、保存エラー通知 |
| client/util | Minecraft非依存の角度・距離・選択計算 |
| client/compat | 任意のMod Menu連携 |
| mixin/client | KeyBindingの合成押下とpressed状態への最小限の接続 |

専用サーバーのエントリーポイント・独自の通信プロトコルは追加しない。選択された操作自体は通常のMinecraft操作としてサーバーへ作用し得る。

## 入力とアクション

- キーを物理状態で監視するのは、Screen表示中にMinecraftの通常の押下状態が変化するため。`isPressed()` は状態取得、`wasPressed()` はイベント消費であり、同一ではない。
- メニュー中の移動キーのみ同期する。全イベントをそのまま通す方式は、メニュー操作中の攻撃・使用の誤発火を招くため採用しない。
- メニューを閉じ、選択アクションを実行する順序を守る。アクションが開いた画面を後から閉じない。
- 合成イベントは実入力キューと分離する。移動・使用・Tab等の継続アクションは1ゲームティックのパルスで、無期限保持/トグルにはしない。通常の実キー移動とは別に扱う。
- スクリーンショットと全画面切替は通常のKeyBinding消費だけでは発火しないため専用処理を持つ。他MODの生キーボードイベント、F3組合せ等は対応範囲外。
- フォーカス喪失/ワールド変更をキャンセルし、キーを離してから再び開く。短い押下・HiDPI・トグル操作の実挙動は実機検証対象。

## 設定と互換性

YACLは画面構築に使用し、永続化はGsonによる明示的なI/Oで行う。旧計画のYACL自動シリアライズ案は採用していない。JSONはフラットな `slotCount`、`slotActions`、半径・色の各フィールドで、旧計画の `slots/action/type` 形式ではない。開閉キーはMinecraftの操作設定が管理する。

- 初期8項目: inventory / swapOffhand / drop / togglePerspective / command / screenshot / advancements / playerlist（各 `key.` ID）。
- nullや不正数値を補正し、未知のキーIDは保持する。スロット数を減らして保存した場合は範囲外の割当を削除する。
- YACLのpending値を使い、検索画面で選択しただけでは保存しない。古いYACL 3.0.3と共通のBuilder APIを用いる。未翻訳キーは安定IDを表示する。旧版ライブラリ自身の翻訳不足とMODの翻訳不足を区別して実機確認する。
- JSONの構文・最大1MiB・最大深さ32を検証する。不正JSONの原本は起動時上書きせず、一意なバックアップを作れた場合だけ明示保存で回復する。
- 保存は同じフォルダの一時ファイルをflushして置換する。原子的move非対応時には通常の置換moveへフォールバックし、既存ファイルを直接切り詰めない。
- 読み取り/退避失敗時は保存を禁止する。修復して再起動が必要。通常の書込み失敗は通知画面から再試行できる。

## マルチバージョンと描画

Fabric Loom + Yarn mappings + Stonecutterを使用する。Mojang公式名を混ぜない。Javaの実行要件は旧2版17、1.21.1は21。ビルド環境は完全なJDK 21。現在の管理ソース版は1.21.1。

| 差分 | 1.19.4 | 1.20.1 | 1.21.1 |
|---|---|---|---|
| 画面描画 | MatrixStack | DrawContext | DrawContext |
| バッファ | getBuffer / begin / next | 同左 | Tessellator.begin / next不要 |
| Identifier | コンストラクタ | コンストラクタ | Identifier.of |

- バージョン差はソースの `//? if` 等のStonecutter分岐に限定する。古い指示のJava実行時 `Stonecutter.getCurrent()` 方針は採用しない。
- 円弧はTRIANGLE_STRIP、境界はDEBUG_LINES、POSITION_COLORを使う。矩形fillへ任意頂点を渡す旧実装へ戻さない。
- 描画コンテキストの行列を使用し、ブレンド・深度・シェーダーを適切に設定する。形状を描いた後に文字を描き、文字描画による状態変更を後続の形状へ漏らさない。
- 1.21.1のぼかし対策はRadialMenuScreen.renderBackgroundの空実装。super.renderを削除した旧試案は採用しない。非ポーズのメニューとして通常ウィジェットの処理を維持する。
- 依存関係の具体値は `versions/<version>/gradle.properties`、対象版は `settings.gradle.kts` が正。Fabric LoaderとFabric APIを混同せず、`deps.fabric_loader` と `deps.fabric_api` を分ける。重複する `fabric_version` を再導入しない。
- YACL 1.19.4のMaven座標は `dev.isxander.yacl:yet-another-config-lib-fabric`、新2版は `dev.isxander:yet-another-config-lib`。単純に統一しない。

## 文書統合の履歴

2026-10-21に旧report 12件と旧指示書2件を現行6文書へ統合した。元資料はGit履歴の整理前コミット `28cf3c3c3ccb7d0c67ec445fed821176c6754b13` から参照できる。

| 元資料の分類 | 残した情報 |
|---|---|
| 初期準備・client構造・Yarn移行 | クライアント専用境界、対応版、mapping、ビルド方式 |
| Gradleプロパティの不具合/標準化 | Loader/APIの区別、依存定義の所在、JDK要件 |
| 1.21.11除外 | 対象3版を維持する方針 |
| 開発計画2件 | 完成条件、設定UI、入力改善、任意/将来機能 |
| 描画・ぼかし報告 | 採用した描画方式とバージョン差、退行を避ける判断理由 |
| 指示書とそのレビュー | AGENTSの自律開発・保存・検証・確認ルール |

承認待ちの重複、廃案コード、当時の個人環境、過去の実機確認済み宣言は現状へ引き継がない。仕様変更はこの文書、検証証拠はVALIDATIONを更新する。
