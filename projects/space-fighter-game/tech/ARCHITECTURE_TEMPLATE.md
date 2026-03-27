# 太空战机游戏 - 技术架构设计

**文档版本：** v1.0  
**创建日期：** 2026-03-20  
**技术经理：** 待定  
**状态：** 草稿

---

## 1. 技术选型

### 1.1 核心技术
- **渲染引擎：** HTML5 Canvas API
- **编程语言：** 原生JavaScript (ES6+)
- **样式：** CSS3
- **构建工具：** 无需构建，直接运行

### 1.2 为什么选择这个技术栈？
- ✅ **零依赖：** 无需安装任何库，直接运行
- ✅ **性能好：** Canvas渲染性能优秀
- ✅ **兼容性强：** 所有现代浏览器都支持
- ✅ **开发快：** 适合快速原型开发

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────┐
│         Game Loop (requestAnimationFrame)      │
├─────────────────────────────────────┤
│  Input Handler  │  Game Manager  │  Renderer   │
│  - Keyboard     │  - State       │  - Canvas    │
│  - Touch        │  - Entities    │  - Drawing   │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│          Entity System              │
│  ┌──────┐ ┌──────┐ ┌──────┐        │
│  │Player│ │Enemy │ │Bullet│        │
│  └──────┘ └──────┘ └──────┘        │
│  ┌──────┐ ┌──────┐                 │
│  │Powerup│ │Particle│              │
│  └──────┘ └──────┘                 │
└─────────────────────────────────────┘
```

### 2.2 核心模块

#### Game Loop（游戏主循环）
```javascript
function gameLoop() {
  update();    // 更新状态
  draw();      // 渲染画面
  requestAnimationFrame(gameLoop);
}
```

#### Input Handler（输入处理）
- 监听键盘事件（WASD、方向键、空格）
- 监听触摸事件（移动端）
- 统一的输入接口

#### Game Manager（游戏管理器）
- 游戏状态管理（MENU, PLAYING, GAME_OVER）
- 实体管理（创建、更新、销毁）
- 碰撞检测
- 分数计算

#### Renderer（渲染器）
- 清空画布
- 绘制背景
- 绘制所有实体
- 绘制UI（HUD）

---

## 3. 数据结构设计

### 3.1 实体基类
```javascript
class Entity {
  constructor(x, y, width, height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.markedForDeletion = false;
  }
  
  update(deltaTime) {}
  draw(ctx) {}
  
  getBounds() {
    return { x: this.x, y: this.y, width: this.width, height: this.height };
  }
}
```

### 3.2 玩家类
```javascript
class Player extends Entity {
  constructor() {
    super(canvas.width / 2, canvas.height - 100, 40, 40);
    this.speed = 5;
    this.health = 100;
    this.weaponLevel = 1;
    this.lastShotTime = 0;
    this.shootInterval = 200; // ms
  }
  
  update(deltaTime) {
    // 根据输入移动
    // 自动射击
  }
  
  draw(ctx) {
    // 绘制玩家飞船
  }
  
  shoot() {
    // 发射子弹
  }
}
```

### 3.3 敌人类
```javascript
class Enemy extends Entity {
  constructor(type) {
    super(0, 0, 40, 40);
    this.type = type; // NORMAL, FAST, BOSS
    this.health = type === 'BOSS' ? 100 : 10;
    this.speed = type === 'FAST' ? 4 : 2;
    this.score = type === 'BOSS' ? 100 : 10;
  }
  
  update(deltaTime) {
    // 根据类型移动
    // BOSS会发射弹幕
  }
  
  draw(ctx) {
    // 绘制敌机
  }
}
```

### 3.4 子弹类
```javascript
class Bullet extends Entity {
  constructor(x, y, vx, vy, isEnemy = false) {
    super(x, y, 8, 16);
    this.vx = vx;
    this.vy = vy;
    this.isEnemy = isEnemy;
  }
  
  update(deltaTime) {
    this.x += this.vx;
    this.y += this.vy;
    
    // 超出屏幕标记删除
    if (this.y < 0 || this.y > canvas.height) {
      this.markedForDeletion = true;
    }
  }
  
  draw(ctx) {
    // 绘制子弹
  }
}
```

### 3.5 粒子类
```javascript
class Particle extends Entity {
  constructor(x, y, color) {
    super(x, y, 4, 4);
    this.vx = (Math.random() - 0.5) * 8;
    this.vy = (Math.random() - 0.5) * 8;
    this.life = 1.0;
    this.color = color;
  }
  
  update(deltaTime) {
    this.x += this.vx;
    this.y += this.vy;
    this.life -= 0.02;
    
    if (this.life <= 0) {
      this.markedForDeletion = true;
    }
  }
  
  draw(ctx) {
    ctx.globalAlpha = this.life;
    ctx.fillStyle = this.color;
    ctx.fillRect(this.x, this.y, this.width, this.height);
    ctx.globalAlpha = 1.0;
  }
}
```

---

## 4. 关键算法

### 4.1 碰撞检测（AABB）
```javascript
function checkCollision(rect1, rect2) {
  return rect1.x < rect2.x + rect2.width &&
         rect1.x + rect1.width > rect2.x &&
         rect1.y < rect2.y + rect2.height &&
         rect1.y + rect1.height > rect2.y;
}
```

### 4.2 敌人生成算法
```javascript
function spawnEnemy() {
  const types = ['NORMAL', 'NORMAL', 'FAST'];
  const type = types[Math.floor(Math.random() * types.length)];
  const x = Math.random() * (canvas.width - 40);
  
  enemies.push(new Enemy(type, x, -40));
}

// Boss生成（每5波）
if (wave % 5 === 0 && !bossSpawned) {
  enemies.push(new Enemy('BOSS', canvas.width / 2 - 50, -100));
  bossSpawned = true;
}
```

### 4.3 粒子爆炸效果
```javascript
function createExplosion(x, y, color, count = 20) {
  for (let i = 0; i < count; i++) {
    particles.push(new Particle(x, y, color));
  }
}
```

---

## 5. 性能优化

### 5.1 渲染优化
- 使用对象池复用子弹和粒子
- 限制同屏粒子数量（最多100个）
- 使用离屏Canvas缓存静态图形

### 5.2 逻辑优化
- 空间分区优化碰撞检测（四叉树）
- 批量删除标记的实体
- delta time归一化

### 5.3 移动端优化
- 触摸事件节流
- 减少粒子数量
- 简化背景特效

---

## 6. 目录结构

```
space-fighter-game/
├── index.html              # 游戏入口
├── css/
│   └── style.css          # 样式文件
├── js/
│   ├── main.js            # 游戏入口，初始化
│   ├── game.js            # 游戏循环和状态管理
│   ├── player.js          # 玩家类
│   ├── enemy.js           # 敌人类
│   ├── bullet.js          # 子弹类
│   ├── particle.js        # 粒子系统
│   ├── input.js           # 输入处理
│   ├── collision.js       # 碰撞检测
│   └── utils.js           # 工具函数
├── assets/
│   ├── images/            # 图片资源（可选，可用Canvas绘制）
│   └── sounds/            # 音效资源（可选）
└── docs/                  # 文档
```

---

## 7. 开发计划

### 7.1 开发阶段

#### 阶段1：基础框架（1小时）
- [ ] 搭建HTML结构
- [ ] 设置Canvas和游戏循环
- [ ] 实现输入处理
- [ ] 创建玩家类和基本移动

#### 阶段2：核心玩法（2小时）
- [ ] 实现射击系统
- [ ] 实现敌人生成和AI
- [ ] 实现碰撞检测
- [ ] 实现计分系统

#### 阶段3：游戏系统（1小时）
- [ ] 实现游戏状态管理
- [ ] 实现道具系统
- [ ] 实现粒子特效
- [ ] 实现UI界面

#### 阶段4：优化和打磨（1小时）
- [ ] 性能优化
- [ ] 视觉效果增强
- [ ] 移动端适配
- [ ] Bug修复

---

## 8. 测试策略

### 8.1 单元测试
- 碰撞检测函数测试
- 实体更新逻辑测试

### 8.2 集成测试
- 游戏流程完整性测试
- 多实体同屏测试

### 8.3 性能测试
- FPS监控
- 内存占用测试
- 移动端流畅度测试

---

## 9. 部署方案

### 9.1 本地运行
直接用浏览器打开 index.html

### 9.2 在线部署
- GitHub Pages
- Netlify
- Vercel

### 9.3 打包
单HTML文件，内嵌CSS和JS，方便分发

---

## 10. 技术债务和改进空间

- [ ] 添加TypeScript类型
- [ ] 使用WebGL替代Canvas（性能）
- [ ] 添加音效系统
- [ ] 实现关卡编辑器
- [ ] 添加成就系统

---

**技术经理签名：** ___________  
**日期：** 2026-03-20
