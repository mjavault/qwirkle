class Notify {
    constructor() {
        const that = this;
        this.granted = false;
        this.focused = true;
        (async () => {
            if (Notification.permission === 'granted') {
                that.granted = true;
            } else if (Notification.permission !== 'denied') {
                let permission = await Notification.requestPermission();
                that.granted = permission === 'granted';
            }
        })();
        //inhibit notifications when game has focus
        window.onfocus = function () {
            that.focused = true;
        };
        window.onblur = function () {
            that.focused = false;
        };
    }

    send() {
        if (!this.focused) {
            const notification = new Notification("Qwirkle", {
                body: "It's your turn to play!",
                icon: 'favicon.png'
            });
            notification.onshow = function () {
                const audio = new Audio('sounds/play-others.mp3');
                audio.play();
            };
            notification.onclick = function () {
                window.focus();
                this.close();
            };
        }
    }
}
