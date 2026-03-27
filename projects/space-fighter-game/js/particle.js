// 粒子系统 - 用于爆炸特效

import * as Utils from './utils.js';

export class Particle {
    constructor(x, y, color) {
        this.x = x;
        this.y = y;
        this.size = Utils.random(2, 5);
        this.speedX = Utils.random(-3, 3);
        this.speedY = Utils.random(-3, 3);
        this.color = color;
        this.alpha = 1;
        this.decay = Utils.random(0.01, 0.03);
        this.markedForDeletion = false;
    }

    update() {
        this.x += this.speedX;
        this.y += this.speedY;
        this.alpha -= this.decay;

        if (this.alpha <= 0) {
            this.markedForDeletion = true;
        }
    }

    draw(ctx) {
        ctx.save();
        ctx.globalAlpha = this.alpha;
        ctx.fillStyle = this.color;
        ctx.shadowBlur = 10;
        ctx.shadowColor = this.color;
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
    }
}

// 粒子管理器
export class ParticleSystem {
    constructor() {
        this.particles = [];
    }

    // 创建爆炸效果
    createExplosion(x, y, color, count = 20) {
        for (let i = 0; i < count; i++) {
            this.particles.push(new Particle(x, y, color));
        }
    }

    // 创建尾迹效果
    createTrail(x, y, color) {
        if (Math.random() > 0.5) {
            this.particles.push(new Particle(x, y, color));
        }
    }

    update() {
        this.particles.forEach(particle => particle.update());
        this.particles = this.particles.filter(particle => !particle.markedForDeletion);
    }

    draw(ctx) {
        this.particles.forEach(particle => particle.draw(ctx));
    }

    // 清空所有粒子
    clear() {
        this.particles = [];
    }
}
