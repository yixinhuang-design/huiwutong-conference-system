// 输入处理系统

export class InputHandler {
    constructor(player) {
        this.player = player;
        this.keys = {};
        this.touchActive = false;
        this.touchX = 0;
        this.touchY = 0;

        this.initKeyboard();
        this.initTouch();
    }

    // 初始化键盘控制
    initKeyboard() {
        window.addEventListener('keydown', (e) => {
            this.keys[e.key] = true;
            this.updatePlayerMovement();
        });

        window.addEventListener('keyup', (e) => {
            this.keys[e.key] = false;
            this.updatePlayerMovement();
        });
    }

    // 初始化触摸控制
    initTouch() {
        const canvas = document.getElementById('gameCanvas');

        canvas.addEventListener('touchstart', (e) => {
            e.preventDefault();
            this.touchActive = true;
            const touch = e.touches[0];
            this.touchX = touch.clientX;
            this.touchY = touch.clientY;
        });

        canvas.addEventListener('touchmove', (e) => {
            e.preventDefault();
            if (!this.touchActive) return;

            const touch = e.touches[0];
            const dx = touch.clientX - this.touchX;
            const dy = touch.clientY - this.touchY;

            this.player.x += dx;
            this.player.y += dy;

            this.touchX = touch.clientX;
            this.touchY = touch.clientY;
        });

        canvas.addEventListener('touchend', () => {
            this.touchActive = false;
        });
    }

    // 更新玩家移动状态
    updatePlayerMovement() {
        this.player.moveLeft = this.keys['ArrowLeft'] || this.keys['a'] || this.keys['A'];
        this.player.moveRight = this.keys['ArrowRight'] || this.keys['d'] || this.keys['D'];
        this.player.moveUp = this.keys['ArrowUp'] || this.keys['w'] || this.keys['W'];
        this.player.moveDown = this.keys['ArrowDown'] || this.keys['s'] || this.keys['S'];
    }
}
