// 子弹类

import * as Utils from './utils.js';

export class Bullet {
    constructor(x, y, vx, vy, isEnemy = false) {
        this.x = x;
        this.y = y;
        this.width = 6;
        this.height = 15;
        this.vx = vx;
        this.vy = vy;
        this.isEnemy = isEnemy;
        this.markedForDeletion = false;
        this.color = isEnemy ? '#f00' : '#0ff';
    }

    update(canvasWidth, canvasHeight) {
        this.x += this.vx;
        this.y += this.vy;

        // 超出屏幕标记删除
        if (this.y < -this.height || 
            this.y > canvasHeight + this.height ||
            this.x < -this.width || 
            this.x > canvasWidth + this.width) {
            this.markedForDeletion = true;
        }
    }

    draw(ctx) {
        ctx.save();
        ctx.fillStyle = this.color;
        ctx.shadowBlur = 10;
        ctx.shadowColor = this.color;
        
        // 绘制子弹
        ctx.beginPath();
        ctx.moveTo(this.x, this.y - this.height / 2);
        ctx.lineTo(this.x - this.width / 2, this.y + this.height / 2);
        ctx.lineTo(this.x + this.width / 2, this.y + this.height / 2);
        ctx.closePath();
        ctx.fill();
        
        ctx.restore();
    }

    // 获取碰撞盒
    getBounds() {
        return {
            x: this.x - this.width / 2,
            y: this.y - this.height / 2,
            width: this.width,
            height: this.height
        };
    }
}
