/**
 * 座位管理系统 - 辅助安排模块
 * 功能: 处理交通、住宿、讨论室、用餐、接送机等辅助安排
 * 版本: 2.0
 * 优先级: 2
 */

export const SeatingArrangementsModule = {
    /**
     * 初始化模块
     * @param {Object} vm - Vue实例
     * @param {Object} dependencies - 依赖的数据和方法
     */
    init(vm, dependencies) {
        const {
            attendees,
            transportList,
            accommodationList,
            discussionList,
            diningList,
            airportTransferList,
            loading,
            saveData,
            updateStatistics
        } = dependencies;

        // ===== 1. 乘车安排管理 =====

        const newTransport = {
            id: '',
            name: '',
            type: '大巴',
            plateNumber: '',
            departure: '',
            destination: '',
            departureTime: '',
            capacity: 45,
            assignedCount: 0,
            passengers: []
        };

        const showTransportModal = () => {
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
        };

        const closeTransportModal = () => {
            newTransport.id = '';
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
        };

        const saveTransport = () => {
            if (!newTransport.name) {
                console.warn('请输入车辆名称');
                return;
            }

            const capacityMap = {
                '小车': 4,
                '中巴': 15,
                '大巴': 45
            };
            newTransport.capacity = capacityMap[newTransport.type] || 45;

            if (newTransport.id) {
                const index = transportList.value.findIndex(v => v.id === newTransport.id);
                if (index > -1) {
                    const existingPassengers = transportList.value[index].passengers || [];
                    transportList.value[index] = {
                        ...JSON.parse(JSON.stringify(newTransport)),
                        passengers: existingPassengers
                    };
                }
            } else {
                transportList.value.push({
                    id: `transport_${Date.now()}`,
                    ...JSON.parse(JSON.stringify(newTransport))
                });
            }

            saveData();
            closeTransportModal();
            console.log('车辆保存成功');
        };

        const editTransport = (vehicle) => {
            Object.assign(newTransport, {
                id: vehicle.id,
                name: vehicle.name,
                type: vehicle.type,
                plateNumber: vehicle.plateNumber,
                departure: vehicle.departure,
                destination: vehicle.destination,
                departureTime: vehicle.departureTime,
                capacity: vehicle.capacity,
                assignedCount: vehicle.assignedCount,
                passengers: vehicle.passengers || []
            });
        };

        const deleteTransport = (id) => {
            if (confirm('确定要删除这辆车吗？')) {
                const index = transportList.value.findIndex(v => v.id === id);
                if (index > -1) {
                    transportList.value[index].passengers?.forEach(p => {
                        p.transportAssigned = false;
                        p.vehicleId = null;
                    });
                    transportList.value.splice(index, 1);
                    saveData();
                }
            }
        };

        const removePassenger = (vehicleId, passengerId) => {
            const vehicle = transportList.value.find(v => v.id === vehicleId);
            if (vehicle) {
                vehicle.passengers = vehicle.passengers.filter(p => p.id !== passengerId);
                vehicle.assignedCount = vehicle.passengers.length;

                const attendee = attendees.value.find(a => a.id === passengerId);
                if (attendee) {
                    attendee.transportAssigned = false;
                    attendee.vehicleId = null;
                }
                saveData();
            }
        };

        // ===== 2. 住宿安排管理 =====

        const newAccommodation = {
            id: '',
            hotelName: '',
            roomNumber: '',
            type: '标准间',
            capacity: 2,
            assignedCount: 0,
            occupants: [],
            checkInTime: '',
            checkOutTime: ''
        };

        const showAccommodationModal = () => {
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
        };

        const closeAccommodationModal = () => {
            newAccommodation.id = '';
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
        };

        const saveAccommodation = () => {
            if (!newAccommodation.hotelName || !newAccommodation.roomNumber) {
                console.warn('请填写完整信息');
                return;
            }

            if (newAccommodation.id) {
                const index = accommodationList.value.findIndex(r => r.id === newAccommodation.id);
                if (index > -1) {
                    const existingOccupants = accommodationList.value[index].occupants || [];
                    accommodationList.value[index] = {
                        ...JSON.parse(JSON.stringify(newAccommodation)),
                        occupants: existingOccupants
                    };
                }
            } else {
                accommodationList.value.push({
                    id: `accommodation_${Date.now()}`,
                    ...JSON.parse(JSON.stringify(newAccommodation))
                });
            }

            saveData();
            closeAccommodationModal();
            console.log('房间保存成功');
        };

        const editAccommodation = (room) => {
            Object.assign(newAccommodation, {
                id: room.id,
                hotelName: room.hotelName,
                roomNumber: room.roomNumber,
                type: room.type,
                capacity: room.capacity,
                assignedCount: room.assignedCount,
                occupants: room.occupants || [],
                checkInTime: room.checkInTime,
                checkOutTime: room.checkOutTime
            });
        };

        const deleteAccommodation = (id) => {
            if (confirm('确定要删除这个房间吗？')) {
                const index = accommodationList.value.findIndex(r => r.id === id);
                if (index > -1) {
                    accommodationList.value[index].occupants?.forEach(o => {
                        o.accommodationAssigned = false;
                        o.accommodationId = null;
                    });
                    accommodationList.value.splice(index, 1);
                    saveData();
                }
            }
        };

        const removeOccupant = (roomId, occupantId) => {
            const room = accommodationList.value.find(r => r.id === roomId);
            if (room) {
                room.occupants = room.occupants.filter(o => o.id !== occupantId);
                room.assignedCount = room.occupants.length;

                const attendee = attendees.value.find(a => a.id === occupantId);
                if (attendee) {
                    attendee.accommodationAssigned = false;
                    attendee.accommodationId = null;
                }
                saveData();
            }
        };

        // ===== 3. 讨论室安排管理 =====

        const newDiscussion = {
            id: '',
            name: '',
            location: '',
            capacity: 15,
            assignedCount: 0,
            groups: [],
            startTime: '',
            endTime: ''
        };

        const showDiscussionModal = () => {
            Object.assign(newDiscussion, {
                name: '',
                location: '',
                capacity: 15,
                assignedCount: 0,
                groups: [],
                startTime: '',
                endTime: ''
            });
        };

        const closeDiscussionModal = () => {
            newDiscussion.id = '';
            Object.assign(newDiscussion, {
                name: '',
                location: '',
                capacity: 15,
                assignedCount: 0,
                groups: [],
                startTime: '',
                endTime: ''
            });
        };

        const saveDiscussion = () => {
            if (!newDiscussion.name || !newDiscussion.location) {
                console.warn('请填写完整信息');
                return;
            }

            if (newDiscussion.id) {
                const index = discussionList.value.findIndex(d => d.id === newDiscussion.id);
                if (index > -1) {
                    const existingGroups = discussionList.value[index].groups || [];
                    discussionList.value[index] = {
                        ...JSON.parse(JSON.stringify(newDiscussion)),
                        groups: existingGroups
                    };
                }
            } else {
                discussionList.value.push({
                    id: `discussion_${Date.now()}`,
                    ...JSON.parse(JSON.stringify(newDiscussion))
                });
            }

            saveData();
            closeDiscussionModal();
            console.log('讨论室保存成功');
        };

        const editDiscussion = (discussion) => {
            Object.assign(newDiscussion, {
                id: discussion.id,
                name: discussion.name,
                location: discussion.location,
                capacity: discussion.capacity,
                assignedCount: discussion.assignedCount,
                groups: discussion.groups || [],
                startTime: discussion.startTime,
                endTime: discussion.endTime
            });
        };

        const deleteDiscussion = (id) => {
            if (confirm('确定要删除这个讨论室吗？')) {
                const index = discussionList.value.findIndex(d => d.id === id);
                if (index > -1) {
                    discussionList.value[index].groups?.forEach(group => {
                        group.attendees?.forEach(a => {
                            a.discussionAssigned = false;
                            a.discussionId = null;
                        });
                    });
                    discussionList.value.splice(index, 1);
                    saveData();
                }
            }
        };

        // ===== 4. 用餐安排管理 =====

        const newDining = {
            id: '',
            name: '',
            type: '午餐',
            capacity: 10,
            assignedCount: 0,
            groups: [],
            time: '',
            location: ''
        };

        const showDiningModal = () => {
            Object.assign(newDining, {
                name: '',
                type: '午餐',
                capacity: 10,
                assignedCount: 0,
                groups: [],
                time: '',
                location: ''
            });
        };

        const closeDiningModal = () => {
            newDining.id = '';
        };

        const saveDining = () => {
            if (!newDining.name || !newDining.location) {
                console.warn('请填写完整信息');
                return;
            }

            diningList.value.push({
                id: `dining_${Date.now()}`,
                ...JSON.parse(JSON.stringify(newDining))
            });

            saveData();
            closeDiningModal();
            console.log('用餐安排添加成功');
        };

        const autoAssignDining = async () => {
            if (diningList.value.length === 0) return;

            const unassigned = attendees.value.filter(a => !a.diningAssigned);
            let tableIndex = 0;
            unassigned.forEach(att => {
                if (tableIndex < diningList.value.length) {
                    diningList.value[tableIndex].assignedCount++;
                    att.diningAssigned = true;
                    att.diningTableId = diningList.value[tableIndex].id;
                    tableIndex = (tableIndex + 1) % diningList.value.length;
                }
            });
        };

        // ===== 5. 接送机安排管理 =====

        const newAirportTransfer = {
            id: '',
            name: '',
            type: '接机',
            flightNumber: '',
            capacity: 4,
            assignedCount: 0,
            passengers: [],
            time: '',
            fromAirport: '',
            toDestination: ''
        };

        const showAirportTransferModal = () => {
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
        };

        const closeAirportTransferModal = () => {
            newAirportTransfer.id = '';
        };

        const saveAirportTransfer = () => {
            if (!newAirportTransfer.name || !newAirportTransfer.flightNumber) {
                console.warn('请填写完整信息');
                return;
            }

            airportTransferList.value.push({
                id: `transfer_${Date.now()}`,
                ...JSON.parse(JSON.stringify(newAirportTransfer))
            });

            saveData();
            closeAirportTransferModal();
            console.log('接送机安排添加成功');
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
        };

        // ===== 6. 智能分配所有辅助安排 =====

        const autoAssignAllArrangements = async () => {
            if (attendees.value.length === 0) {
                console.warn('请先导入参会人员');
                return;
            }

            try {
                loading.value = true;
                console.log('正在智能分配...');

                await autoAssignTransport();
                await autoAssignAccommodation();
                await autoAssignDiscussions();
                await autoAssignDining();
                await autoAssignAirportTransfer();

                saveData();
                console.log('智能分配完成！');
            } catch (error) {
                console.error('智能分配失败:', error);
            } finally {
                loading.value = false;
            }
        };

        const autoAssignTransport = async () => {
            if (transportList.value.length === 0) return;

            const unassigned = attendees.value.filter(a => !a.transportAssigned);
            const deptGroups = {};
            unassigned.forEach(att => {
                const dept = att.department || '未知单位';
                if (!deptGroups[dept]) deptGroups[dept] = [];
                deptGroups[dept].push(att);
            });

            for (const vehicle of transportList.value) {
                const remainingCapacity = vehicle.capacity - vehicle.assignedCount;
                if (remainingCapacity <= 0) continue;

                let assignedCount = 0;
                for (const dept in deptGroups) {
                    if (deptGroups[dept].length === 0) continue;

                    const canAssign = Math.min(remainingCapacity - assignedCount, deptGroups[dept].length);
                    const assigned = deptGroups[dept].splice(0, canAssign);

                    assigned.forEach(attendee => {
                        attendee.transportAssigned = true;
                        attendee.vehicleId = vehicle.id;
                        vehicle.passengers.push(attendee);
                    });

                    assignedCount += canAssign;
                    vehicle.assignedCount += canAssign;

                    if (assignedCount >= remainingCapacity) break;
                }
            }
        };

        const autoAssignAccommodation = async () => {
            if (accommodationList.value.length === 0) return;

            const unassigned = attendees.value.filter(a => !a.roomAssigned);
            const maleAttendees = unassigned.filter(a => a.gender === '男');
            const femaleAttendees = unassigned.filter(a => a.gender === '女');
            const unknownAttendees = unassigned.filter(a => !a.gender || a.gender === '未知');

            for (const room of accommodationList.value) {
                const remainingCapacity = room.capacity - room.assignedCount;
                if (remainingCapacity <= 0) continue;

                let attendeesToAssign = [];

                if (room.type === '单人间') {
                    if (unknownAttendees.length > 0) {
                        attendeesToAssign = [unknownAttendees.shift()];
                    } else if (maleAttendees.length > 0) {
                        attendeesToAssign = [maleAttendees.shift()];
                    } else if (femaleAttendees.length > 0) {
                        attendeesToAssign = [femaleAttendees.shift()];
                    }
                } else {
                    if (room.capacity === 2) {
                        if (maleAttendees.length >= 2) {
                            attendeesToAssign = maleAttendees.splice(0, 2);
                        } else if (femaleAttendees.length >= 2) {
                            attendeesToAssign = femaleAttendees.splice(0, 2);
                        } else {
                            attendeesToAssign = unknownAttendees.splice(0, 2);
                        }
                    } else {
                        attendeesToAssign = unknownAttendees.splice(0, remainingCapacity);
                    }
                }

                attendeesToAssign.forEach(attendee => {
                    if (attendee) {
                        attendee.roomAssigned = true;
                        attendee.roomId = room.id;
                        room.occupants.push(attendee);
                        room.assignedCount++;
                    }
                });
            }
        };

        const autoAssignDiscussions = async () => {
            if (discussionList.value.length === 0) return;

            const unassigned = attendees.value.filter(a => !a.discussionAssigned);

            const deptGroups = {};
            unassigned.forEach(att => {
                const dept = att.department || '未知单位';
                if (!deptGroups[dept]) deptGroups[dept] = [];
                deptGroups[dept].push(att);
            });

            for (const discussion of discussionList.value) {
                const remainingCapacity = discussion.capacity - discussion.assignedCount;
                if (remainingCapacity <= 0) continue;

                for (const dept in deptGroups) {
                    if (deptGroups[dept].length <= remainingCapacity && deptGroups[dept].length > 0) {
                        const group = deptGroups[dept];

                        group.forEach(attendee => {
                            attendee.discussionAssigned = true;
                            attendee.discussionId = discussion.id;
                            discussion.groups.push({
                                department: dept,
                                attendees: group
                            });
                            discussion.assignedCount++;
                        });

                        delete deptGroups[dept];
                        break;
                    }
                }
            }

            const remainingAttendees = Object.values(deptGroups).flat();
            for (const attendee of remainingAttendees) {
                for (const discussion of discussionList.value) {
                    if (discussion.assignedCount < discussion.capacity) {
                        attendee.discussionAssigned = true;
                        attendee.discussionId = discussion.id;
                        discussion.assignedCount++;
                        break;
                    }
                }
            }
        };

        // ===== 导出公开的方法 =====
        return {
            // 乘车安排
            newTransport,
            showTransportModal,
            closeTransportModal,
            saveTransport,
            editTransport,
            deleteTransport,
            removePassenger,

            // 住宿安排
            newAccommodation,
            showAccommodationModal,
            closeAccommodationModal,
            saveAccommodation,
            editAccommodation,
            deleteAccommodation,
            removeOccupant,

            // 讨论室安排
            newDiscussion,
            showDiscussionModal,
            closeDiscussionModal,
            saveDiscussion,
            editDiscussion,
            deleteDiscussion,

            // 用餐安排
            newDining,
            showDiningModal,
            closeDiningModal,
            saveDining,
            autoAssignDining,

            // 接送机安排
            newAirportTransfer,
            showAirportTransferModal,
            closeAirportTransferModal,
            saveAirportTransfer,
            autoAssignAirportTransfer,

            // 智能分配
            autoAssignAllArrangements,
            autoAssignTransport,
            autoAssignAccommodation,
            autoAssignDiscussions
        };
    }
};

export default SeatingArrangementsModule;
