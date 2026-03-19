// ============================================
// 紧急修复：关闭无法关闭的弹窗
// ============================================
// 使用方法：将此代码复制到浏览器控制台（按F12）中运行

console.log('========== 开始诊断弹窗问题 ==========');

// 1. 获取Vue应用实例
const appElement = document.getElementById('app');
if (!appElement) {
    console.error('❌ 无法找到#app元素');
} else {
    console.log('✅ 找到#app元素');

    // 2. 尝试获取Vue实例（可能在不同位置）
    let app = null;
    try {
        // 方法1: 直接从__vue__获取
        if (appElement.__vue_app__) {
            app = appElement.__vue_app__.config.globalProperties;
            console.log('✅ 通过__vue_app__获取实例');
        }
        // 方法2: 从父元素获取
        else if (appElement.__vue__) {
            app = appElement.__vue__;
            console.log('✅ 通过__vue__获取实例');
        }
    } catch (e) {
        console.error('❌ 获取Vue实例失败:', e);
    }

    if (app) {
        // 3. 检查弹窗状态
        console.log('当前弹窗状态:', app.showAreaConfigModal);

        // 4. 关闭弹窗
        if (app.showAreaConfigModal === true) {
            console.log('⚠️  检测到弹窗处于打开状态，正在关闭...');
            app.showAreaConfigModal = false;
            console.log('✅ 已设置showAreaConfigModal为false');

            // 强制更新视图
            if (app.$forceUpdate) {
                app.$forceUpdate();
                console.log('✅ 已强制更新视图');
            }
        } else {
            console.log('✅ 弹窗已经是关闭状态');
        }

        // 5. 检查其他可能导致问题的地方
        console.log('检查其他可能的问题:');
        console.log('- editingArea:', app.editingArea);
        console.log('- areaConfig:', app.areaConfig);

    } else {
        console.error('❌ 无法获取Vue实例，请尝试以下方法：');
        console.log('方法1: 刷新页面（Ctrl + F5）');
        console.log('方法2: 清除浏览器缓存');
        console.log('方法3: 检查是否有JavaScript错误');
    }
}

console.log('========== 诊断完成 ==========');
console.log('');
console.log('💡 提示：如果弹窗仍然无法关闭，请尝试：');
console.log('1. 强制刷新页面（Ctrl + Shift + R 或 Ctrl + F5）');
console.log('2. 清除浏览器缓存');
console.log('3. 在控制台运行: app.showAreaConfigModal = false');
