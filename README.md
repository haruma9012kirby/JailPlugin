# JailPlugin

このプラグインは、Minecraft サーバー上で囚人システムを実装するための Bukkit/Spigot/PaperMC プラグインです。

## 機能

- プレイヤーの投獄
- プレイヤーの釈放
- 監獄の設定/削除
- 釈放地点の設定

## 使用方法

1. プラグインを PaperMC サーバーの `plugins` フォルダに配置します。
2. サーバーを再起動するか、プラグインをリロードします。
3. 以下のコマンドを使用して囚人システムを管理します：

   - `/jail <プレイヤー名> <監獄名> <刑期> <アドベンチャーモード(true/false)>` - プレイヤーを投獄します
   - `/unjail <プレイヤー名> <仮釈放(入力しない場合釈放)(期間)>` - プレイヤーを釈放します
   - `/setjail <監獄名>` - 監獄の位置を設定します
   - `/setunjail <監獄名>` - 釈放地点を設定します
   - `/removejail <監獄名>` - 監獄を削除します
   - `/jaillist` - 監獄一覧を表示します

   (その他は自分で確認してください。)

## API

- PaperMC 1.21.1-R0.1-SNAPSHOT
- SQLite JDBC 3.46.1.3

## ライセンス

このプラグインは [MIT ライセンス](LICENSE.head) の下で公開されています。

## 作者

Soryu-haruma (Asl, haruma9012kirby)

## サポート

問題や提案がある場合は、GitHub の Issue トラッカーを使用してください。

`Copyright (c) 2024 Soryu-haruma (Asl, haruma9012kirby)`
