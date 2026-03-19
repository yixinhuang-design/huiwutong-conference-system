// ============================================
// 诊断和修复弹窗关闭问题 v2
// ============================================
// 使用方法：将此代码复制到浏览器控制台（按F12）中运行

console.log('========== 开始深入诊断弹窗问题 ==========');

// 1. 检查 DOM 结构
console.log('\n【步骤1】检查 DOM 结构');
const appDiv = document.getElementById('app');
if (!appDiv) {
    console.error('❌ 无法找到 #app 元素');
} else {
    console.log('✅ 找到 #app 元素');

    // 检查弹窗是否在 #app 内部
    const modal = appDiv.querySelector('.area-config-modal');
    if (modal) {
        console.log('✅ 弹窗在 #app 内部（正确）');
        console.log('弹窗 v-if 状态:', modal.hasAttribute('v-if') ? '有 v-if 属性' : '没有 v-if 属性');
        console.log('弹窗 display 样式:', window.getComputedStyle(modal).display);
        console.log('弹窗 visibility 样式:', window.getComputedStyle(modal).visibility);
    } else {
        console.error('❌ 在 #app 内部找不到弹窗');
        console.log('在全局查找弹窗...');
        const globalModal = document.querySelector('.area-config-modal');
        if (globalModal) {
            console.error('⚠️  弹窗在 #app 外部（这就是问题所在！）');
            console.log('弹窗父元素:', globalModal.parentElement);
        } else {
            console.log('ℹ️  当前没有弹窗显示');
        }
    }
}

// 2. 获取 Vue 应用实例
console.log('\n【步骤2】获取 Vue 应用实例');
let app = null;

// 方法1: 尝试从 __vue_app__ 获取
if (appDiv && appDiv.__vue_app__) {
    app = appDiv.__vue_app__.config.globalProperties;
    console.log('✅ 通过 __vue_app__ 获取实例');
}
// 方法2: 尝试从 __vue__ 获取
else if (appDiv && appDiv.__vue__) {
    app = appDiv.__vue__;
    console.log('✅ 通过 __vue__ 获取实例');
}

if (!app) {
    console.error('❌ 无法获取 Vue 实例');
} else {
    console.log('✅ Vue 实例获取成功');

    // 3. 检查响应式数据
    console.log('\n【步骤3】检查响应式数据');
    console.log('showAreaConfigModal:', app.showAreaConfigModal);
    console.log('editingArea:', app.editingArea);
    console.log('areaConfig:', app.areaConfig);

    // 4. 测试 closeAreaConfig 方法
    console.log('\n【步骤4】测试 closeAreaConfig 方法');
    if (typeof app.closeAreaConfig === 'function') {
        console.log('✅ closeAreaConfig 方法存在');

        // 监控调用
        const originalCloseAreaConfig = app.closeAreaConfig;
        app.closeAreaConfig = function() {
            console.log('🔧 closeAreaConfig 被调用');
            console.log('调用前 showAreaConfigModal:', app.showAreaConfigModal);
            const result = originalCloseAreaConfig.call(app);
            console.log('调用后 showAreaConfigModal:', app.showAreaConfigModal);

            // 检查 DOM 更新
            setTimeout(() => {
                const modal = document.querySelector('.area-config-modal');
                if (modal) {
                    const styles = window.getComputedStyle(modal);
                    console.log('DOM 更新后:');
                    console.log('  - display:', styles.display);
                    console.log('  - visibility:', styles.visibility);
                    console.log('  - opacity:', styles.opacity);
                } else {
                    console.log('✅ DOM 中已找不到弹窗（已成功移除）');
                }
            }, 100);

            return result;
        };

        console.log('✅ 已监控 closeAreaConfig 方法');
        console.log('💡 现在点击关闭按钮或遮罩层，会看到详细的调用信息');
    } else {
        console.error('❌ closeAreaConfig 方法不存在');
    }

    // 5. 强制关闭弹窗
    console.log('\n【步骤5】尝试强制关闭弹窗');
    if (app.showAreaConfigModal === true) {
        console.log('⚠️  检测到弹窗处于打开状态，正在强制关闭...');
        app.showAreaConfigModal = false;
        console.log('✅ 已设置 showAreaConfigModal = false');

        // 尝试多种方法触发更新
        if (app.$forceUpdate) {
            app.$forceUpdate();
            console.log('✅ 已调用 $forceUpdate()');
        }

        // 检查 DOM 中的弹窗
        setTimeout(() => {
            const modal = document.querySelector('.area-config-modal');
            if (modal) {
                const styles = window.getComputedStyle(modal);
                console.log('⚠️  弹窗仍然存在于 DOM 中');
                console.log('  - display:', styles.display);
                console.log('  - visibility:', styles.visibility);

                // 尝试强制隐藏
                modal.style.display = 'none';
                console.log('🔧 已强制设置 display: none');
            } else {
                console.log('✅ 弹窗已从 DOM 中移除');
            }
        }, 100);
    } else {
        console.log('ℹ️  showAreaConfigModal 当前为 false（弹窗应该已关闭）');
    }
}

// 6. 检查事件监听器
console.log('\n【步骤6】检查事件监听器');
const modal = document.querySelector('.area-config-modal');
if (modal) {
    console.log('找到弹窗元素');

    // 检查点击事件
    modal.addEventListener('click', function(e) {
        console.log('🔍 弹窗被点击:', e.target);
        console.log('  - currentTarget:', e.currentTarget);
        console.log('  - target === currentTarget:', e.target === e.currentTarget);
    });

    const closeBtn = modal.querySelector('.close-btn');
    if (closeBtn) {
        console.log('找到关闭按钮');

        closeBtn.addEventListener('click', function(e) {
            console.log('🔍 关闭按钮被点击');
            e.stopPropagation();
            e.preventDefault();
            if (app && typeof app.closeAreaConfig === 'function') {
                console.log('🔧 手动调用 app.closeAreaConfig()');
                app.closeAreaConfig();
            }
        });
        console.log('✅ 已为关闭按钮添加调试事件监听器');
    }
}

console.log('\n========== 诊断完成 ==========');
console.log('\n💡 提示：');
console.log('1. 现在尝试点击关闭按钮或遮罩层');
console.log('2. 观察控制台输出的调试信息');
console.log('3. 如果弹窗仍然无法关闭，请截图控制台输出');
