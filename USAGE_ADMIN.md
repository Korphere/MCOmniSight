# MCOmniSight 使用ガイド (サーバー管理者向け)

MCOmniSight を導入し、正しく動作させるための手順を解説します。

## 1. インストール
1. `MCOmniSight-x.x.x.jar` をサーバーの `plugins` フォルダに配置します。
2. サーバーを起動します。
3. `plugins/MCOmniSight/config.yml` が生成されたことを確認してください。

## 2. 重要設定 (config.yml)
サーバーのセキュリティを守るため、以下の項目を必ず設定してください。

- **api-key**:
    - 外部からの接続を認証するためのパスワードです。
    - **必ずデフォルト値から変更してください。**
- **websocket-port**:
    - WebSocket サーバーが使用するポート（デフォルト: 8887）です。
    - 他のプラグイン（Dynmap 等）や OS のポートと重複しないようにしてください。
- **connection-mode**:
    - `DIRECT`: 自宅サーバーなどでポートを直接開放する場合。
    - `PROXY`: Nginx 等のリバースプロキシを通す場合。
    - `TUNNEL`: Cloudflare Tunnel 等を使用する場合。

## 3. セキュリティ設定
特定の Web サーバー（ダッシュボード）からのみ接続を許可する場合は、ホワイトリストを有効にしてください。

```yaml
whitelist:
  enabled: true
  allowed-ips:
    - "127.0.0.1"
    - "あなたのWebサーバーのIP"
```

## 4. 管理コマンド
ゲーム内またはコンソールで以下のコマンドを使用できます。

`/omnisight reload`: config.yml を再読み込みします（接続中のクライアントは切断されます）。

`/omnisight status`: 現在の接続数などの統計を表示します。
