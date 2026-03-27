// 游戏核心逻辑

import { Player } from './player.js';
import { Enemy } from './enemy.js';
import { Bullet } from './bullet.js';
import { ParticleSystem } from './particle.js';
import { InputHandler } from './input.js';
import { updateCollisions } from './collision.js';

export class Game {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.resize();

        // 游戏状态
        this.state = 'MENU'; // MENU, PLAYING, GAMEOVER
        this.score = 0;
        this.highScore = this.loadHighScore();
        this.wave = 1;
        this.waveTimer = 0;
        this.enemySpawnTimer = 0;

        // 实体
        this.player = null;
        this.enemies = [];
        this.bullets = [];
        this.particleSystem = new ParticleSystem();
        this.inputHandler = null;

        // 星空背景
        this.stars = this.createStars();

        // 绑定事件
        window.addEventListener('resize', () => this.resize());

        // UI元素
        this.scoreElement = document.getElementById('score');
        this.healthElement = document.getElementById('health');
        this.waveElement = document.getElementById('wave');
        this.startScreen = document.getElementById('start-screen');
        this.gameoverScreen = document.getElementById('gameover-screen');
        this.finalScoreElement = document.getElementById('final-score');
        this.highScoreElement = document.getElementById('high-score');

        // 按钮事件
        document.getElementById('start-btn').addEventListener('click', () => this.start());
        document.getElementById('restart-btn').addEventListener('click', () => this.restart());
    }

    resize() {
        this.canvas.width = window.innerWidth;
        this.canvas.height = window.innerHeight;
    }

    // 创建星空背景
    createStars() {
        const stars = [];
        for (let i = 0; i < 100; i++) {
            stars.push({
                x: Math.random() * this.canvas.width,
                y: Math.random() * this.canvas.height,
                size: Math.random() * 2,
                speed: Math.random() * 2 + 0.5
            });
        }
        return stars;
    }

    // 加载最高分
    loadHighScore() {
        const saved = localStorage.getItem('spaceFighterHighScore');
        return saved ? parseInt(saved) : 0;
    }

    // 保存最高分
    saveHighScore() {
        if (this.score > this.highScore) {
            this.highScore = this.score;
            localStorage.setItem('spaceFighterHighScore', this.highScore);
        }
    }

    // 开始游戏
    start() {
        this.state = 'PLAYING';
        this.startScreen.classList.add('hidden');
        this.gameoverScreen.classList.add('hidden');
        this.reset();
        this.loop();
    }

    // 重新开始
    restart() {
        this.start();
    }

    // 重置游戏
    reset() {
        this.score = 0;
        this.wave = 1;
        this.waveTimer = 0;
        this.enemySpawnTimer = 0;
        this.enemies = [];
        this.bullets = [];
        this.particleSystem.clear();

        this.player = new Player(this.canvas.width, this.canvas.height);
        this.inputHandler = new InputHandler(this.player);

        this.updateUI();
    }

    // 游戏结束
    gameOver() {
        this.state = 'GAMEOVER';
        this.saveHighScore();
        this.finalScoreElement.textContent = this.score;
        this.highScoreElement.textContent = this.highScore;
        this.gameoverScreen.classList.remove('hidden');
    }

    // 更新UI
    updateUI() {
        this.scoreElement.textContent = this.score;
        this.healthElement.textContent = Math.max(0, this.player.health);
        this.waveElement.textContent = this.wave;
    }

    // 生成敌人
    spawnEnemy() {
        // 根据波次调整生成间隔
        const spawnInterval = Math.max(30, 100 - this.wave * 5);
        
        this.enemySpawnTimer++;
        if (this.enemySpawnTimer >= spawnInterval) {
            this.enemySpawnTimer = 0;

            // 随机生成敌人类型
            const rand = Math.random();
            let type = 'NORMAL';
            
            if (rand > 0.9 && this.wave > 2) {
                type = 'FAST';
            } else if (rand > 0.95 && this.wave > 4) {
                type = 'BOSS';
            }

            const x = Math.random() * (this.canvas.width - 80) + 40;
            const enemy = new Enemy(type, x, -50, this.canvas.width);
            this.enemies.push(enemy);
        }
    }

    // 更新游戏逻辑
    update() {
        if (this.state !== 'PLAYING') return;

        // 更新玩家
        this.player.update(this.canvas.width, this.canvas.height);

        // 玩家自动射击
        const newBullets = this.player.shoot();
        newBullets.forEach(b => {
            this.bullets.push(new Bullet(b.x, b.y, b.vx, b.vy, false));
        });

        // 更新波次
        this.waveTimer++;
        if (this.waveTimer >= 600) { // 每10秒增加一波
            this.waveTimer = 0;
            this.wave++;
        }

        // 生成敌人
        this.spawnEnemy();

        // 更新敌人
        this.enemies.forEach(enemy => {
            enemy.update(this.canvas.width, this.canvas.height, this.player.x, this.player.y);
            
            // 敌人射击
            const enemyBullets = enemy.shoot(this.player.x, this.player.y);
            if (enemyBullets) {
                enemyBullets.forEach(b => {
                    this.bullets.push(new Bullet(b.x, b.y, b.vx, b.vy, true));
                });
            }
        });

        // 更新子弹
        this.bullets.forEach(bullet => {
            bullet.update(this.canvas.width, this.canvas.height);
        });

        // 碰撞检测
        const { score, damage } = updateCollisions(
            this.bullets,
            this.enemies,
            this.player,
            this.particleSystem
        );

        this.score += score;
        if (damage > 0) {
            this.updateUI();
        }

        // 更新粒子
        this.particleSystem.update();

        // 清理已删除的实体
        this.enemies = this.enemies.filter(e => !e.markedForDeletion);
        this.bullets = this.bullets.filter(b => !b.markedForDeletion);

        // 检查玩家死亡
        if (this.player.markedForDeletion) {
            this.particleSystem.createExplosion(
                this.player.x,
                this.player.y,
                '#0ff',
                50
            );
            this.gameOver();
        }

        // 更新UI
        this.updateUI();
    }

    // 绘制游戏画面
    draw() {
        // 清空画布
        this.ctx.fillStyle = '#000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);

        // 绘制星空背景
        this.drawStars();

        // 绘制粒子
        this.particleSystem.draw(this.ctx);

        // 绘制子弹
        this.bullets.forEach(bullet => bullet.draw(this.ctx));

        // 绘制敌人
        this.enemies.forEach(enemy => enemy.draw(this.ctx));

        // 绘制玩家
        if (this.player && !this.player.markedForDeletion) {
            this.player.draw(this.ctx);
        }
    }

    // 绘制星空
    drawStars() {
        this.ctx.fillStyle = '#fff';
        this.stars.forEach(star => {
            this.ctx.globalAlpha = Math.random() * 0.5 + 0.5;
            this.ctx.fillRect(star.x, star.y, star.size, star.size);
            
            // 星星移动
            star.y += star.speed;
            if (star.y > this.canvas.height) {
                star.y = 0;
                star.x = Math.random() * this.canvas.width;
            }
        });
        this.ctx.globalAlpha = 1;
    }

    // 游戏主循环
    loop() {
        if (this.state !== 'PLAYING') return;

        this.update();
        this.draw();
        requestAnimationFrame(() => this.loop());
    }

    // 初始绘制（菜单背景）
    drawMenuBackground() {
        this.drawStars();
    }
}
