const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

/**
 * TabBar Icon Generator V2
 * 使用正确 viewBox 的 SVG 路径生成 81×81 PNG 图标
 */

const CONFIG = {
  iconsDir: path.join(__dirname, '../static/icons'),
  size: 81,
  normalColor: '#64748b',   // 灰色
  activeColor: '#3b82f6',   // 蓝色
};

// 图标 SVG 路径 (viewBox = "0 0 24 24")
const ICONS = [
  {
    name: 'home',
    // 房屋图标
    viewBox: '0 0 24 24',
    path: 'M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z',
  },
  {
    name: 'assistant',
    // 智能助手/机器人图标
    viewBox: '0 0 24 24',
    path: 'M20 9V7c0-1.1-.9-2-2-2h-3c0-1.66-1.34-3-3-3S9 3.34 9 5H6c-1.1 0-2 .9-2 2v2c-1.66 0-3 1.34-3 3s1.34 3 3 3v4c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2v-4c1.66 0 3-1.34 3-3s-1.34-3-3-3zM7.5 11.5c0-.83.67-1.5 1.5-1.5s1.5.67 1.5 1.5S9.83 13 9 13s-1.5-.67-1.5-1.5zM16 17H8v-2h8v2zm-1-4c-.83 0-1.5-.67-1.5-1.5S14.17 10 15 10s1.5.67 1.5 1.5S15.83 13 15 13z',
  },
  {
    name: 'chat',
    // 聊天气泡图标
    viewBox: '0 0 24 24',
    path: 'M21 6h-2v9H6v2c0 .55.45 1 1 1h11l4 4V7c0-.55-.45-1-1-1zm-4 6V3c0-.55-.45-1-1-1H3c-.55 0-1 .45-1 1v14l4-4h10c.55 0 1-.45 1-1z',
  },
  {
    name: 'archive',
    // 文件夹图标
    viewBox: '0 0 24 24',
    path: 'M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z',
  },
  {
    name: 'profile',
    // 用户头像图标
    viewBox: '0 0 24 24',
    path: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z',
  },
];

function createSVG(icon, color, size) {
  // 在 viewBox 内绘制图标，保留适当 padding
  const padding = Math.round(size * 0.15);
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
  <rect width="${size}" height="${size}" fill="none"/>
  <svg x="${padding}" y="${padding}" width="${size - padding * 2}" height="${size - padding * 2}" viewBox="${icon.viewBox}">
    <path d="${icon.path}" fill="${color}"/>
  </svg>
</svg>`;
}

async function main() {
  console.log('🎨 Generating TabBar Icons V2...\n');

  if (!fs.existsSync(CONFIG.iconsDir)) {
    fs.mkdirSync(CONFIG.iconsDir, { recursive: true });
  }

  for (const icon of ICONS) {
    const normalFile = path.join(CONFIG.iconsDir, `${icon.name}.png`);
    const activeFile = path.join(CONFIG.iconsDir, `${icon.name}-active.png`);

    // 普通状态
    const normalSvg = createSVG(icon, CONFIG.normalColor, CONFIG.size);
    await sharp(Buffer.from(normalSvg)).png().toFile(normalFile);
    const normalSize = fs.statSync(normalFile).size;
    console.log(`✓ ${icon.name}.png          (${normalSize} bytes, ${CONFIG.normalColor})`);

    // 激活状态
    const activeSvg = createSVG(icon, CONFIG.activeColor, CONFIG.size);
    await sharp(Buffer.from(activeSvg)).png().toFile(activeFile);
    const activeSize = fs.statSync(activeFile).size;
    console.log(`✓ ${icon.name}-active.png   (${activeSize} bytes, ${CONFIG.activeColor})`);
  }

  console.log('\n✅ All 10 tabbar icons generated!');
}

main().catch(err => { console.error('Error:', err); process.exit(1); });
