# 太空战机游戏 - 快速启动指南

## 🎮 立即游玩

### 方法1：直接打开（推荐）
1. 找到 `index.html` 文件
2. 双击用浏览器打开
3. 点击"开始游戏"

### 方法2：本地服务器
```bash
# 使用Python启动本地服务器
python -m http.server 8000

# 访问 http://localhost:8000
```

---

## 🕹️ 操作说明

### PC端
- **WASD** 或 **方向键** - 移动飞船
- 自动射击

### 移动端
- **触摸拖动** - 控制飞船移动
- 自动射击

---

## ✨ 游戏特色

- ✨ 霓虹科幻视觉风格
- 🎯 流畅的60 FPS体验
- 📱 完美支持PC和移动端
- 🎮 三种武器等级升级
- 👾 多种敌机类型（普通/快速/Boss）
- 💾 本地最高分记录

---

## 📁 项目结构

```
space-fighter-game/
├── index.html          # 游戏主页面
├── css/
│   └── style.css      # 样式文件
├── js/
│   ├── main.js        # 入口文件
│   ├── game.js        # 游戏核心
│   ├── player.js      # 玩家类
│   ├── enemy.js       # 敌人类
│   ├── bullet.js      # 子弹类
│   ├── particle.js    # 粒子系统
│   ├── collision.js   # 碰撞检测
│   ├── input.js       # 输入处理
│   └── utils.js       # 工具函数
└── docs/              # 文档
```

---

## 🛠️ 技术栈

- **HTML5 Canvas** - 渲染引擎
- **原生JavaScript** - ES6+模块化
- **CSS3** - 样式和动画
- **LocalStorage** - 本地存储

---

## 🚀 部署

### GitHub Pages
1. 创建仓库并上传文件
2. Settings → Pages → 启用
3. 访问 `https://username.github.io/space-fighter-game`

### Netlify
1. 拖拽文件夹到 netlify.com
2. 自动获得公开URL

---

## 📝 开发日志

- **2026-03-20** - 项目启动，MVP完成
- 使用ES6模块化开发
- 纯Canvas绘制，无图片依赖
- 移动端触摸优化

---

## 🎯 MVP功能清单

- ✅ 玩家飞船控制
- ✅ 自动射击系统
- ✅ 敌机AI（普通/快速/Boss）
- ✅ 碰撞检测
- ✅ 粒子爆炸特效
- ✅ 武器升级系统
- ✅ 计分和最高分
- ✅ 游戏循环（开始-游戏-结束）

---

**Enjoy the game! 🚀**
