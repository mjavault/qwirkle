class WebSocketClient {
    constructor(uri) {
        this.uri = uri;
        this.ws = null
        this.onMessage = null;
        this.onConnect = null;
        this.keepAlive = null;
        this.autoReconnect = false;
        this.autoReconnectPending = false;
        this.debug = false;
    }

    enableDebug() {
        this.debug = true;
    }

    connect() {
        this.autoReconnectPending = false;
        this.autoReconnect = true;
        const url = new URL(this.uri, window.location.href);
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
            if (this.autoReconnect && !this.autoReconnectPending) {
                this.autoReconnectPending = true;
                setTimeout(() => this.connect(), 5000);
                console.log('Reconnecting in 5 seconds');
            }
        };

        this.ws.onerror = (event) => {
            console.log('[error] Connection error, eventPhase=' + event.eventPhase);
            if (this.autoReconnect && !this.autoReconnectPending) {
                this.autoReconnectPending = true;
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

    setKeepAlive(fn, seconds) {
        this.keepAlive = fn;
        setInterval(() => {
            if (this.debug) {
                console.log(`Keep alive (readyState=${this.ws.readyState})`);
            }
            if (this.ws.readyState === WebSocket.OPEN) {
                this.keepAlive();
            }
        }, seconds * 1000);
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
