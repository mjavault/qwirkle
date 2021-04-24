class WebSocketClient {
    constructor() {
        this.ws = null
        this.onMessage = null;
        this.onConnect = null;
        this.autoReconnect = false;
        this.debug = false;
    }

    enableDebug() {
        this.debug = true;
    }

    connect() {
        this.autoReconnect = true;
        const url = new URL('ws/game', window.location.href);
        url.protocol = url.protocol.replace('http', 'ws');
        this.ws = new WebSocket(url.href);

        this.ws.onopen = (event) => {
            console.log("[open] Connection established");
            if (this.onConnect) this.onConnect();
        };

        this.ws.onmessage = (event) => {
            if (this.onMessage) {
                const body = JSON.parse(event.data);
                if (this.debug) {
                    console.log(body);
                }
                this.onMessage(body);
            }
        };

        this.ws.onclose = (event) => {
            if (event.wasClean) {
                console.log(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
            } else {
                console.log('[close] Connection died');
            }
            if (this.autoReconnect) {
                setTimeout(() => this.connect(), 5000);
                console.log('Reconnecting in 5 seconds');
            }
        };

        this.ws.onerror = (error) => {
            console.log('Error: ' + error);
            if (this.autoReconnect) {
                setTimeout(() => this.connect(), 5000);
                console.log('Reconnecting in 5 seconds');
            }
        };
    }

    disconnect() {
        this.autoReconnect = false;
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }

    send(topic, data) {
        if (this.debug) {
            console.log(data);
        }
        this.ws.send(JSON.stringify({
            topic: topic,
            data: data
        }))
    }
}
