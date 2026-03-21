# MCOmniSight API ガイド (開発者・データ利用側向け)

MCOmniSight の WebSocket からデータを取得し、外部ツールを作成するためのガイドです。

## 1. WebSocket 接続
以下の形式でエンドポイントに接続します。

- **URL**: `ws://[サーバーIP]:[ポート]/?key=[YOUR_API_KEY]`
- **データ形式**: JSON (UTF-8)

## 2. データ型 (Message Types)
受信する全ての JSON データには `type` フィールドが含まれます。

### A. FULL_DATA
接続成功直後に送信される、現在のサーバーの完全なスナップショットです。
- `type`: "FULL_DATA"
- `data`: サーバーの基本情報、現在の全プレイヤーリスト、OS 統計の初期値。

### B. STATUS
設定されたインターバル（デフォルト 1 秒）ごとに送信される動的データです。
- `type`: "STATUS"
- `features`:
    - `performance`: TPS, MSPT, Memory 使用率。
    - `host_status`: CPU Load, Disk I/O, Network 通信量。

### C. EVENT
サーバー内で特定の動きがあった瞬間に送信されるリアルタイム通知です。
- `type`: "EVENT"
- `event_type`: イベントの種類（例: `PLAYER_CHAT`, `PLAYER_JOIN`, `PLAYER_PRE_LOGIN`）
- `payload`: 各イベント固有のデータ（プレイヤー名、UUID、メッセージ内容等）

## 3. 実装上の注意点
- **再接続処理**: サーバーのリロード等で接続が切断された場合、クライアント側で自動再接続（Exponential Backoff 推奨）を行ってください。
- **データのフィルタリング**: 受信データには膨大な情報が含まれる場合があります。必要なフィールドのみを抽出して処理してください。
- **Gzip 圧縮**: プラグイン設定で `gzip-enabled: true` の場合、バイナリ形式でデータが届くことがあります。クライアント側での解凍処理が必要です。