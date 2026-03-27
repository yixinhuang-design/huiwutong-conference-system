// 游戏入口文件

import { Game } from './game.js';

// 等待DOM加载完成
window.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('gameCanvas');
    const game = new Game(canvas);

    // 初始绘制菜单背景
    function menuLoop() {
        if (game.state === 'MENU' || game.state === 'GAMEOVER') {
            game.drawMenuBackground();
            requestAnimationFrame(menuLoop);
        }
    }
    menuLoop();

    console.log('太空战机 - Space Fighter');
    console.log('游戏已加载，点击开始按钮开始游戏');
});
