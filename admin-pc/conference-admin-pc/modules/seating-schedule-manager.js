/**
 * 多日程管理模块
 * 
 * 功能：
 * - 日程列表加载与切换
 * - 日程配置管理（日期、时间、场地）
 * - 日程座位数据隔离存储（各日程独立的座位配置和参会人员）
 * - 日程间数据切换与保存
 * 
 * 导出Composable:
 * - useScheduleManager(conference, refFn?, reactiveFn?): 返回所有日程管理状态和方法
 * 
 * 注意：Vue的ref和reactive由调用方（seating-mgr.html）注入
 * 这避免了在ESM浏览器环境中直接导入'vue'的问题
 */

/**
 * 创建日程管理Composable
 * 
 * @param {object} context - 上下文 {conference, seatingAreas, attendees, ...}
 * @param {Function} injectedRef - Vue ref函数（由主文件注入）
 * @param {Function} injectedReactive - Vue reactive函数（由主文件注入）
 * @returns {object} 包含状态和方法的对象
 */
export function useScheduleManager(context = {}, injectedRef, injectedReactive) {
    // 获取Vue API引用（从注入或从全局Vue对象）
    const refFn = injectedRef || (typeof window !== 'undefined' && window.Vue?.ref) || ((val) => ({ value: val }));
    const reactiveFn = injectedReactive || (typeof window !== 'undefined' && window.Vue?.reactive) || ((obj) => obj);
    
    // ===== 日程管理状态 =====
    const schedulesList = refFn([]);
    const currentScheduleId = refFn(null);
    const currentSchedule = reactiveFn({
        id: null,
        name: '',
        date: '',
        timeStart: '',
        timeEnd: '',
        venueId: null,
        venueName: '',
        capacity: 0,
        status: 'draft'
    });
    
    // 多日程的座位和参会人员隔离存储
    const scheduleHistories = reactiveFn({});      // 日程排座历史快照
    const attendeesBySchedule = reactiveFn({});    // 按日程分类的参会人员
    const seatingAreasBySchedule = reactiveFn({});  // 按日程分类的座位配置
    
    // 日程切换确认对话框
    const showScheduleModal = refFn(false);
    const showScheduleConfirmModal = refFn(false);
    const scheduleChangeConfirm = reactiveFn({
        show: false,
        scheduleId: null,
        scheduleName: '',
        venueName: '',
        capacity: 0,
        attendeeCount: 0,
        assignedCount: 0
    });

    /**
     * 加载日程列表（从后端API加载）
     */
    const loadSchedules = async () => {
        try {
            // 从后端API加载日程数据
            // 处理context.conference可能是Ref对象的情况
            const conferenceId = context.conference?.value?.id || context.conference?.id;
            if (conferenceId) {
                // 构建请求头，携带认证Token以支持多租户数据隔离
                const scheduleHeaders = { 'Content-Type': 'application/json' };
                const token = localStorage.getItem('authToken');
                if (token) {
                    scheduleHeaders['Authorization'] = `Bearer ${token}`;
                }
                const response = await fetch(
                    `/api/schedule/all?meetingId=${conferenceId}`,
                    { 
                        method: 'GET',
                        headers: scheduleHeaders
                    }
                );
                
                if (response.ok) {
                    const data = await response.json();
                    const schedulesData = data.data || data || [];
                    
                    // 转换API响应为前端格式
                    schedulesList.value = Array.isArray(schedulesData) ? schedulesData.map(schedule => ({
                        id: schedule.id,
                        name: schedule.title || schedule.name,
                        date: schedule.scheduleDate || schedule.date,
                        timeStart: schedule.startTime || schedule.timeStart,
                        timeEnd: schedule.endTime || schedule.timeEnd,
                        venueId: schedule.venueId || 'venue_a',
                        venueName: schedule.venueName || schedule.location || '会场A',
                        capacity: schedule.capacity || 500,
                        attendeeCount: schedule.participantCount || 0,
                        assignedCount: 0,
                        status: schedule.status || 'draft'
                    })) : [];
                    
                    console.log(`✅ 从API加载日程成功，共${schedulesList.value.length}个日程`);
                } else {
                    console.warn('从API加载日程失败，使用本地模拟数据');
                    loadSchedulesFromLocal();
                }
            } else {
                console.warn('会议信息不完整，使用本地模拟数据');
                loadSchedulesFromLocal();
            }

            // 如果没有选中日程，默认选中第一个
            if (!currentScheduleId.value && schedulesList.value.length > 0) {
                const firstSchedule = schedulesList.value[0];
                currentScheduleId.value = firstSchedule.id;
                Object.assign(currentSchedule, firstSchedule);
                // 注意：这里只设置 currentScheduleId，不加载座位数据
                // 座位数据由 onMounted 中的 loadSeatingData() 统一加载
                console.log(`✅ 页面加载时，已自动加载日程：${firstSchedule.name}`);
            }
        } catch (error) {
            console.error('❌ 加载日程列表失败:', error);
            loadSchedulesFromLocal();
        }
    };

    /**
     * 本地模拟日程数据（降级方案）
     */
    const loadSchedulesFromLocal = () => {
        schedulesList.value = [
            {
                id: 'sched_001',
                name: '开幕式',
                date: '2026-06-15',
                timeStart: '09:00',
                timeEnd: '11:30',
                venueId: 'venue_a',
                venueName: '大会场',
                capacity: 800,
                attendeeCount: 750,
                assignedCount: 0,
                status: 'draft'
            },
            {
                id: 'sched_002',
                name: '主题演讲',
                date: '2026-06-16',
                timeStart: '09:00',
                timeEnd: '11:30',
                venueId: 'venue_b',
                venueName: '分会场1',
                capacity: 200,
                attendeeCount: 180,
                assignedCount: 0,
                status: 'draft'
            },
            {
                id: 'sched_003',
                name: '闭幕式',
                date: '2026-06-17',
                timeStart: '14:00',
                timeEnd: '16:00',
                venueId: 'venue_a',
                venueName: '大会场',
                capacity: 800,
                attendeeCount: 740,
                assignedCount: 0,
                status: 'draft'
            }
        ];
    };

    /**
     * 选择日程（打开确认对话框）
     */
    const selectSchedule = async (schedule) => {
        try {
            // 填充确认对话框数据
            scheduleChangeConfirm.scheduleId = schedule.id;
            scheduleChangeConfirm.scheduleName = schedule.name;
            scheduleChangeConfirm.venueName = schedule.venueName;
            scheduleChangeConfirm.capacity = schedule.capacity;
            scheduleChangeConfirm.attendeeCount = schedule.attendeeCount;
            scheduleChangeConfirm.assignedCount = schedule.assignedCount;
            
            // 关闭日程列表modal，显示确认modal
            showScheduleModal.value = false;
            scheduleChangeConfirm.show = true;
            
            console.log(`✅ 已选择日程：${schedule.name}，等待用户确认`);
        } catch (error) {
            console.error('❌ 选择日程失败:', error);
        }
    };

    /**
     * 确认日程切换
     */
    const confirmScheduleChange = async (seatingAreas, attendees, updateStatistics) => {
        try {
            const newScheduleId = scheduleChangeConfirm.scheduleId;

            // 步骤1：保存当前日程的排座结果
            if (currentScheduleId.value) {
                await saveScheduleSeatingResult(
                    currentScheduleId.value,
                    seatingAreas.value,
                    attendees.value
                );
            }

            // 步骤2：更新当前日程
            currentScheduleId.value = newScheduleId;

            // 步骤3：加载新日程的数据
            const schedule = schedulesList.value.find(s => s.id === newScheduleId);
            if (schedule) {
                Object.assign(currentSchedule, schedule);

                // 加载或创建该日程的座位配置和参会人员
                await loadScheduleSeatingData(newScheduleId, seatingAreas, attendees);

                // 更新统计信息
                if (updateStatistics) updateStatistics();

                console.log(`✅ 已切换到日程：${schedule.name}`);
            }

            // 步骤4：关闭确认对话框
            scheduleChangeConfirm.show = false;
            showScheduleModal.value = false;
        } catch (error) {
            console.error('❌ 切换日程失败:', error);
        }
    };

    /**
     * 保存当前日程的排座结果
     */
    const saveScheduleSeatingResult = async (scheduleId, seatingAreas, attendees) => {
        try {
            // 处理context.conference可能是Ref对象的情况
            const conference = (context.conference?.value || context.conference) || { id: 'default' };
            const result = {
                scheduleId,
                seatingAreas: JSON.parse(JSON.stringify(seatingAreas || [])),
                attendees: JSON.parse(JSON.stringify(attendees || [])),
                timestamp: new Date().toISOString()
            };

            // 保存到历史记录
            scheduleHistories[scheduleId] = result;

            // 保存到localStorage
            const key = `schedule_seating_${conference.id}_${scheduleId}`;
            localStorage.setItem(key, JSON.stringify(result));

            console.log(`✅ 已保存日程${scheduleId}的排座结果`);
        } catch (error) {
            console.error('❌ 保存排座结果失败:', error);
        }
    };

    /**
     * 加载日程的座位数据
     */
    const loadScheduleSeatingData = async (scheduleId, seatingAreas, attendees) => {
        try {
            // 处理context.conference可能是Ref对象的情况
            const conference = (context.conference?.value || context.conference) || { id: 'default' };
            const key = `schedule_seating_${conference.id}_${scheduleId}`;
            const savedData = localStorage.getItem(key);

            if (savedData) {
                // 恢复之前保存的数据
                const data = JSON.parse(savedData);
                
                if (seatingAreas) {
                    seatingAreas.value = data.seatingAreas || [];
                }
                if (attendees) {
                    attendees.value = data.attendees || [];
                }
                
                // 同时更新隔离存储
                seatingAreasBySchedule[scheduleId] = JSON.parse(JSON.stringify(data.seatingAreas || []));
                attendeesBySchedule[scheduleId] = JSON.parse(JSON.stringify(data.attendees || []));
            } else {
                // 创建新的日程数据
                const schedule = schedulesList.value.find(s => s.id === scheduleId);
                if (schedule) {
                    // 生成该日程的参会人员
                    const newAttendees = generateAttendeesForSchedule(scheduleId, schedule.attendeeCount);
                    
                    if (attendees) {
                        attendees.value = newAttendees;
                    }
                    
                    // 保存到隔离存储
                    attendeesBySchedule[scheduleId] = JSON.parse(JSON.stringify(newAttendees));
                    
                    // 初始化座位区（由父组件提供）
                    if (seatingAreas && seatingAreas.value.length === 0) {
                        console.log(`📝 日程${scheduleId}的座位区需要由父组件初始化`);
                    }
                }
            }

            console.log(`✅ 已加载日程${scheduleId}的数据`);
        } catch (error) {
            console.error('❌ 加载日程数据失败:', error);
        }
    };

    /**
     * 为日程生成参会人员
     */
    const generateAttendeesForSchedule = (scheduleId, count) => {
        const departments = ['办公室', '财政局', '水利部', '交通运输局', '自然资源局'];
        const positions = ['主任', '副局长', '科长', '科员'];
        const attendees = [];

        for (let i = 0; i < count; i++) {
            attendees.push({
                id: `att_${scheduleId}_${i + 1}`,
                name: `参会人${i + 1}`,
                department: departments[i % departments.length],
                position: positions[i % positions.length],
                isVip: i < 2,
                company: '会议组织方',
                scheduleId: scheduleId  // 关键：标记所属日程
            });
        }

        return attendees;
    };

    /**
     * 打开日程选择对话框
     */
    const openScheduleSelector = () => {
        showScheduleModal.value = true;
    };

    /**
     * 关闭日程选择对话框
     */
    const closeScheduleSelector = () => {
        showScheduleModal.value = false;
    };

    // 返回公共接口
    return {
        // 状态
        schedulesList,
        currentScheduleId,
        currentSchedule,
        scheduleHistories,
        attendeesBySchedule,
        seatingAreasBySchedule,
        showScheduleModal,
        showScheduleConfirmModal,
        scheduleChangeConfirm,
        
        // 方法
        loadSchedules,
        selectSchedule,
        confirmScheduleChange,
        saveScheduleSeatingResult,
        loadScheduleSeatingData,
        generateAttendeesForSchedule,
        openScheduleSelector,
        closeScheduleSelector
    };
}
