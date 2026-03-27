// ============================================
// 深度数据检查脚本 - 查找 undefined 元素
// ============================================
// 在浏览器控制台运行此脚本

console.log('========== 深度数据检查 ==========');

const app = document.getElementById('app');
let vm = null;

if (app.__vue_app__) {
    vm = app.__vue_app__.config.globalProperties;
} else if (app.__vue__) {
    vm = app.__vue__;
}

if (!vm) {
    console.error('❌ 无法获取 Vue 实例');
} else {
    console.log('✓ Vue 实例获取成功');

    // 深度检查 seatingAreas
    console.log('\n========== seatingAreas 深度检查 ==========');
    if (vm.seatingAreas && vm.seatingAreas.length > 0) {
        vm.seatingAreas.forEach((area, areaIndex) => {
            console.log(`\n--- 座位区 [${areaIndex}] ---`);
            console.log('area 对象:', area);
            console.log('area.id:', area.id);
            console.log('area.name:', area.name);
            console.log('area.rows 类型:', typeof area.rows);
            console.log('area.rows 是否为数组:', Array.isArray(area.rows));
            console.log('area.rows 长度:', area.rows ? area.rows.length : 'N/A');

            if (area.rows && Array.isArray(area.rows)) {
                area.rows.forEach((row, rowIndex) => {
                    console.log(`\n  行 [${rowIndex}]:`);
                    console.log('    row 对象:', row);
                    console.log('    row.number:', row.number);
                    console.log('    row.seats 类型:', typeof row.seats);
                    console.log('    row.seats 是否为数组:', Array.isArray(row.seats));
                    console.log('    row.seats 长度:', row.seats ? row.seats.length : 'N/A');

                    if (row.seats && Array.isArray(row.seats)) {
                        row.seats.forEach((seat, seatIndex) => {
                            if (!seat) {
                                console.error(`      ❌ seats[${seatIndex}] 是 undefined!`);
                            } else if (!seat.id) {
                                console.error(`      ❌ seats[${seatIndex}].id 是 undefined:`, seat);
                            } else if (!seat.number && seat.number !== 0) {
                                console.warn(`      ⚠️  seats[${seatIndex}].number 是 undefined:`, seat);
                            }
                        });

                        // 检查是否有 undefined
                        const hasUndefined = row.seats.some(seat => !seat);
                        if (hasUndefined) {
                            console.error(`      ❌ 该行包含 undefined 座位!`);
                        } else {
                            console.log(`      ✓ 该行所有座位正常`);
                        }
                    }
                });

                // 检查 rows 是否有 undefined
                const hasUndefinedRows = area.rows.some(row => !row);
                if (hasUndefinedRows) {
                    console.error(`  ❌ 该区域包含 undefined 行!`);
                }
            }
        });

        // 检查 seatingAreas 是否有 undefined
        const hasUndefinedAreas = vm.seatingAreas.some(area => !area);
        if (hasUndefinedAreas) {
            console.error(`\n❌ seatingAreas 包含 undefined 元素!`);
        } else {
            console.log(`\n✓ seatingAreas 结构完整`);
        }
    } else {
        console.error('❌ seatingAreas 为空或不存在');
    }

    // 检查其他关键数据
    console.log('\n========== 其他数据检查 ==========');
    console.log('conference:', vm.conference);
    console.log('venue:', vm.venue);
    console.log('algorithms:', vm.algorithms);
    console.log('departmentDistribution:', vm.departmentDistribution);

    // 检查是否有空值
    console.log('\n========== 空值检查 ==========');
    console.log('conference.id:', vm.conference?.id);
    console.log('venue.name:', vm.venue?.name);
    console.log('algorithms[0].id:', vm.algorithms?.[0]?.id);
}

console.log('\n========== 检查完成 ==========');
