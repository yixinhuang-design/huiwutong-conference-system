// ============================================
// 强制关闭弹窗的紧急修复脚本
// ============================================
// 复制到控制台运行即可强制关闭弹窗

console.log('========== 强制关闭弹窗 ==========');

// 方法1: 直接移除弹窗DOM
const modal = document.querySelector('.area-config-modal');
if (modal) {
    console.log('✓ 找到弹窗，正在移除...');
    modal.remove();
    console.log('✓ 弹窗已移除');
} else {
    console.log('ℹ️  未找到弹窗元素');
}

// 方法2: 通过Vue实例关闭
const app = document.getElementById('app');
let vm = null;

if (app.__vue_app__) {
    vm = app.__vue_app__.config.globalProperties;
    console.log('✓ 通过 __vue_app__ 获取实例');
} else if (app.__vue__) {
    vm = app.__vue__;
    console.log('✓ 通过 __vue__ 获取实例');
}

if (vm && vm.showAreaConfigModal) {
    console.log('✓ 找到Vue实例，关闭弹窗...');
    vm.showAreaConfigModal = false;
    vm.editingArea = null;
    vm.$forceUpdate();
    console.log('✓ 弹窗状态已重置');
    console.log('✓ 已强制更新视图');
}

// 方法3: 如果上面都不行，手动设置样式
const modalAgain = document.querySelector('.area-config-modal');
if (modalAgain) {
    console.log('⚠️  弹窗仍然存在，强制隐藏...');
    modalAgain.style.display = 'none';
    modalAgain.style.visibility = 'hidden';
    modalAgain.style.opacity = '0';
    console.log('✓ 弹窗已被强制隐藏');
}

console.log('========== 完成 ==========');
console.log('');
console.log('💡 如果弹窗仍然出现，请按 Ctrl + F5 强制刷新页面');

// 将vm保存到window对象，方便后续使用
if (vm) {
    window.vm = vm;
    console.log('💡 提示：现在可以使用 window.vm.closeAreaConfig() 来关闭弹窗');
}
