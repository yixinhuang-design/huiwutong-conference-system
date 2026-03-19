// ============================================
// 深度检查座位数据 - 手动调试脚本
// ============================================
// 在控制台中运行此脚本来找出问题

console.log('========== 开始深度检查座位数据 ==========');

// 获取 Vue 实例
const appDiv = document.getElementById('app');
let vm = null;

if (appDiv.__vue_app__) {
    vm = appDiv.__vue_app__.config.globalProperties;
    console.log('✓ 通过 __vue_app__ 获取实例');
} else if (appDiv.__vue__) {
    vm = appDiv.__vue__;
    console.log('✓ 通过 __vue__ 获取实例');
}

if (!vm) {
    console.error('❌ 无法获取 Vue 实例');
} else {
    console.log('✓ Vue 实例获取成功\n');

    // 检查 seatingAreas 的完整结构
    if (vm.seatingAreas && vm.seatingAreas.length > 0) {
        console.log('检查 seatingAreas 结构：');

        vm.seatingAreas.forEach((area, areaIndex) => {
            console.log(`\n--- 座位区 [${areaIndex}] ---`);
            console.log('area 对象:', area);
            console.log('area.id:', area.id);
            console.log('area.name:', area.name);
            console.log('area.type:', area.type);

            if (area.rows) {
                console.log(`  rows 数量: ${area.rows.length}`);

                area.rows.forEach((row, rowIndex) => {
                    console.log(`\n  行 [${rowIndex}]:`);
                    console.log('    row 对象:', row);
                    console.log('    row.number:', row.number);

                    if (row.seats) {
                        console.log(`    seats 数量: ${row.seats.length}`);

                        // 检查每个座位
                        row.seats.forEach((seat, seatIndex) => {
                            if (!seat) {
                                console.error(`      ❌ seats[${seatIndex}] 是 undefined`);
                            } else if (!seat.id) {
                                console.error(`      ❌ seats[${seatIndex}].id 是 undefined:`, seat);
                            } else {
                                // 正常，不输出以减少日志量
                            }
                        });

                        // 如果没有问题，输出一条确认信息
                        const allValid = row.seats.every(seat => seat && seat.id);
                        if (allValid) {
                            console.log(`      ✓ 所有座位正常`);
                        }
                    } else {
                        console.error(`    ❌ row.seats 是 undefined`);
                    }
                });
            } else {
                console.error('  ❌ area.rows 是 undefined');
            }
        });
    } else {
        console.error('❌ seatingAreas 是空的或 undefined');
    }

    console.log('\n========== 检查完成 ==========');
    console.log('\n💡 如果上述日志中没有发现 undefined，');
    console.log('   问题可能在于：');
    console.log('   1. 某个方法/计算属性返回了 undefined');
    console.log('   2. 某个 v-for 的数据源在渲染时变成了 undefined');
    console.log('   3. Vue 的响应式系统在处理某个属性时出错');
}

// 导出检查函数供后续使用
window.debugSeatingData = function() {
    const appDiv = document.getElementById('app');
    let vm = appDiv.__vue_app__ ? appDiv.__vue_app__.config.globalProperties : appDiv.__vue__;

    if (!vm || !vm.seatingAreas) {
        console.error('无法获取数据');
        return;
    }

    let problemFound = false;

    vm.seatingAreas.forEach((area, ai) => {
        if (!area || !area.id) {
            console.error(`❌ seatingAreas[${ai}] 问题:`, area);
            problemFound = true;
        }

        if (area.rows) {
            area.rows.forEach((row, ri) => {
                if (!row || !row.number) {
                    console.error(`❌ area[${ai}].rows[${ri}] 问题:`, row);
                    problemFound = true;
                }

                if (row.seats) {
                    row.seats.forEach((seat, si) => {
                        if (!seat || !seat.id) {
                            console.error(`❌ area[${ai}].rows[${ri}].seats[${si}] 问题:`, seat);
                            problemFound = true;
                        }
                    });
                }
            });
        }
    });

    if (!problemFound) {
        console.log('✓ 数据检查完成，未发现明显问题');
    }

    return vm;
};

console.log('\n💡 提示：后续可以运行 debugSeatingData() 来快速检查数据');
