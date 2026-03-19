// ============================================
// 页面诊断脚本 - 检查所有模块是否正确渲染
// ============================================
// 在浏览器控制台中运行此脚本来诊断问题

console.log('========== 开始页面诊断 ==========');

// 1. 检查 Vue 实例
const appDiv = document.getElementById('app');
console.log('1. Vue 容器:', appDiv);

let vueApp = null;
if (appDiv.__vue_app__) {
    vueApp = appDiv.__vue_app__.config.globalProperties;
    console.log('✓ Vue 3 应用已挂载');
} else if (appDiv.__vue__) {
    vueApp = appDiv.__vue__;
    console.log('✓ Vue 2 应用已挂载');
} else {
    console.error('❌ Vue 应用未挂载');
}

if (vueApp) {
    // 2. 检查数据状态
    console.log('\n2. 数据状态:');
    console.log('   conference:', vueApp.conference);
    console.log('   venue:', vueApp.venue);
    console.log('   seatingAreas 数量:', vueApp.seatingAreas?.length || 0);
    console.log('   attendees 数量:', vueApp.attendees?.length || 0);
    console.log('   algorithms 数量:', vueApp.algorithms?.length || 0);

    // 3. 检查关键模块的 DOM 元素
    console.log('\n3. DOM 元素检查:');
    const modules = [
        { name: '会场信息设置', selector: '.venue-config' },
        { name: '座位区列表', selector: '.area-list' },
        { name: '参会人员管理', selector: '.attendees-manage' },
        { name: '排座方式', selector: '.seating-method' },
        { name: '算法选择', selector: '.algorithm-selection' },
        { name: '座位图编辑', selector: '.seating-editor' },
        { name: '排座统计', selector: '.seating-statistics' },
        { name: '辅助安排', selector: '.auxiliary-arrangement' }
    ];

    modules.forEach(module => {
        const element = document.querySelector(module.selector);
        if (element) {
            const isVisible = element.offsetWidth > 0 && element.offsetHeight > 0;
            const display = window.getComputedStyle(element).display;
            console.log(`   ✓ ${module.name}: 存在, 可见=${isVisible}, display=${display}`);
        } else {
            console.error(`   ❌ ${module.name}: 未找到 DOM 元素`);
        }
    });

    // 4. 检查 glass-card 元素
    console.log('\n4. Glass-card 元素:');
    const glassCards = document.querySelectorAll('.glass-card');
    console.log(`   找到 ${glassCards.length} 个 glass-card 元素`);
    glassCards.forEach((card, index) => {
        const isVisible = card.offsetWidth > 0 && card.offsetHeight > 0;
        const display = window.getComputedStyle(card).display;
        const title = card.querySelector('h3')?.textContent || '(无标题)';
        console.log(`   [${index + 1}] ${title}: 可见=${isVisible}, display=${display}`);
    });

    // 5. 检查 page-with-nav 容器
    console.log('\n5. 主容器检查:');
    const pageWithNav = document.querySelector('.page-with-nav');
    if (pageWithNav) {
        const isVisible = pageWithNav.offsetWidth > 0 && pageWithNav.offsetHeight > 0;
        const display = window.getComputedStyle(pageWithNav).display;
        console.log(`   page-with-nav: 可见=${isVisible}, display=${display}`);
        console.log(`   offsetWidth: ${pageWithNav.offsetWidth}`);
        console.log(`   offsetHeight: ${pageWithNav.offsetHeight}`);
    } else {
        console.error('   ❌ page-with-nav 容器未找到');
    }
}

console.log('\n========== 诊断完成 ==========');

// 保存 vueApp 到全局变量，方便后续调试
if (vueApp) {
    window.vueApp = vueApp;
    console.log('\n💡 提示：可以使用 window.vueApp 访问 Vue 实例');
    console.log('   例如：window.vueApp.seatingAreas');
}
