const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

/**
 * TabBar Icon Generator for Huiwutong UniApp Project
 * Uses Font Awesome 6 Free icons to generate PNG files
 */

const CONFIG = {
  iconsDir: path.join(__dirname, '../static/icons'),
  size: 81,
  normalColor: '#64748b',
  activeColor: '#3b82f6',
  // Font Awesome 6 Solid SVG paths (from https://fontawesome.com/icons)
  icons: [
    {
      name: 'home',
      activeName: 'home-active',
      // fa-home
      path: 'M10.707 2.293a1 1 0 00-1.414 0l-7 7a1 1 0 001.414 1.414L4 10.414V17a1 1 0 001 1h2a1 1 0 001-1v-2a1 1 0 011-1h2a1 1 0 011 1v2a1 1 0 001 1h2a1 1 0 001-1v-6.586l.293.293a1 1 0 001.414-1.414l-7-7z'
    },
    {
      name: 'assistant',
      activeName: 'assistant-active',
      // fa-robot
      path: 'M12 2a2 2 0 012 2c0 .74-.4 1.39-1 1.73V7h1a7 7 0 017 7h1a1 1 0 011 1v3a1 1 0 01-1 1h-1v1a2 2 0 01-2 2H5a2 2 0 01-2-2v-1H2a1 1 0 01-1-1v-3a1 1 0 011-1h1a7 7 0 017-7h1V5.73c-.6-.34-1-.99-1-1.73a2 2 0 012-2M7.5 13A1.5 1.5 0 006 14.5 1.5 1.5 0 007.5 16 1.5 1.5 0 009 14.5 1.5 1.5 0 007.5 13zm9 0a1.5 1.5 0 00-1.5 1.5 1.5 1.5 0 001.5 1.5 1.5 1.5 0 001.5-1.5 1.5 1.5 0 00-1.5-1.5zM6 20a4 4 0 004 4h4a4 4 0 004-4'
    },
    {
      name: 'chat',
      activeName: 'chat-active',
      // fa-comments (simplified for clarity)
      path: 'M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z'
    },
    {
      name: 'archive',
      activeName: 'archive-active',
      // fa-folder
      path: 'M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2v11z'
    },
    {
      name: 'profile',
      activeName: 'profile-active',
      // fa-user
      path: 'M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'
    }
  ]
};

/**
 * Creates an SVG string for the icon
 */
function createSVG(pathData, color, size) {
  const padding = size * 0.15;
  const iconSize = size - padding * 2;

  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 512 512">
    <g transform="translate(${padding}, ${padding}) scale(${iconSize / 512})">
      <path d="${pathData}" fill="${color}"/>
    </g>
  </svg>`;
}

/**
 * Main generation function
 */
async function generateIcons() {
  console.log('🎨 Generating TabBar Icons with Font Awesome...\n');

  // Ensure icons directory exists
  if (!fs.existsSync(CONFIG.iconsDir)) {
    fs.mkdirSync(CONFIG.iconsDir, { recursive: true });
    console.log(`✓ Created directory: ${CONFIG.iconsDir}\n`);
  }

  // Generate each icon pair
  for (const icon of CONFIG.icons) {
    const normalPath = path.join(CONFIG.iconsDir, `${icon.name}.png`);
    const activePath = path.join(CONFIG.iconsDir, `${icon.activeName}.png`);

    try {
      // Generate normal icon
      const normalSVG = createSVG(icon.path, CONFIG.normalColor, CONFIG.size);
      await sharp(Buffer.from(normalSVG)).png().toFile(normalPath);
      console.log(`✓ Generated ${normalPath.replace(/\\/g, '/')} (${CONFIG.size}×${CONFIG.size}px, ${CONFIG.normalColor})`);

      // Generate active icon
      const activeSVG = createSVG(icon.path, CONFIG.activeColor, CONFIG.size);
      await sharp(Buffer.from(activeSVG)).png().toFile(activePath);
      console.log(`✓ Generated ${activePath.replace(/\\/g, '/')} (${CONFIG.size}×${CONFIG.size}px, ${CONFIG.activeColor})`);
    } catch (error) {
      console.error(`✗ Error generating ${icon.name}:`, error.message);
      throw error;
    }
  }

  console.log('\n✅ All 10 tabbar icons generated successfully!');
  console.log('\n📁 Icon files location: static/icons/');
  console.log('\n🚀 Next steps:');
  console.log('   1. Restart dev server: npm run dev:h5');
  console.log('   2. Visit http://localhost:9066');
  console.log('   3. Verify icons display in bottom tabbar\n');
}

// Run the generator
generateIcons().catch(console.error);
