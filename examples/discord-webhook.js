// examples/discord-webhook.js
// Usage:
// 1. npm install ws node-fetch
// 2. node discord-webhook.js

const WebSocket = require('ws');
const fetch = require('node-fetch');

// --- Configuration ---
const MCOMNISIGHT_URL = 'ws://localhost:8887'; // MCOmniSight Server URL
const API_KEY = 'your-secret-key-here'; // MCOmniSight API Key
const DISCORD_WEBHOOK_URL = 'https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN'; // Discord Webhook URL

// --- Initialize WebSocket ---
const ws = new WebSocket(`${MCOMNISIGHT_URL}/?key=${API_KEY}`);

console.log('Connecting to MCOmniSight...');

ws.on('open', () => {
    console.log('Connected to MCOmniSight. Watching for PreLogin events...');
});

ws.on('message', async (data) => {
    try {
        const event = JSON.parse(data);

        // 1. Filter for PLAYER_PRE_LOGIN event
        if (event.type === 'EVENT' && event.event_type === 'PLAYER_PRE_LOGIN') {
            const payload = event.payload;

            console.log(`PreLogin detected: ${payload.player} (${payload.result})`);

            // 2. Prepare Discord Embed
            const embed = {
                title: 'Player PreLogin Attempt',
                color: payload.result === 'ALLOWED' ? 0x2ecc71 : 0xe74c3c, // Green for Allowed, Red for Kicked
                fields: [
                    { name: 'Player Name', value: payload.player, inline: true },
                    { name: 'UUID', value: `\`${payload.uuid}\``, inline: true },
                    { name: 'IP Address', value: `\`${payload.ip_address}\``, inline: false },
                    { name: 'Login Result', value: payload.result, inline: true }
                ],
                footer: { text: 'MCOmniSight Integration' },
                timestamp: new Date()
            };

            // 3. Send to Discord Webhook
            await sendDiscordWebhook({ embeds: [embed] });
        }
    } catch (error) {
        console.error('Error processing message:', error);
    }
});

ws.on('close', () => {
    console.log('Disconnected from MCOmniSight.');
});

ws.on('error', (error) => {
    console.error('WebSocket Error:', error);
});

// --- Send Discord Webhook ---
async function sendDiscordWebhook(body) {
    try {
        const response = await fetch(DISCORD_WEBHOOK_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            console.error(`Discord Webhook failed: ${response.status} ${response.statusText}`);
        }
    } catch (error) {
        console.error('Error sending Discord webhook:', error);
    }
}