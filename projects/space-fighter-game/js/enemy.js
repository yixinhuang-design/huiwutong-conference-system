// 敌人类

import * as Utils from './utils.js';

export class Enemy {
    constructor(type, x, y, canvasWidth) {
        this.type = type; // 'NORMAL', 'FAST', 'BOSS'
        this.x = x;
        this.y = y;
        this.markedForDeletion = false;
        
        // 根据类型设置属性
        switch(type) {
            case 'FAST':
                this.width = 30;
                this.height = 30;
                this.speed = 4;
                this.health = 5;
                this.maxHealth = 5;
                this.score = 20;
                this.color = '#ff0';
                this.shootInterval = 2000;
                break;
            case 'BOSS':
                this.width = 80;
                this.height = 80;
                this.speed = 1;
                this.health = 100;
                this.maxHealth = 100;
                this.score = 500;
                this.color = '#f0f';
                this.shootInterval = 1000;
                break;
            case 'NORMAL':
            default:
                this.width = 40;
                this.height = 40;
                this.speed = 2;
                this.health = 10;
                this.maxHealth = 10;
                this.score = 10;
                this.color = '#f00';
                this.shootInterval = 3000;
                break;
        }
        
        this.lastShotTime = Date.now();
        this.moveAngle = Utils.random(0, Math.PI * 2);
    }

    update(canvasWidth, canvasHeight, playerX, playerY) {
        // 根据类型执行不同的AI
        switch(this.type) {
            case 'FAST':
                // 快速敌机：追踪玩家
                this.trackPlayer(playerX, playerY);
                break;
            case 'BOSS':
                // Boss：缓慢移动 + 射击
                this.bossPattern(canvasWidth, playerX, playerY);
                break;
            case 'NORMAL':
            default:
                // 普通敌机：直线向下
                this.y += this.speed;
                this.x += Math.sin(this.y * 0.01) * 1; // 轻微摆动
                break;
        }

        // 超出屏幕底部标记删除（Boss除外）
        if (this.y > canvasHeight + this.height && this.type !== 'BOSS') {
            this.markedForDeletion = true;
        }
    }

    // 追踪玩家（快速敌机）
    trackPlayer(playerX, playerY) {
        const angle = Math.atan2(playerY - this.y, playerX - this.x);
        this.x += Math.cos(angle) * this.speed;
        this.y += Math.sin(angle) * this.speed;
    }

    // Boss行为模式
    bossPattern(canvasWidth, playerX, playerY) {
        // 左右移动
        this.x += Math.sin(Date.now() * 0.001) * 2;
        
        // 缓慢向下
        if (this.y < 100) {
            this.y += this.speed;
        }
        
        // 限制在屏幕内
        this.x = Utils.clamp(this.x, this.width / 2, canvasWidth - this.width / 2);
    }

    // 射击
    shoot(playerX, playerY) {
        const now = Date.now();
        if (now - this.lastShotTime < this.shootInterval) {
            return null;
        }
        this.lastShotTime = now;

        if (this.type === 'BOSS') {
            // Boss发射散射
            const bullets = [];
            for (let i = -2; i <= 2; i++) {
                const angle = Math.atan2(playerY - this.y, playerX - this.x) + i * 0.2;
                const speed = 5;
                bullets.push({
                    x: this.x,
                    y: this.y + this.height / 2,
                    vx: Math.cos(angle) * speed,
                    vy: Math.sin(angle) * speed
                });
            }
            return bullets;
        } else {
            // 普通射击
            const angle = Math.atan2(playerY - this.y, playerX - this.x);
            const speed = 3;
            return [{
                x: this.x,
                y: this.y + this.height / 2,
                vx: Math.cos(angle) * speed,
                vy: Math.sin(angle) * speed
            }];
        }
    }

    // 受到伤害
    takeDamage(damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.markedForDeletion = true;
            return this.score;
        }
        return 0;
    }

    draw(ctx) {
        ctx.save();
        
        // 绘制敌机
        ctx.fillStyle = this.color;
        ctx.shadowBlur = 15;
        ctx.shadowColor = this.color;
        
        // 简单的敌机形状
        ctx.beginPath();
        ctx.moveTo(this.x, this.y + this.height / 2);
        ctx.lineTo(this.x - this.width / 2, this.y - this.height / 2);
        ctx.lineTo(this.x + this.width / 2, this.y - this.height / 2);
        ctx.closePath();
        ctx.fill();

        // Boss绘制血条
        if (this.type === 'BOSS') {
            const healthPercent = this.health / this.maxHealth;
            ctx.fillStyle = '#333';
            ctx.fillRect(this.x - 40, this.y - 60, 80, 8);
            ctx.fillStyle = healthPercent > 0.3 ? '#0f0' : '#f00';
            ctx.fillRect(this.x - 40, this.y - 60, 80 * healthPercent, 8);
        }
        
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
