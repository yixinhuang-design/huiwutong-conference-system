// ============================================
// 临时解决方案：在模板渲染前添加数据验证
// ============================================
// 复制整个代码块到控制台运行

// 获取 Vue 实例
const app = document.getElementById('app');
const vm = app.__vue_app__ ? app.__vue_app__.config.globalProperties : app.__vue__;

if (vm && vm.seatingAreas) {
    console.log('开始清理和修复数据...');

    // 修复 seatingAreas 数据
    vm.seatingAreas = vm.seatingAreas.filter(area => area && area.id).map(area => {
        // 修复 rows
        if (area.rows) {
            area.rows = area.rows.filter(row => row && row.number).map(row => {
                // 修复 seats
                if (row.seats) {
                    row.seats = row.seats.filter(seat => seat && seat.id);
                }
                return row;
            });
        }
        return area;
    });

    console.log('✓ 数据清理完成');
    console.log('seatingAreas:', vm.seatingAreas.length);

    // 强制更新
    vm.$forceUpdate();
    console.log('✓ 已强制更新视图');

    // 检查其他数组
    ['attendees', 'algorithms', 'transportList', 'accommodationList', 'discussionList', 'conflicts'].forEach(arrName => {
        if (vm[arrName]) {
            const originalLength = vm[arrName].length;
            vm[arrName] = vm[arrName].filter(item => item && item.id);
            if (vm[arrName].length !== originalLength) {
                console.log(`⚠️  ${arrName}: 清理了 ${originalLength - vm[arrName].length} 个无效元素`);
            }
        }
    });

    console.log('\n✓ 所有数据已清理，错误应该已解决');
    console.log('💡 如果仍有错误，请刷新页面');
} else {
    console.error('❌ 无法获取 Vue 实例');
}
