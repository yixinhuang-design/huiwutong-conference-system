// ============================================
// 捕获 Vue 渲染错误 - 调试脚本
// ============================================
// 使用方法：在 seating-mgr.html 的 <head> 中添加此脚本
// 或者在控制台中运行

// 1. 覆盖 Vue 的错误处理
window.addEventListener('error', function(event) {
    console.error('========== 全局错误捕获 ==========');
    console.error('错误消息:', event.message);
    console.error('错误文件:', event.filename);
    console.error('错误行号:', event.lineno);
    console.error('错误列号:', event.colno);
    console.error('错误堆栈:', event.error && event.error.stack);
    console.error('====================================');
});

// 2. 监听未捕获的 Promise 错误
window.addEventListener('unhandledrejection', function(event) {
    console.error('========== 未捕获的 Promise 错误 ==========');
    console.error('错误原因:', event.reason);
    console.error('========================================');
});

// 3. 检查所有可能有问题的数据
console.log('========== 数据完整性检查 ==========');

// 等待 Vue 挂载
setTimeout(() => {
    const app = document.getElementById('app');
    if (app && app.__vue__) {
        const vm = app.__vue__;

        // 检查 seatingAreas
        console.log('检查 seatingAreas:');
        if (vm.seatingAreas) {
            vm.seatingAreas.forEach((area, index) => {
                if (!area) {
                    console.error(`❌ seatingAreas[${index}] 是 undefined`);
                } else if (!area.id) {
                    console.error(`❌ seatingAreas[${index}].id 是 undefined`, area);
                } else {
                    console.log(`✓ seatingAreas[${index}]:`, area.id);

                    // 检查 rows
                    if (area.rows) {
                        area.rows.forEach((row, rowIndex) => {
                            if (!row) {
                                console.error(`❌ area.rows[${rowIndex}] 是 undefined`);
                            } else if (!row.number) {
                                console.error(`❌ area.rows[${rowIndex}].number 是 undefined`, row);
                            } else {
                                // 检查 seats
                                if (row.seats) {
                                    row.seats.forEach((seat, seatIndex) => {
                                        if (!seat) {
                                            console.error(`❌ area.rows[${rowIndex}].seats[${seatIndex}] 是 undefined`);
                                        } else if (!seat.id) {
                                            console.error(`❌ area.rows[${rowIndex}].seats[${seatIndex}].id 是 undefined`, seat);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        } else {
            console.error('❌ seatingAreas 是 undefined');
        }

        // 检查 attendees
        console.log('\n检查 attendees:');
        if (vm.attendees) {
            vm.attendees.forEach((attendee, index) => {
                if (!attendee) {
                    console.error(`❌ attendees[${index}] 是 undefined`);
                } else if (!attendee.id) {
                    console.error(`❌ attendees[${index}].id 是 undefined`, attendee);
                } else {
                    console.log(`✓ attendees[${index}]:`, attendee.id);
                }
            });
        } else {
            console.error('❌ attendees 是 undefined');
        }

        // 检查 algorithms
        console.log('\n检查 algorithms:');
        if (vm.algorithms) {
            vm.algorithms.forEach((algo, index) => {
                if (!algo) {
                    console.error(`❌ algorithms[${index}] 是 undefined`);
                } else if (!algo.id) {
                    console.error(`❌ algorithms[${index}].id 是 undefined`, algo);
                } else {
                    console.log(`✓ algorithms[${index}]:`, algo.id);
                }
            });
        } else {
            console.warn('⚠️  algorithms 是 undefined');
        }

        // 检查其他数组
        const arraysToCheck = [
            'transportList',
            'accommodationList',
            'discussionList',
            'conflicts'
        ];

        arraysToCheck.forEach(arrayName => {
            console.log(`\n检查 ${arrayName}:`);
            if (vm[arrayName]) {
                vm[arrayName].forEach((item, index) => {
                    if (!item) {
                        console.error(`❌ ${arrayName}[${index}] 是 undefined`);
                    } else if (!item.id) {
                        console.error(`❌ ${arrayName}[${index}].id 是 undefined`, item);
                    }
                });
            } else {
                console.log(`ℹ️  ${arrayName} 是 undefined 或为空`);
            }
        });

    } else {
        console.error('❌ 无法获取 Vue 实例');
    }

    console.log('====================================');
}, 2000);

// 4. 提供手动检查函数
window.checkVueData = function() {
    const app = document.getElementById('app');
    if (!app) {
        console.error('找不到 #app 元素');
        return;
    }

    let vm = null;
    if (app.__vue_app__) {
        vm = app.__vue_app__.config.globalProperties;
        console.log('✓ 通过 __vue_app__ 获取实例');
    } else if (app.__vue__) {
        vm = app.__vue__;
        console.log('✓ 通过 __vue__ 获取实例');
    }

    if (!vm) {
        console.error('无法获取 Vue 实例');
        return;
    }

    console.log('========== 当前数据状态 ==========');
    console.log('seatingAreas:', vm.seatingAreas);
    console.log('attendees:', vm.attendees);
    console.log('algorithms:', vm.algorithms);
    console.log('================================');

    return vm;
};

console.log('✓ 调试脚本已加载');
console.log('💡 提示：等待2秒后自动检查数据，或手动运行 checkVueData()');
