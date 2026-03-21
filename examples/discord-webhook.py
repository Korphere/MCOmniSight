# examples/discord-webhook.py
# Usage:
# 1. pip install websockets requests
# 2. python discord-webhook.py

import asyncio
import websockets
import json
import requests
import datetime

# --- Configuration ---
MCOMNISIGHT_URL = "ws://localhost:8887" # MCOmniSight Server URL
API_KEY = "your-secret-key-here" # MCOmniSight API Key
DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN" # Discord Webhook URL

async def watch_prelogin_events():
    url = f"{MCOMNISIGHT_URL}/?key={API_KEY}"

    print("Connecting to MCOmniSight...")

    async with websockets.connect(url) as websocket:
        print("Connected to MCOmniSight. Watching for PreLogin events...")

        while True:
            try:
                # 1. Receive data
                data = await websocket.recv()
                event = json.loads(data)

                # 2. Filter for PLAYER_PRE_LOGIN event
                if event.get("type") == "EVENT" and event.get("event_type") == "PLAYER_PRE_LOGIN":
                    payload = event.get("payload", {})

                    print(f"PreLogin detected: {payload.get('player')} ({payload.get('result')})")

                    # 3. Prepare Discord Embed
                    embed = {
                        "title": "Player PreLogin Attempt (Python)",
                        "color": 0x2ecc71 if payload.get("result") == "ALLOWED" else 0xe74c3c, # Green for Allowed, Red for Kicked
                        "fields": [
                            { "name": "Player Name", "value": payload.get("player"), "inline": True },
                            { "name": "UUID", "value": f"`{payload.get('uuid')}`", "inline": True },
                            { "name": "IP Address", "value": f"`{payload.get('ip_address')}`", "inline": False },
                            { "name": "Login Result", "value": payload.get("result"), "inline": True }
                        ],
                        "footer": { "text": "MCOmniSight Python Integration" },
                        "timestamp": datetime.datetime.utcnow().isoformat()
                    }

                    # 4. Send to Discord Webhook
                    send_discord_webhook({ "embeds": [embed] })

            except websockets.exceptions.ConnectionClosed:
                print("Connection to MCOmniSight closed.")
                break
            except Exception as e:
                print(f"Error: {e}")

def send_discord_webhook(body):
    try:
        response = requests.post(
            DISCORD_WEBHOOK_URL,
            data=json.dumps(body),
            headers={"Content-Type": "application/json"}
        )
        if not response.ok:
            print(f"Discord Webhook failed: {response.status_code} {response.text}")
    except Exception as e:
        print(f"Error sending Discord webhook: {e}")

if __name__ == "__main__":
    asyncio.run(watch_prelogin_events())