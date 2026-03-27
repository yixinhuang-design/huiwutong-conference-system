// 玩家类

import * as Utils from './utils.js';

export class Player {
    constructor(canvasWidth, canvasHeight) {
        this.width = 40;
        this.height = 40;
        this.x = canvasWidth / 2;
        this.y = canvasHeight - 100;
        this.speed = 5;
        this.health = 100;
        this.maxHealth = 100;
        this.markedForDeletion = false;
        
        // 武器系统
        this.weaponLevel = 1;
        this.lastShotTime = Date.now();
        this.shootInterval = 200; // 射击间隔(ms)
        
        // 无敌时间
        this.invincibleTime = 0;
        
        // 移动方向
        this.moveLeft = false;
        this.moveRight = false;
        this.moveUp = false;
        this.moveDown = false;
    }

    update(canvasWidth, canvasHeight) {
        // 移动
        if (this.moveLeft) this.x -= this.speed;
        if (this.moveRight) this.x += this.speed;
        if (this.moveUp) this.y -= this.speed;
        if (this.moveDown) this.y += this.speed;

        // 边界限制
        this.x = Utils.clamp(this.x, this.width / 2, canvasWidth - this.width / 2);
        this.y = Utils.clamp(this.y, this.height / 2, canvasHeight - this.height / 2);

        // 更新无敌时间
        if (this.invincibleTime > 0) {
            this.invincibleTime--;
        }
    }

    // 射击
    shoot() {
        const now = Date.now();
        if (now - this.lastShotTime < this.shootInterval) {
            return [];
        }
        this.lastShotTime = now;

        const bullets = [];
        const bulletSpeed = -8;

        // 根据武器等级发射不同数量的子弹
        switch(this.weaponLevel) {
            case 1:
                // 单发
                bullets.push({ x: this.x, y: this.y - this.height / 2, vx: 0, vy: bulletSpeed });
                break;
            case 2:
                // 双发
                bullets.push({ x: this.x - 10, y: this.y - this.height / 2, vx: 0, vy: bulletSpeed });
                bullets.push({ x: this.x + 10, y: this.y - this.height / 2, vx: 0, vy: bulletSpeed });
                break;
            case 3:
            default:
                // 三发
                bullets.push({ x: this.x, y: this.y - this.height / 2, vx: 0, vy: bulletSpeed });
                bullets.push({ x: this.x - 15, y: this.y - this.height / 2, vx: -1, vy: bulletSpeed * 0.95 });
                bullets.push({ x: this.x + 15, y: this.y - this.height / 2, vx: 1, vy: bulletSpeed * 0.95 });
                break;
        }

        return bullets;
    }

    // 受到伤害
    takeDamage(damage) {
        if (this.invincibleTime > 0) {
            return 0;
        }
        
        this.health -= damage;
        this.invincibleTime = 60; // 60帧无敌时间（约1秒）
        
        if (this.health <= 0) {
            this.health = 0;
            this.markedForDeletion = true;
        }
        
        return damage;
    }

    // 恢复生命
    heal(amount) {
        this.health = Math.min(this.health + amount, this.maxHealth);
    }

    // 升级武器
    upgradeWeapon() {
        if (this.weaponLevel < 3) {
            this.weaponLevel++;
        }
    }

    // 重置武器
    resetWeapon() {
        this.weaponLevel = 1;
    }

    draw(ctx) {
        ctx.save();

        // 无敌时闪烁
        if (this.invincibleTime > 0 && Math.floor(this.invincibleTime / 5) % 2 === 0) {
            ctx.globalAlpha = 0.5;
        }

        // 绘制玩家飞船
        ctx.fillStyle = '#0ff';
        ctx.shadowBlur = 20;
        ctx.shadowColor = '#0ff';

        // 飞船主体（三角形）
        ctx.beginPath();
        ctx.moveTo(this.x, this.y - this.height / 2);
        ctx.lineTo(this.x - this.width / 2, this.y + this.height / 2);
        ctx.lineTo(this.x + this.width / 2, this.y + this.height / 2);
        ctx.closePath();
        ctx.fill();

        // 驾驶舱
        ctx.fillStyle = '#fff';
        ctx.beginPath();
        ctx.arc(this.x, this.y, 8, 0, Math.PI * 2);
        ctx.fill();

        // 引擎火焰
        ctx.fillStyle = '#ff0';
        ctx.shadowColor = '#ff0';
        const flameHeight = 10 + Math.random() * 5;
        ctx.beginPath();
        ctx.moveTo(this.x - 10, this.y + this.height / 2);
        ctx.lineTo(this.x, this.y + this.height / 2 + flameHeight);
        ctx.lineTo(this.x + 10, this.y + this.height / 2);
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
