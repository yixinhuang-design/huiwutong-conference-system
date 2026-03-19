/**
 * 座位管理系统 - 参会人员导入模块
 * 功能: 通过Excel、报名系统、复制粘贴等多种方式导入参会人员
 * 版本: 2.0
 * 优先级: 2
 */

export const SeatingImportAttendeesModule = {
    /**
     * 初始化模块
     * @param {Object} vm - Vue实例
     * @param {Object} dependencies - 依赖的数据和方法
     */
    init(vm, dependencies) {
        const {
            attendees,
            currentScheduleId,
            loading,
            saveData,
            updateStatistics,
            isVipByPosition
        } = dependencies;

        // ===== 参会人员导入相关状态 =====
        const importModalState = vm.$options.methods?.getImportModalState?.() || {
            show: false,
            method: 'excel',
            sourceScheduleId: null,
            importResults: null
        };

        // ===== 从报名系统导入 =====
        const importFromRegistration = async () => {
            try {
                loading.value = true;

                const registrationData = localStorage.getItem('registrationData');
                if (!registrationData) {
                    console.warn('未找到报名数据，请先在报名系统中添加参会人员');
                    return;
                }

                const data = JSON.parse(registrationData);
                const registrations = data.registrations || [];

                // 筛选已审核通过的人员
                const approved = registrations.filter(r => r.status === 'approved');

                // 转换为参会人员
                attendees.value = approved.map((reg, index) => ({
                    id: `att_${reg.id}`,
                    name: reg.name,
                    department: reg.unit || '未知单位',
                    position: reg.position || '未知职位',
                    isVip: isVipByPosition(reg.position || ''),
                    phone: reg.phone,
                    email: reg.email,
                    registrationId: reg.id
                }));

                saveData();
                updateStatistics();

                console.log(`成功导入 ${attendees.value.length} 名参会人员`);
            } catch (error) {
                console.error('导入失败:', error);
            } finally {
                loading.value = false;
            }
        };

        // ===== 生成模拟参会人员数据 =====
        const generateMockAttendees = async () => {
            const confirmed = await window.InteractionUtils?.showConfirm?.(
                '确定要生成30条模拟参会人员数据吗？这将添加到现有数据中。',
                '生成模拟数据',
                'info'
            ) || confirm('确定要生成30条模拟参会人员数据吗？');

            if (!confirmed) return;

            try {
                loading.value = true;

                const units = [
                    '市委组织部', '市委宣传部', '市教育局', '市财政局', '市卫健委',
                    '市公安局', '市人社局', '市发改委', '市住建局', '市交通局',
                    '市农业农村局', '市商务局', '市文旅局', '市体育局', '市科技局'
                ];

                const departments = ['办公室', '人事科', '财务科', '业务科', '综合处', '管理处', '规划科'];
                const positions = ['处长', '副处长', '科长', '副科长', '科员', '主任', '副主任', '办事员'];

                const mockAttendees = [];
                const vipPositions = ['书记', '市长', '局长', '主任', '部长', '主席'];

                for (let i = 0; i < 30; i++) {
                    const unit = units[Math.floor(Math.random() * units.length)];
                    const department = departments[Math.floor(Math.random() * departments.length)];
                    const position = positions[Math.floor(Math.random() * positions.length)];
                    const isVip = vipPositions.some(keyword => position.includes(keyword)) || (i < 3);

                    mockAttendees.push({
                        id: `mock_${Date.now()}_${i}`,
                        name: getRandomName(),
                        department: unit,
                        position: `${department} - ${position}`,
                        isVip: isVip,
                        phone: '138' + String(Math.floor(Math.random() * 100000000)).padStart(8, '0'),
                        email: `user${i + 1}@example.com`
                    });
                }

                attendees.value = [...attendees.value, ...mockAttendees];

                saveData();
                updateStatistics();

                loading.value = false;
                window.InteractionUtils?.showToast?.(
                    `成功生成30条模拟参会人员数据，当前总数：${attendees.value.length}人`,
                    'success'
                ) || alert(`成功生成30条模拟参会人员数据，当前总数：${attendees.value.length}人`);
            } catch (error) {
                loading.value = false;
                console.error('生成模拟数据失败:', error);
                window.InteractionUtils?.showToast?.('生成模拟数据失败，请重试', 'error') || alert('生成模拟数据失败');
            }
        };

        // ===== 生成随机中文姓名 =====
        const getRandomName = () => {
            const surnames = ['王', '李', '张', '刘', '陈', '杨', '赵', '黄', '周', '吴', '徐', '孙', '胡', '朱', '高', '林', '何', '郭', '马', '罗'];
            const names1 = ['建', '国', '明', '文', '平', '立', '志', '海', '永', '世', '德', '金', '玉', '晓', '光', '春', '善', '家', '正'];
            const names2 = ['强', '军', '伟', '勇', '辉', '杰', '涛', '超', '波', '明', '亮', '华', '平', '建', '国', '明', '文', '平', '立'];

            const surname = surnames[Math.floor(Math.random() * surnames.length)];
            const name1 = names1[Math.floor(Math.random() * names1.length)];
            const name2 = Math.random() > 0.5 ? names2[Math.floor(Math.random() * names2.length)] : '';

            return surname + name1 + name2;
        };

        // ===== Excel导入 =====
        const handleExcelImport = async (event) => {
            const file = event.target.files[0];
            if (!file) return;

            try {
                loading.value = true;

                // 显示读取进度
                window.SeatingOptimization?.progress?.show?.('正在读取Excel文件...', 30);

                // 读取Excel文件
                const data = await file.arrayBuffer();
                const workbook = window.XLSX.read(data, { type: 'array' });

                // 获取第一个工作表
                const firstSheetName = workbook.SheetNames[0];
                const worksheet = workbook.Sheets[firstSheetName];

                // 转换为JSON
                const jsonData = window.XLSX.utils.sheet_to_json(worksheet);

                if (jsonData.length === 0) {
                    window.InteractionUtils?.showToast?.('Excel文件为空，请检查文件内容', 'error') ||
                        alert('Excel文件为空，请检查文件内容');
                    return;
                }

                // 数据验证
                let validData;
                if (window.ExcelDataValidator) {
                    const validator = new window.ExcelDataValidator();
                    const validationResult = validator.validate(jsonData);

                    if (validationResult.errors.length > 0) {
                        window.InteractionUtils?.showToast?.(
                            `数据验证失败：${validationResult.errors[0].message}`,
                            'error'
                        ) || alert(`数据验证失败：${validationResult.errors[0].message}`);
                        return;
                    }

                    if (validationResult.warnings.length > 0) {
                        const warningMsg = `发现${validationResult.warnings.length}条警告，是否继续？`;
                        const confirmed = await window.InteractionUtils?.showConfirm?.(warningMsg) ||
                            confirm(warningMsg);

                        if (!confirmed) {
                            window.InteractionUtils?.showToast?.('已取消导入', 'info');
                            return;
                        }
                    }

                    validData = validationResult.validData;
                } else {
                    validData = jsonData;
                }

                window.SeatingOptimization?.progress?.update?.(60, '正在导入数据...');

                // 根据导入模式处理
                const excelImportMode = vm.$options.methods?.getExcelImportMode?.() || 'list';
                if (excelImportMode === 'list') {
                    await importAttendeeListFromExcel(validData);
                } else {
                    await importSeatingFromExcel(validData);
                }

                saveData();
                updateStatistics();

                const successMsg = `成功导入 ${validData.length} 条参会数据`;
                window.InteractionUtils?.showToast?.(successMsg, 'success') || alert(successMsg);
                window.SeatingOptimization?.progress?.update?.(100, '导入完成');

            } catch (error) {
                const errorMsg = `Excel导入失败: ${error.message || '未知错误'}`;
                window.InteractionUtils?.showToast?.(errorMsg, 'error') || alert(errorMsg);
                console.error('Excel导入失败:', error);
            } finally {
                loading.value = false;
                setTimeout(() => {
                    window.SeatingOptimization?.progress?.hide?.();
                }, 500);
                event.target.value = '';
            }
        };

        // ===== 仅导入参会名单 =====
        const importAttendeeListFromExcel = async (jsonData) => {
            const newAttendees = jsonData.map((row, index) => {
                const name = row['姓名'] || row['name'] || row['Name'] || '';
                const department = row['单位'] || row['部门'] || row['department'] || '未知单位';
                const position = row['职位'] || row['职务'] || row['position'] || '未知职位';
                const phone = row['手机'] || row['电话'] || row['phone'] || '';
                const email = row['邮箱'] || row['email'] || '';

                return {
                    id: `att_excel_${Date.now()}_${index}`,
                    name,
                    department,
                    position,
                    phone,
                    email,
                    scheduleId: currentScheduleId.value,
                    isVip: isVipByPosition(position)
                };
            }).filter(att => att.name);

            attendees.value = [...attendees.value, ...newAttendees];

            if (currentScheduleId.value) {
                const attendeesBySchedule = window.SeatingOptimization?.attendeesBySchedule || {};
                attendeesBySchedule[currentScheduleId.value] = JSON.parse(JSON.stringify(attendees.value));
            }
        };

        // ===== 导入名单+座位号 =====
        const importSeatingFromExcel = async (jsonData) => {
            const seatingAreas = vm.$options.methods?.getSeatingAreas?.() || [];
            
            if (seatingAreas.length === 0) {
                console.warn('请先创建座位区域');
                return;
            }

            // 清空现有座位分配
            clearAllSeatsInternal(seatingAreas);

            const newAttendees = [];

            for (let i = 0; i < jsonData.length; i++) {
                const row = jsonData[i];

                const name = row['姓名'] || row['name'] || '';
                const department = row['单位'] || row['部门'] || '未知单位';
                const position = row['职位'] || row['职务'] || '未知职位';
                const phone = row['手机'] || row['电话'] || '';
                const email = row['邮箱'] || '';

                const seatArea = row['区域'] || row['座位区'] || '';
                const seatRow = parseInt(row['排'] || row['行'] || row['row'] || '');
                const seatCol = parseInt(row['号'] || row['列'] || row['col'] || '');

                if (!name) continue;

                const attendee = {
                    id: `att_excel_${Date.now()}_${i}`,
                    name,
                    department,
                    position,
                    phone,
                    email,
                    scheduleId: currentScheduleId.value,
                    isVip: isVipByPosition(position)
                };

                newAttendees.push(attendee);

                if (!isNaN(seatRow) && !isNaN(seatCol)) {
                    await assignSeatByPosition(attendee, seatArea, seatRow, seatCol, seatingAreas);
                }
            }

            attendees.value = newAttendees;

            if (currentScheduleId.value) {
                const attendeesBySchedule = window.SeatingOptimization?.attendeesBySchedule || {};
                attendeesBySchedule[currentScheduleId.value] = JSON.parse(JSON.stringify(attendees.value));
            }
        };

        // ===== 根据位置分配座位 =====
        const assignSeatByPosition = async (attendee, areaName, row, col, seatingAreas) => {
            let targetArea = null;
            if (areaName) {
                targetArea = seatingAreas.find(a => a && a.name.includes(areaName));
            }

            if (!targetArea && seatingAreas.length > 0) {
                targetArea = seatingAreas[0];
            }

            if (!targetArea || !targetArea.rows) {
                console.warn(`未找到座位区: ${areaName}，参会者 ${attendee.name} 未分配座位`);
                return;
            }

            const targetRow = targetArea.rows.find(r => r && r.number === row);
            if (!targetRow || !targetRow.seats) {
                console.warn(`未找到第 ${row} 排，参会者 ${attendee.name} 未分配座位`);
                return;
            }

            const targetSeat = targetRow.seats.find(s => s && s.number === col);
            if (!targetSeat) {
                console.warn(`未找到 ${row}排${col}号，参会者 ${attendee.name} 未分配座位`);
                return;
            }

            if (targetSeat.isAisle) {
                console.warn(`${row}排${col}号是过道，参会者 ${attendee.name} 未分配座位`);
                return;
            }

            if (targetSeat.status === 'available') {
                targetSeat.status = 'occupied';
                targetSeat.occupant = attendee;
            } else {
                console.warn(`${row}排${col}号已被占用，参会者 ${attendee.name} 未分配座位`);
            }
        };

        // ===== 清空所有座位分配（内部方法） =====
        const clearAllSeatsInternal = (seatingAreas) => {
            seatingAreas.forEach(area => {
                if (area && area.rows) {
                    area.rows.forEach(row => {
                        if (row && row.seats) {
                            row.seats.forEach(seat => {
                                if (seat) {
                                    seat.status = 'available';
                                    seat.occupant = null;
                                    if (seat.type === 'vip' && !seat.occupant) {
                                        seat.type = 'regular';
                                    }
                                }
                            });
                        }
                    });
                }
            });
        };

        // ===== 导出公开的方法 =====
        return {
            importFromRegistration,
            generateMockAttendees,
            handleExcelImport,
            importAttendeeListFromExcel,
            importSeatingFromExcel,
            assignSeatByPosition,
            clearAllSeatsInternal,
            importModalState
        };
    }
};

export default SeatingImportAttendeesModule;
