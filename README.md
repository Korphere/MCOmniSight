# MCOmniSight

MCOmniSightは、Minecraftサーバー（Paper）のリアルタイムステータスをWebSocket/TCP経由で配信する高機能プラグインです。
サーバーの負荷だけでなく、OSレイヤーの詳細な情報を収集・配信します。
MCOmniSight is a high-performance plugin that streams real-time status data for Minecraft servers (Paper) via WebSocket/TCP.
It collects and streams not only server load data but also detailed information at the OS level.

## ✨ 主な機能 / Key Features
- **Real-time Monitoring**: WebSocket (ws/wss) による低遅延なデータ配信。
- **Hardware Metrics**: OSHIを使用したCPU/GPU/Network/Diskの詳細な統計。
- **Event Streaming**: チャット、参加、プレログイン等のイベントをリアルタイム通知。
- **High Performance**: Gzip圧縮対応、差分データ送信による通信量の最適化。
- **Customizable**: `config.yml` ですべての配信項目を制御可能。

## 🚀 クイックスタート
1. `plugins` フォルダに `MCOmniSight-x.x.x.jar` を配置。
2. サーバーを起動し、生成された `config.yml` の `api-key` を設定。
3. `ws://[Your-IP]:8887/?key=[Your-API-Key]` に接続。

## 📖 ドキュメント
- [サーバー管理者向けガイド (インストール・設定)](USAGE_ADMIN.md)
- [開発者向けガイド (API・データ構造)](USAGE_DEV.md)