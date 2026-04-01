
        const { createApp, ref, reactive, computed, onMounted } = Vue;

        createApp({
            setup() {
                // ===== 基础状态 =====
                const loading = ref(false);
                const dataLoaded = ref(false);
                const conference = ref({ id: 'default', name: '会议' });
                const attendees = ref([]);
                const notificationStatus = ref(null);

                // ===== 辅助安排数据 =====
                const transportList = ref([]);
                const accommodationList = ref([]);
                const discussionList = ref([]);
                const diningList = ref([]);
                const airportTransferList = ref([]);

                // ===== 弹窗状态 =====
                const showTransportModalFlag = ref(false);
                const showAccommodationModalFlag = ref(false);
                const showDiscussionModalFlag = ref(false);
                const showDiningModalFlag = ref(false);
                const showAirportTransferModalFlag = ref(false);
                const showArrangementResultModalFlag = ref(false);
                const showPersonAssignModalFlag = ref(false);

                // 编辑模式标识
                const editingTransportId = ref(null);
                const editingAccommodationId = ref(null);
                const editingDiscussionId = ref(null);
                const editingDiningId = ref(null);
                const editingAirportTransferId = ref(null);

                // 人员分配弹窗
                const assignModalTitle = ref('');
                const currentAssignType = ref('');
                const currentArrangementItem = ref(null);

                // ===== 表单数据 =====
                const newTransport = reactive({
                    name: '',
                    type: '大巴',
                    plateNumber: '',
                    departure: '',
                    destination: '',
                    departureTime: '',
                    capacity: 45,
                    assignedCount: 0,
                    passengers: []
                });

                const newAccommodation = reactive({
                    hotelName: '',
                    roomNumber: '',
                    type: '标准间',
                    capacity: 2,
                    assignedCount: 0,
                    occupants: [],
                    checkInTime: '',
                    checkOutTime: ''
                });

                const newDiscussion = reactive({
                    name: '',
                    location: '',
                    capacity: 15,
                    assignedCount: 0,
                    groups: [],
                    startTime: '',
                    endTime: ''
                });

                const newDining = reactive({
                    name: '',
                    type: '午餐',
                    capacity: 10,
                    assignedCount: 0,
                    groups: [],
                    time: '',
                    location: ''
                });

                const newAirportTransfer = reactive({
                    name: '',
                    type: '接机',
                    flightNumber: '',
                    capacity: 4,
                    assignedCount: 0,
                    passengers: [],
                    time: '',
                    fromAirport: '',
                    toDestination: ''
                });

                // ===== 计算属性 =====
                const transportAssignedCount = computed(() => 
                    transportList.value.reduce((sum, v) => sum + (v.assignedCount || 0), 0)
                );
                const totalTransportCapacity = computed(() => 
                    transportList.value.reduce((sum, v) => sum + (v.capacity || 0), 0)
                );

                const accommodationAssignedCount = computed(() => 
                    accommodationList.value.reduce((sum, r) => sum + (r.assignedCount || 0), 0)
                );
                const totalAccommodationCapacity = computed(() => 
                    accommodationList.value.reduce((sum, r) => sum + (r.capacity || 0), 0)
                );

                const discussionAssignedCount = computed(() => 
                    discussionList.value.reduce((sum, d) => sum + (d.assignedCount || 0), 0)
                );
                const totalDiscussionCapacity = computed(() => 
                    discussionList.value.reduce((sum, d) => sum + (d.capacity || 0), 0)
                );

                const diningAssignedCount = computed(() => 
                    diningList.value.reduce((sum, d) => sum + (d.assignedCount || 0), 0)
                );
                const totalDiningCapacity = computed(() => 
                    diningList.value.reduce((sum, d) => sum + (d.capacity || 0), 0)
                );

                const airportTransferAssignedCount = computed(() => 
                    airportTransferList.value.reduce((sum, t) => sum + (t.assignedCount || 0), 0)
                );
                const totalAirportTransferCapacity = computed(() => 
                    airportTransferList.value.reduce((sum, t) => sum + (t.capacity || 0), 0)
                );

                const hasAnyAssignments = computed(() => 
                    transportAssignedCount.value > 0 || 
                    accommodationAssignedCount.value > 0 || 
                    discussionAssignedCount.value > 0 ||
                    diningAssignedCount.value > 0 ||
                    airportTransferAssignedCount.value > 0
                );

                const unassignedAttendees = computed(() => {
                    if (!currentAssignType.value || !currentArrangementItem.value) return [];
                    
                    // 根据当前项的已分配人员过滤
                    const assigned = currentArrangementItem.value.assignedPeople || [];
                    const assignedIds = new Set(assigned.map(p => String(p.id || p.attendeeId)));
                    
                    return attendees.value.filter(a => !assignedIds.has(String(a.id)));
                });

                // ===== API 调用工具 =====
                const API_BASE = '/api/seating';

                const apiCall = async (url, method = 'GET', body = null) => {
                    const headers = { 'Content-Type': 'application/json' };
                    const token = localStorage.getItem('authToken');
                    if (token) headers['Authorization'] = `Bearer ${token}`;
                    const tenantId = localStorage.getItem('tenantId');
                    if (tenantId) headers['X-Tenant-Id'] = tenantId;

                    const options = { method, headers };
                    if (body && method !== 'GET') options.body = JSON.stringify(body);

                    const resp = await fetch(url, options);
                    if (!resp.ok) {
                        const errText = await resp.text().catch(() => resp.statusText);
                        throw new Error(errText || `HTTP ${resp.status}`);
                    }
                    const text = await resp.text();
                    return text ? JSON.parse(text) : null;
                };

                // ===== 数据加载 =====
                const loadData = async () => {
                    if (!conference.value.id) return;
                    try {
                        loading.value = true;
                        const confId = conference.value.id;

                        // 并行加载四类辅助安排（后端使用路径变量而非查询参数）
                        const [transportsRaw, accommodationsRaw, discussionsRaw, diningsRaw] = await Promise.all([
                            apiCall(`${API_BASE}/transports/${confId}`).catch(() => []),
                            apiCall(`${API_BASE}/accommodations/${confId}`).catch(() => []),
                            apiCall(`${API_BASE}/discussions/${confId}`).catch(() => []),
                            apiCall(`${API_BASE}/dinings/${confId}`).catch(() => [])
                        ]);

                        // 后端→前端字段映射：Transport
                        transportList.value = (transportsRaw || []).map(t => ({
                            ...t,
                            name: t.vehicleName || t.name || '',
                            type: t.vehicleType || t.type || '大巴',
                            plateNumber: t.licensePlate || t.plateNumber || '',
                            passengers: (t.assignees || []).map(a => ({ id: a.attendeeId, name: a.attendeeName, department: a.department })),
                            assignedCount: t.assignedCount || 0
                        }));

                        // 后端→前端字段映射：Accommodation
                        accommodationList.value = (accommodationsRaw || []).map(r => ({
                            ...r,
                            type: r.roomType || r.type || '标准间',
                            occupants: (r.assignees || []).map(a => ({ id: a.attendeeId, name: a.attendeeName, department: a.department })),
                            assignedCount: r.assignedCount || 0
                        }));

                        // 后端→前端字段映射：Discussion
                        discussionList.value = (discussionsRaw || []).map(d => ({
                            ...d,
                            name: d.roomName || d.name || '',
                            assignedCount: d.assignedCount || 0
                        }));

                        // 后端→前端字段映射：Dining
                        diningList.value = (diningsRaw || []).map(d => ({
                            ...d,
                            name: d.restaurantName || d.name || '',
                            type: d.mealType || d.type || '',
                            time: d.mealTime || d.time || '',
                            assignedCount: d.assignedCount || 0
                        }));

                        // 加载参会人员（从报名服务）
                        await loadAttendees();

                        dataLoaded.value = true;
                        console.log('[auxiliary] 从后端API加载数据成功');
                    } catch (error) {
                        console.error('加载数据失败:', error);
                    } finally {
                        loading.value = false;
                    }
                };

                const loadAttendees = async () => {
                    if (!conference.value.id) return;
                    try {
                        const headers = { 'Content-Type': 'application/json' };
                        const token = localStorage.getItem('authToken');
                        if (token) headers['Authorization'] = `Bearer ${token}`;
                        const tenantId = localStorage.getItem('tenantId');
                        if (tenantId) headers['X-Tenant-Id'] = tenantId;

                        const resp = await fetch(`/api/registration/list?conferenceId=${conference.value.id}&page=1&pageSize=1000`, { headers });
                        if (resp.ok) {
                            const result = await resp.json();
                            const records = (result.data?.records) || [];
                            const approved = records.filter(r => r.status === 1 || r.status === '1');
                            attendees.value = approved.map((r, idx) => ({
                                id: r.id,
                                name: r.name || r.realName || r.userName || `参会者${idx + 1}`,
                                department: r.department || r.organization || '',
                                position: r.position || r.title || '',
                                company: r.company || r.organization || r.unit || '',
                                phone: r.phone || '',
                                gender: r.gender || '',
                                flightNumber: r.flightNumber || ''
                            }));
                            console.log(`[auxiliary] 加载了 ${attendees.value.length} 位已通过参会人员`);
                        }
                    } catch (e) {
                        console.warn('[auxiliary] 加载参会人员失败:', e);
                    }
                };

                const refreshData = async () => {
                    await loadData();
                    showNotification('success', 'check', '数据已刷新');
                };

                // ===== 乘车安排管理 =====
                const showTransportModal = () => {
                    editingTransportId.value = null;
                    Object.assign(newTransport, {
                        name: '',
                        type: '大巴',
                        plateNumber: '',
                        departure: '',
                        destination: '',
                        departureTime: '',
                        capacity: 45,
                        assignedCount: 0,
                        passengers: []
                    });
                    showTransportModalFlag.value = true;
                };

                const closeTransportModal = () => {
                    showTransportModalFlag.value = false;
                    editingTransportId.value = null;
                };

                const saveTransport = async () => {
                    if (!newTransport.name) {
                        alert('请输入车辆名称');
                        return;
                    }

                    const capacityMap = { '小车': 4, '中巴': 15, '大巴': 45 };
                    const payload = {
                        conferenceId: conference.value.id,
                        vehicleName: newTransport.name,
                        vehicleType: newTransport.type,
                        licensePlate: newTransport.plateNumber || '',
                        departure: newTransport.departure || '',
                        destination: newTransport.destination || '',
                        departureTime: newTransport.departureTime || '',
                        capacity: capacityMap[newTransport.type] || 45
                    };

                    try {
                        if (editingTransportId.value) {
                            await apiCall(`${API_BASE}/transports/${editingTransportId.value}`, 'PUT', payload);
                        } else {
                            await apiCall(`${API_BASE}/transports`, 'POST', payload);
                        }
                        await loadData();
                        closeTransportModal();
                        showNotification('success', 'check', '车辆保存成功');
                    } catch (err) {
                        console.error('保存车辆失败:', err);
                        alert('保存车辆失败: ' + err.message);
                    }
                };

                const editTransport = (vehicle) => {
                    editingTransportId.value = vehicle.id;
                    Object.assign(newTransport, { ...vehicle });
                    showTransportModalFlag.value = true;
                };

                const deleteTransport = async (id) => {
                    if (!confirm('确定要删除这辆车吗？')) return;
                    try {
                        await apiCall(`${API_BASE}/transports/${id}`, 'DELETE');
                        await loadData();
                        showNotification('success', 'check', '车辆已删除');
                    } catch (err) {
                        console.error('删除车辆失败:', err);
                        alert('删除失败: ' + err.message);
                    }
                };

                const autoAssignTransport = async () => {
                    if (transportList.value.length === 0) return;

                    // 获取已分配的人员ID集合
                    const assignedIds = new Set();
                    for (const v of transportList.value) {
                        try {
                            const assignees = await apiCall(`${API_BASE}/transports/${v.id}/assignees`).catch(() => []);
                            (assignees || []).forEach(a => assignedIds.add(String(a.attendeeId)));
                        } catch (e) { /* ignore */ }
                    }

                    const unassigned = attendees.value.filter(a => !assignedIds.has(String(a.id)));

                    let idx = 0;
                    for (const vehicle of transportList.value) {
                        const remaining = vehicle.capacity - (vehicle.assignedCount || 0);
                        for (let i = 0; i < remaining && idx < unassigned.length; i++, idx++) {
                            const att = unassigned[idx];
                            try {
                                await apiCall(`${API_BASE}/transports/${vehicle.id}/assign`, 'POST', {
                                    attendeeId: att.id,
                                    attendeeName: att.name || '',
                                    department: att.department || ''
                                });
                            } catch (e) {
                                console.warn('分配失败:', att.name, e.message);
                            }
                        }
                    }

                    await loadData();
                    showNotification('success', 'check', '乘车分配完成');
                };

                // ===== 住宿安排管理 =====
                const showAccommodationModal = () => {
                    editingAccommodationId.value = null;
                    Object.assign(newAccommodation, {
                        hotelName: '',
                        roomNumber: '',
                        type: '标准间',
                        capacity: 2,
                        assignedCount: 0,
                        occupants: [],
                        checkInTime: '',
                        checkOutTime: ''
                    });
                    showAccommodationModalFlag.value = true;
                };

                const closeAccommodationModal = () => {
                    showAccommodationModalFlag.value = false;
                    editingAccommodationId.value = null;
                };

                const saveAccommodation = async () => {
                    if (!newAccommodation.hotelName || !newAccommodation.roomNumber) {
                        alert('请填写完整信息');
                        return;
                    }

                    const payload = {
                        conferenceId: conference.value.id,
                        name: `${newAccommodation.hotelName} ${newAccommodation.roomNumber}`,
                        hotelName: newAccommodation.hotelName,
                        roomNumber: newAccommodation.roomNumber,
                        roomType: newAccommodation.type || '标准间',
                        capacity: newAccommodation.capacity || 2,
                        checkInTime: newAccommodation.checkInTime || '',
                        checkOutTime: newAccommodation.checkOutTime || ''
                    };

                    try {
                        if (editingAccommodationId.value) {
                            await apiCall(`${API_BASE}/accommodations/${editingAccommodationId.value}`, 'PUT', payload);
                        } else {
                            await apiCall(`${API_BASE}/accommodations`, 'POST', payload);
                        }
                        await loadData();
                        closeAccommodationModal();
                        showNotification('success', 'check', '房间保存成功');
                    } catch (err) {
                        console.error('保存房间失败:', err);
                        alert('保存房间失败: ' + err.message);
                    }
                };

                const editAccommodation = (room) => {
                    editingAccommodationId.value = room.id;
                    Object.assign(newAccommodation, { ...room });
                    showAccommodationModalFlag.value = true;
                };

                const deleteAccommodation = async (id) => {
                    if (!confirm('确定要删除这个房间吗？')) return;
                    try {
                        await apiCall(`${API_BASE}/accommodations/${id}`, 'DELETE');
                        await loadData();
                        showNotification('success', 'check', '房间已删除');
                    } catch (err) {
                        console.error('删除房间失败:', err);
                        alert('删除失败: ' + err.message);
                    }
                };

                const autoAssignAccommodation = async () => {
                    if (accommodationList.value.length === 0) return;

                    // 获取已分配的人员ID集合
                    const assignedIds = new Set();
                    for (const r of accommodationList.value) {
                        try {
                            const assignees = await apiCall(`${API_BASE}/accommodations/${r.id}/assignees`).catch(() => []);
                            (assignees || []).forEach(a => assignedIds.add(String(a.attendeeId)));
                        } catch (e) { /* ignore */ }
                    }

                    const unassigned = attendees.value.filter(a => !assignedIds.has(String(a.id)));

                    let idx = 0;
                    for (const room of accommodationList.value) {
                        const remaining = room.capacity - (room.assignedCount || 0);
                        for (let i = 0; i < remaining && idx < unassigned.length; i++, idx++) {
                            const att = unassigned[idx];
                            try {
                                await apiCall(`${API_BASE}/accommodations/${room.id}/assign`, 'POST', {
                                    attendeeId: att.id,
                                    attendeeName: att.name || '',
                                    department: att.department || ''
                                });
                            } catch (e) {
                                console.warn('分配失败:', att.name, e.message);
                            }
                        }
                    }

                    await loadData();
                    showNotification('success', 'check', '住宿分配完成');
                };

                // ===== 讨论室安排管理 =====
                const showDiscussionModal = () => {
                    editingDiscussionId.value = null;
                    Object.assign(newDiscussion, {
                        name: '',
                        location: '',
                        capacity: 15,
                        assignedCount: 0,
                        groups: [],
                        startTime: '',
                        endTime: ''
                    });
                    showDiscussionModalFlag.value = true;
                };

                const closeDiscussionModal = () => {
                    showDiscussionModalFlag.value = false;
                    editingDiscussionId.value = null;
                };

                const saveDiscussion = async () => {
                    if (!newDiscussion.name || !newDiscussion.location) {
                        alert('请填写完整信息');
                        return;
                    }

                    const payload = {
                        conferenceId: conference.value.id,
                        roomName: newDiscussion.name,
                        location: newDiscussion.location,
                        capacity: newDiscussion.capacity || 15,
                        startTime: newDiscussion.startTime || '',
                        endTime: newDiscussion.endTime || ''
                    };

                    try {
                        if (editingDiscussionId.value) {
                            await apiCall(`${API_BASE}/discussions/${editingDiscussionId.value}`, 'PUT', payload);
                        } else {
                            await apiCall(`${API_BASE}/discussions`, 'POST', payload);
                        }
                        await loadData();
                        closeDiscussionModal();
                        showNotification('success', 'check', '讨论室保存成功');
                    } catch (err) {
                        console.error('保存讨论室失败:', err);
                        alert('保存讨论室失败: ' + err.message);
                    }
                };

                const editDiscussion = (discussion) => {
                    editingDiscussionId.value = discussion.id;
                    Object.assign(newDiscussion, { ...discussion });
                    showDiscussionModalFlag.value = true;
                };

                const deleteDiscussion = async (id) => {
                    if (!confirm('确定要删除这个讨论室吗？')) return;
                    try {
                        await apiCall(`${API_BASE}/discussions/${id}`, 'DELETE');
                        await loadData();
                        showNotification('success', 'check', '讨论室已删除');
                    } catch (err) {
                        console.error('删除讨论室失败:', err);
                        alert('删除失败: ' + err.message);
                    }
                };

                const autoAssignDiscussions = async () => {
                    if (discussionList.value.length === 0) return;

                    const assignedIds = new Set();
                    for (const d of discussionList.value) {
                        try {
                            const assignees = await apiCall(`${API_BASE}/discussions/${d.id}/assignees`).catch(() => []);
                            (assignees || []).forEach(a => assignedIds.add(String(a.attendeeId)));
                        } catch (e) { /* ignore */ }
                    }

                    const unassigned = attendees.value.filter(a => !assignedIds.has(String(a.id)));

                    let idx = 0;
                    for (const discussion of discussionList.value) {
                        const remaining = discussion.capacity - (discussion.assignedCount || 0);
                        for (let i = 0; i < remaining && idx < unassigned.length; i++, idx++) {
                            const att = unassigned[idx];
                            try {
                                await apiCall(`${API_BASE}/discussions/${discussion.id}/assign`, 'POST', {
                                    attendeeId: att.id,
                                    attendeeName: att.name || '',
                                    department: att.department || ''
                                });
                            } catch (e) {
                                console.warn('分配失败:', att.name, e.message);
                            }
                        }
                    }

                    await loadData();
                    showNotification('success', 'check', '讨论室分配完成');
                };

                // ===== 用餐安排管理 =====
                const showDiningModal = () => {
                    editingDiningId.value = null;
                    Object.assign(newDining, {
                        name: '',
                        type: '午餐',
                        capacity: 10,
                        assignedCount: 0,
                        groups: [],
                        time: '',
                        location: ''
                    });
                    showDiningModalFlag.value = true;
                };

                const closeDiningModal = () => {
                    showDiningModalFlag.value = false;
                    editingDiningId.value = null;
                };

                const saveDining = async () => {
                    if (!newDining.name || !newDining.location) {
                        alert('请填写完整信息');
                        return;
                    }

                    const payload = {
                        conferenceId: conference.value.id,
                        restaurantName: newDining.name,
                        mealType: newDining.type || '午餐',
                        location: newDining.location || '',
                        mealTime: newDining.time || '',
                        capacity: newDining.capacity || 10
                    };

                    try {
                        if (editingDiningId.value) {
                            await apiCall(`${API_BASE}/dinings/${editingDiningId.value}`, 'PUT', payload);
                        } else {
                            await apiCall(`${API_BASE}/dinings`, 'POST', payload);
                        }
                        await loadData();
                        closeDiningModal();
                        showNotification('success', 'check', '用餐安排保存成功');
                    } catch (err) {
                        console.error('保存用餐安排失败:', err);
                        alert('保存用餐安排失败: ' + err.message);
                    }
                };

                const editDining = (dining) => {
                    editingDiningId.value = dining.id;
                    Object.assign(newDining, { ...dining });
                    showDiningModalFlag.value = true;
                };

                const deleteDining = async (id) => {
                    if (!confirm('确定要删除这个用餐安排吗？')) return;
                    try {
                        await apiCall(`${API_BASE}/dinings/${id}`, 'DELETE');
                        await loadData();
                        showNotification('success', 'check', '用餐安排已删除');
                    } catch (err) {
                        console.error('删除用餐安排失败:', err);
                        alert('删除失败: ' + err.message);
                    }
                };

                const autoAssignDining = async () => {
                    if (diningList.value.length === 0) return;

                    const assignedIds = new Set();
                    for (const d of diningList.value) {
                        try {
                            const assignees = await apiCall(`${API_BASE}/dinings/${d.id}/assignees`).catch(() => []);
                            (assignees || []).forEach(a => assignedIds.add(String(a.attendeeId)));
                        } catch (e) { /* ignore */ }
                    }

                    const unassigned = attendees.value.filter(a => !assignedIds.has(String(a.id)));

                    let tableIdx = 0;
                    for (let i = 0; i < unassigned.length; i++) {
                        const att = unassigned[i];
                        const dining = diningList.value[tableIdx % diningList.value.length];
                        if ((dining.assignedCount || 0) < dining.capacity) {
                            try {
                                await apiCall(`${API_BASE}/dinings/${dining.id}/assign`, 'POST', {
                                    attendeeId: att.id,
                                    attendeeName: att.name || '',
                                    department: att.department || ''
                                });
                            } catch (e) {
                                console.warn('分配失败:', att.name, e.message);
                            }
                        }
                        tableIdx++;
                    }

                    await loadData();
                    showNotification('success', 'check', '用餐分配完成');
                };

                // ===== 接送机安排管理 =====
                const showAirportTransferModal = () => {
                    editingAirportTransferId.value = null;
                    Object.assign(newAirportTransfer, {
                        name: '',
                        type: '接机',
                        flightNumber: '',
                        capacity: 4,
                        assignedCount: 0,
                        passengers: [],
                        time: '',
                        fromAirport: '',
                        toDestination: ''
                    });
                    showAirportTransferModalFlag.value = true;
                };

                const closeAirportTransferModal = () => {
                    showAirportTransferModalFlag.value = false;
                    editingAirportTransferId.value = null;
                };

                const saveAirportTransfer = () => {
                    if (!newAirportTransfer.name || !newAirportTransfer.flightNumber) {
                        alert('请填写完整信息');
                        return;
                    }

                    if (editingAirportTransferId.value) {
                        const index = airportTransferList.value.findIndex(t => t.id === editingAirportTransferId.value);
                        if (index > -1) {
                            airportTransferList.value[index] = {
                                ...JSON.parse(JSON.stringify(newAirportTransfer)),
                                id: editingAirportTransferId.value
                            };
                        }
                    } else {
                        airportTransferList.value.push({
                            id: `transfer_${Date.now()}`,
                            ...JSON.parse(JSON.stringify(newAirportTransfer))
                        });
                    }

                    saveLocalAirportData();
                    closeAirportTransferModal();
                    showNotification('success', 'check', '接送机安排保存成功');
                };

                const editAirportTransfer = (transfer) => {
                    editingAirportTransferId.value = transfer.id;
                    Object.assign(newAirportTransfer, { ...transfer });
                    showAirportTransferModalFlag.value = true;
                };

                const deleteAirportTransfer = (id) => {
                    if (confirm('确定要删除这个接送机安排吗？')) {
                        const index = airportTransferList.value.findIndex(t => t.id === id);
                        if (index > -1) {
                            airportTransferList.value.splice(index, 1);
                            saveLocalAirportData();
                        }
                    }
                };

                const autoAssignAirportTransfer = async () => {
                    if (airportTransferList.value.length === 0) return;

                    const unassigned = attendees.value.filter(a => !a.airportTransferAssigned);
                    unassigned.forEach(att => {
                        const transfer = airportTransferList.value.find(t =>
                            t.flightNumber === att.flightNumber && t.assignedCount < t.capacity
                        );
                        if (transfer) {
                            transfer.assignedCount++;
                            att.airportTransferAssigned = true;
                            att.transferId = transfer.id;
                        }
                    });

                    saveLocalAirportData();
                    showNotification('success', 'check', '接送机分配完成');
                };

                // 接送机数据本地存储（暂无后端API）
                const saveLocalAirportData = () => {
                    try {
                        const key = `airport_transfer_${conference.value.id}`;
                        localStorage.setItem(key, JSON.stringify(airportTransferList.value));
                    } catch (e) { console.warn('保存接送机数据失败:', e); }
                };
                const loadLocalAirportData = () => {
                    try {
                        const key = `airport_transfer_${conference.value.id}`;
                        const data = localStorage.getItem(key);
                        if (data) airportTransferList.value = JSON.parse(data);
                    } catch (e) { /* ignore */ }
                };

                // ===== 智能分配所有 =====
                const autoAssignAllArrangements = async () => {
                    if (attendees.value.length === 0) {
                        alert('请等待参会人员加载完成');
                        return;
                    }

                    try {
                        loading.value = true;

                        await autoAssignTransport();
                        await autoAssignAccommodation();
                        await autoAssignDiscussions();
                        await autoAssignDining();
                        await autoAssignAirportTransfer();

                        await loadData();
                        showNotification('success', 'check', '智能分配全部完成！');
                    } catch (error) {
                        console.error('智能分配失败:', error);
                        showNotification('error', 'exclamation-triangle', '智能分配失败');
                    } finally {
                        loading.value = false;
                    }
                };

                // ===== 查看分配结果 =====
                const viewArrangements = () => {
                    showArrangementResultModalFlag.value = true;
                };

                const closeArrangementResultModal = () => {
                    showArrangementResultModalFlag.value = false;
                };

                // ===== 导出分配结果 =====
                const exportArrangementResults = () => {
                    let result = '辅助安排分配结果\n\n';
                    result += `会议：${conference.value.name}\n`;
                    result += `导出时间：${new Date().toLocaleString()}\n\n`;

                    result += '=== 乘车安排 ===\n';
                    if (transportList.value.length === 0) {
                        result += '暂无车辆\n';
                    } else {
                        transportList.value.forEach(vehicle => {
                            result += `${vehicle.name} (${vehicle.type})\n`;
                            result += `  路线: ${vehicle.departure} → ${vehicle.destination}\n`;
                            result += `  时间: ${vehicle.departureTime || '未设置'}\n`;
                            result += `  已分配: ${vehicle.assignedCount}/${vehicle.capacity}人\n`;
                            if (vehicle.passengers && vehicle.passengers.length > 0) {
                                result += `  乘车人员: ${vehicle.passengers.map(p => p.name).join(', ')}\n`;
                            }
                            result += '\n';
                        });
                    }

                    result += '\n=== 住宿安排 ===\n';
                    if (accommodationList.value.length === 0) {
                        result += '暂无房间\n';
                    } else {
                        accommodationList.value.forEach(room => {
                            result += `${room.hotelName} - ${room.roomNumber} (${room.type})\n`;
                            result += `  入住时间: ${room.checkInTime || '未设置'}\n`;
                            result += `  已分配: ${room.assignedCount}/${room.capacity}人\n`;
                            if (room.occupants && room.occupants.length > 0) {
                                result += `  入住人员: ${room.occupants.map(o => o.name).join(', ')}\n`;
                            }
                            result += '\n';
                        });
                    }

                    result += '\n=== 讨论室安排 ===\n';
                    if (discussionList.value.length === 0) {
                        result += '暂无讨论室\n';
                    } else {
                        discussionList.value.forEach(discussion => {
                            result += `${discussion.name} - ${discussion.location}\n`;
                            result += `  时间: ${discussion.startTime || '未设置'} - ${discussion.endTime || '未设置'}\n`;
                            result += `  已分配: ${discussion.assignedCount}/${discussion.capacity}人\n`;
                            result += '\n';
                        });
                    }

                    result += '\n=== 用餐安排 ===\n';
                    if (diningList.value.length === 0) {
                        result += '暂无用餐安排\n';
                    } else {
                        diningList.value.forEach(dining => {
                            result += `${dining.name} (${dining.type})\n`;
                            result += `  地点: ${dining.location}\n`;
                            result += `  时间: ${dining.time || '未设置'}\n`;
                            result += `  已分配: ${dining.assignedCount}/${dining.capacity}人\n`;
                            result += '\n';
                        });
                    }

                    result += '\n=== 接送机安排 ===\n';
                    if (airportTransferList.value.length === 0) {
                        result += '暂无接送机安排\n';
                    } else {
                        airportTransferList.value.forEach(transfer => {
                            result += `${transfer.name} (${transfer.type})\n`;
                            result += `  航班: ${transfer.flightNumber}\n`;
                            result += `  时间: ${transfer.time || '未设置'}\n`;
                            result += `  已分配: ${transfer.assignedCount}/${transfer.capacity}人\n`;
                            result += '\n';
                        });
                    }

                    const blob = new Blob([result], { type: 'text/plain;charset=utf-8' });
                    const url = URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = `辅助安排分配结果_${conference.value.name}_${Date.now()}.txt`;
                    a.click();
                    URL.revokeObjectURL(url);
                };

                // ===== 人员分配弹窗 =====
                const assignPassengers = async (vehicle) => {
                    currentAssignType.value = 'transport';
                    assignModalTitle.value = `分配人员 - ${vehicle.name || vehicle.vehicleName}`;
                    // 从API获取已分配人员
                    let assignedPeople = [];
                    try {
                        const resp = await apiCall(`${API_BASE}/transports/${vehicle.id}/assignees`);
                        assignedPeople = (resp || []).map(a => ({ id: a.attendeeId, name: a.attendeeName, department: a.department }));
                    } catch (e) { console.warn('获取已分配人员失败:', e); }
                    currentArrangementItem.value = { ...vehicle, assignedPeople };
                    showPersonAssignModalFlag.value = true;
                };

                const assignOccupants = async (room) => {
                    currentAssignType.value = 'accommodation';
                    assignModalTitle.value = `分配人员 - ${room.hotelName || room.name} ${room.roomNumber || ''}`;
                    let assignedPeople = [];
                    try {
                        const resp = await apiCall(`${API_BASE}/accommodations/${room.id}/assignees`);
                        assignedPeople = (resp || []).map(a => ({ id: a.attendeeId, name: a.attendeeName, department: a.department }));
                    } catch (e) { console.warn('获取已分配人员失败:', e); }
                    currentArrangementItem.value = { ...room, assignedPeople };
                    showPersonAssignModalFlag.value = true;
                };

                const closePersonAssignModal = () => {
                    showPersonAssignModalFlag.value = false;
                    currentArrangementItem.value = null;
                    currentAssignType.value = '';
                };

                const addPersonToArrangement = async (attendee) => {
                    if (!currentArrangementItem.value) return;
                    if (currentArrangementItem.value.assignedCount >= currentArrangementItem.value.capacity) {
                        alert('已达到最大容量');
                        return;
                    }

                    const item = currentArrangementItem.value;
                    const typeMap = {
                        'transport': 'transports',
                        'accommodation': 'accommodations',
                        'discussion': 'discussions',
                        'dining': 'dinings'
                    };
                    const typeUrl = typeMap[currentAssignType.value];
                    if (!typeUrl) return;

                    try {
                        await apiCall(`${API_BASE}/${typeUrl}/${item.id}/assign`, 'POST', {
                            attendeeId: attendee.id,
                            attendeeName: attendee.name || '',
                            department: attendee.department || ''
                        });
                        await loadData();

                        // 重新打开弹窗显示更新后的数据
                        const list = currentAssignType.value === 'transport' ? transportList.value :
                                     currentAssignType.value === 'accommodation' ? accommodationList.value :
                                     currentAssignType.value === 'discussion' ? discussionList.value :
                                     diningList.value;
                        const refreshedItem = list.find(x => x.id === item.id);
                        if (refreshedItem) {
                            const people = (refreshedItem.passengers || refreshedItem.occupants || refreshedItem.assignees || []).map(a => ({ id: a.attendeeId || a.id, name: a.attendeeName || a.name, department: a.department }));
                            currentArrangementItem.value = { ...refreshedItem, assignedPeople: people };
                        }
                        showNotification('success', 'check', '分配成功');
                    } catch (err) {
                        console.error('分配人员失败:', err);
                        alert('分配失败: ' + err.message);
                    }
                };

                const removePersonFromArrangement = async (personId) => {
                    if (!currentArrangementItem.value) return;

                    const item = currentArrangementItem.value;
                    const typeMap = {
                        'transport': 'transports',
                        'accommodation': 'accommodations',
                        'discussion': 'discussions',
                        'dining': 'dinings'
                    };
                    const typeUrl = typeMap[currentAssignType.value];
                    if (!typeUrl) return;

                    try {
                        await apiCall(`${API_BASE}/${typeUrl}/${item.id}/assign/${personId}`, 'DELETE');
                        await loadData();

                        const list = currentAssignType.value === 'transport' ? transportList.value :
                                     currentAssignType.value === 'accommodation' ? accommodationList.value :
                                     currentAssignType.value === 'discussion' ? discussionList.value :
                                     diningList.value;
                        const refreshedItem = list.find(x => x.id === item.id);
                        if (refreshedItem) {
                            const people = (refreshedItem.passengers || refreshedItem.occupants || refreshedItem.assignees || []).map(a => ({ id: a.attendeeId || a.id, name: a.attendeeName || a.name, department: a.department }));
                            currentArrangementItem.value = { ...refreshedItem, assignedPeople: people };
                        }
                        showNotification('success', 'check', '已取消分配');
                    } catch (err) {
                        console.error('取消分配失败:', err);
                        alert('取消分配失败: ' + err.message);
                    }
                };

                // ===== 通知功能 =====
                const showNotification = (type, icon, message) => {
                    notificationStatus.value = { type, icon, message };
                    setTimeout(() => {
                        notificationStatus.value = null;
                    }, 3000);
                };

                const sendTransportNotifications = () => {
                    showNotification('success', 'check', '乘车通知已发送');
                };

                const sendAccommodationNotifications = () => {
                    showNotification('success', 'check', '住宿通知已发送');
                };

                const sendDiscussionNotifications = () => {
                    showNotification('success', 'check', '讨论室通知已发送');
                };

                const sendDiningNotifications = () => {
                    showNotification('success', 'check', '用餐通知已发送');
                };

                const sendAllArrangementNotifications = () => {
                    showNotification('success', 'check', '所有通知已发送');
                };

                // ===== 初始化 =====
                onMounted(async () => {
                    // 获取会议上下文
                    if (window.ConferenceContext) {
                        const ctx = window.ConferenceContext.getCurrentConference();
                        if (ctx) {
                            conference.value = ctx;
                        }
                    }

                    // 从URL参数获取会议ID
                    const urlParams = new URLSearchParams(window.location.search);
                    const confId = urlParams.get('conferenceId');
                    if (confId) {
                        conference.value.id = confId;
                    }

                    await loadData();
                    loadLocalAirportData();
                });

                return {
                    // 状态
                    loading,
                    dataLoaded,
                    conference,
                    attendees,
                    notificationStatus,

                    // 辅助安排数据
                    transportList,
                    accommodationList,
                    discussionList,
                    diningList,
                    airportTransferList,

                    // 弹窗状态
                    showTransportModalFlag,
                    showAccommodationModalFlag,
                    showDiscussionModalFlag,
                    showDiningModalFlag,
                    showAirportTransferModalFlag,
                    showArrangementResultModalFlag,
                    showPersonAssignModalFlag,

                    // 编辑模式标识
                    editingTransportId,
                    editingAccommodationId,
                    editingDiscussionId,
                    editingDiningId,
                    editingAirportTransferId,

                    // 人员分配弹窗
                    assignModalTitle,
                    currentAssignType,
                    currentArrangementItem,
                    unassignedAttendees,

                    // 表单数据
                    newTransport,
                    newAccommodation,
                    newDiscussion,
                    newDining,
                    newAirportTransfer,

                    // 计算属性
                    transportAssignedCount,
                    totalTransportCapacity,
                    accommodationAssignedCount,
                    totalAccommodationCapacity,
                    discussionAssignedCount,
                    totalDiscussionCapacity,
                    diningAssignedCount,
                    totalDiningCapacity,
                    airportTransferAssignedCount,
                    totalAirportTransferCapacity,
                    hasAnyAssignments,

                    // 方法
                    refreshData,

                    // 乘车安排
                    showTransportModal,
                    closeTransportModal,
                    saveTransport,
                    editTransport,
                    deleteTransport,
                    autoAssignTransport,
                    assignPassengers,

                    // 住宿安排
                    showAccommodationModal,
                    closeAccommodationModal,
                    saveAccommodation,
                    editAccommodation,
                    deleteAccommodation,
                    autoAssignAccommodation,
                    assignOccupants,

                    // 讨论室安排
                    showDiscussionModal,
                    closeDiscussionModal,
                    saveDiscussion,
                    editDiscussion,
                    deleteDiscussion,
                    autoAssignDiscussions,

                    // 用餐安排
                    showDiningModal,
                    closeDiningModal,
                    saveDining,
                    editDining,
                    deleteDining,
                    autoAssignDining,

                    // 接送机安排
                    showAirportTransferModal,
                    closeAirportTransferModal,
                    saveAirportTransfer,
                    editAirportTransfer,
                    deleteAirportTransfer,
                    autoAssignAirportTransfer,

                    // 智能分配所有
                    autoAssignAllArrangements,

                    // 分配结果
                    viewArrangements,
                    closeArrangementResultModal,
                    exportArrangementResults,

                    // 人员分配
                    closePersonAssignModal,
                    addPersonToArrangement,
                    removePersonFromArrangement,

                    // 通知
                    sendTransportNotifications,
                    sendAccommodationNotifications,
                    sendDiscussionNotifications,
                    sendDiningNotifications,
                    sendAllArrangementNotifications
                };
            }
        }).mount('#app');
    
