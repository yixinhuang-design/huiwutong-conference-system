/**
 * 座位管理系统 - 座位交互模块
 * 功能: 处理座位点击、拖拽、交换、编辑等交互
 * 版本: 2.0
 * 优先级: 2
 */

export const SeatingInteractionsModule = {
    /**
     * 初始化模块
     * @param {Object} vm - Vue实例
     * @param {Object} dependencies - 依赖的数据和方法
     */
    init(vm, dependencies) {
        const {
            attendees,
            seatingAreas,
            selectedSeats,
            draggedSeat,
            editMode,
            seatDetailPanel,
            seatChangeHistory,
            saveData,
            updateStatistics,
            recordSeatChange,
            nextTick
        } = dependencies;

        // ===== 座位点击处理 =====
        const handleSeatClick = (seat) => {
            if (!seat) return;

            if (selectedSeats.value.length > 0) {
                const firstSelected = selectedSeats.value[0];

                if (firstSelected.id === seat.id) {
                    selectedSeats.value = [];
                    return;
                }

                if (selectedSeats.value.length === 1) {
                    if (seat.status === 'available' && firstSelected.occupant) {
                        moveAttendeeToSeat(firstSelected, seat, '点击移动');
                        selectedSeats.value = [];
                        window.InteractionUtils?.showToast?.(
                            `${firstSelected.occupant?.name || '参会者'} 已移动到新座位`,
                            'success'
                        );
                    } else if (seat.status === 'occupied' && firstSelected.occupant) {
                        const name1 = firstSelected.occupant?.name;
                        const name2 = seat.occupant?.name;

                        const previousState = {
                            seat1: {
                                id: firstSelected.id,
                                occupant: firstSelected.occupant ? {...firstSelected.occupant} : null
                            },
                            seat2: {
                                id: seat.id,
                                occupant: seat.occupant ? {...seat.occupant} : null
                            }
                        };

                        swapSeats(firstSelected, seat);

                        recordSeatChange({
                            type: 'swap',
                            attendeeName: `${name1} <-> ${name2}`,
                            oldPosition: `${firstSelected.row}排${firstSelected.number}号`,
                            newPosition: `${seat.row}排${seat.number}号`,
                            reason: '点击交换',
                            timestamp: new Date().toLocaleString(),
                            previousState: previousState
                        });

                        selectedSeats.value = [];
                        window.InteractionUtils?.showToast?.(`${name1} 和 ${name2} 已交换座位`, 'success');
                    } else {
                        selectedSeats.value = [seat];
                    }
                } else {
                    selectedSeats.value = [seat];
                }
            } else {
                selectedSeats.value = [seat];
                if (seat.occupant) {
                    console.log(`已选择：${seat.occupant?.name} ${seat.row}排${seat.number}号`);
                }
            }
        };

        // ===== 移动参会者 =====
        const moveAttendeeToSeat = (fromSeat, toSeat, reason = '手动调整') => {
            if (!fromSeat.occupant) return;

            const attendee = fromSeat.occupant;
            const oldPosition = `${fromSeat.row}排${fromSeat.number}号`;
            const newPosition = `${toSeat.row}排${toSeat.number}号`;

            const previousState = {
                fromSeat: {
                    id: fromSeat.id,
                    status: fromSeat.status,
                    occupant: fromSeat.occupant ? {...fromSeat.occupant} : null,
                    type: fromSeat.type
                },
                toSeat: {
                    id: toSeat.id,
                    status: toSeat.status,
                    occupant: toSeat.occupant ? {...toSeat.occupant} : null,
                    type: toSeat.type
                }
            };

            recordSeatChange({
                type: 'move',
                attendeeName: attendee.name,
                oldPosition: oldPosition,
                newPosition: newPosition,
                reason: reason,
                timestamp: new Date().toLocaleString(),
                previousState: previousState
            });

            fromSeat.status = 'available';
            fromSeat.occupant = null;
            if (fromSeat.type === 'vip') fromSeat.type = 'regular';

            toSeat.status = 'occupied';
            toSeat.occupant = attendee;
            if (attendee.isVip) toSeat.type = 'vip';

            saveData();
            updateStatistics();
        };

        // ===== 交换两个座位 =====
        const swapSeats = (seat1, seat2) => {
            if (!seat1.occupant || !seat2.occupant) return;

            const temp = seat1.occupant;
            seat1.occupant = seat2.occupant;
            seat2.occupant = temp;

            saveData();
        };

        // ===== 将参会者分配到座位 =====
        const assignAttendeeToSeat = (attendee, seat) => {
            seat.status = 'occupied';
            seat.occupant = attendee;
            if (attendee.isVip) seat.type = 'vip';

            saveData();
            updateStatistics();
        };

        // ===== 搜索定位功能 =====
        const handleSearchInput = (searchKeyword, searchResults, showSearchResults) => {
            const keyword = searchKeyword.value.trim().toLowerCase();
            if (!keyword) {
                searchResults.value = [];
                return;
            }

            const results = [];
            attendees.value.forEach(attendee => {
                const matchName = attendee.name.toLowerCase().includes(keyword);
                const matchDept = (attendee.department || '').toLowerCase().includes(keyword);
                const matchPhone = (attendee.phone || '').includes(keyword);

                if (matchName || matchDept || matchPhone) {
                    let seatInfo = null;
                    seatingAreas.value.forEach(area => {
                        area.rows.forEach(row => {
                            if (row.seats) {
                                row.seats.forEach(seat => {
                                    if (seat.occupant && seat.occupant.id === attendee.id) {
                                        seatInfo = `${area.name} ${row.number}排${seat.number}号`;
                                    }
                                });
                            }
                        });
                    });

                    results.push({
                        ...attendee,
                        seatInfo: seatInfo || '未分配座位'
                    });
                }
            });

            searchResults.value = results.slice(0, 10);
        };

        const locateFirstResult = (searchResults, selectedSeats, showSearchResults) => {
            if (searchResults.value.length > 0) {
                locateSeat(searchResults.value[0], selectedSeats, showSearchResults);
            }
        };

        const locateSeat = (attendeeResult, selectedSeats, showSearchResults, highlightedResultId, searchKeyword) => {
            showSearchResults.value = false;
            if (highlightedResultId) highlightedResultId.value = attendeeResult.id;

            seatingAreas.value.forEach(area => {
                area.rows.forEach(row => {
                    if (row.seats) {
                        row.seats.forEach(seat => {
                            if (seat.occupant && seat.occupant.id === attendeeResult.id) {
                                highlightSeat(seat);
                                selectedSeats.value = [seat];
                            }
                        });
                    }
                });
            });

            setTimeout(() => {
                if (searchKeyword) searchKeyword.value = '';
                if (highlightedResultId) highlightedResultId.value = null;
            }, 3000);
        };

        const highlightSeat = (seat) => {
            nextTick(() => {
                const seatElement = document.querySelector(`[data-seat-id="${seat.id}"]`);
                if (seatElement) {
                    seatElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    seatElement.classList.add('highlight-located');
                    setTimeout(() => {
                        seatElement.classList.remove('highlight-located');
                    }, 4500);
                }
            });
        };

        // ===== 编辑模式切换 =====
        const toggleEditMode = () => {
            if (editMode.value === 'click') {
                editMode.value = 'drag';
                document.querySelector('.seating-chart-container')?.classList.add('drag-mode-active');
                window.InteractionUtils?.showToast?.(
                    '🖐️ 拖拽模式已开启！拖动有人的座位到其他位置即可交换或移动',
                    'success'
                );
            } else {
                editMode.value = 'click';
                document.querySelector('.seating-chart-container')?.classList.remove('drag-mode-active');
                window.InteractionUtils?.showToast?.(
                    '👆 点选模式已开启！点击座位进行选择和操作',
                    'info'
                );
            }
        };

        // ===== 拖拽功能 =====
        const handleDragStart = (event, seat) => {
            if (editMode.value !== 'drag' || seat.status !== 'occupied') {
                event.preventDefault();
                return;
            }

            draggedSeat.value = seat;
            event.target.classList.add('dragging');
            event.dataTransfer.effectAllowed = 'move';
            event.dataTransfer.setData('text/plain', seat.id);
        };

        const handleDragOver = (event, seat) => {
            if (!draggedSeat.value || draggedSeat.value.id === seat.id) return;
            event.preventDefault();
            event.dataTransfer.dropEffect = 'move';
            event.target.classList.add('drag-over');
        };

        const handleDragLeave = (event) => {
            event.target.classList.remove('drag-over');
        };

        const handleDrop = (event, targetSeat) => {
            event.preventDefault();
            event.target.classList.remove('drag-over');

            const sourceSeat = draggedSeat.value;
            if (!sourceSeat || sourceSeat.id === targetSeat.id) {
                draggedSeat.value = null;
                return;
            }

            document.querySelectorAll('.seat.dragging').forEach(el => el.classList.remove('dragging'));

            if (targetSeat.status === 'available') {
                moveAttendeeToSeat(sourceSeat, targetSeat, '拖拽移动');
            } else if (targetSeat.status === 'occupied') {
                swapSeats(sourceSeat, targetSeat);
                recordSeatChange({
                    type: 'swap',
                    attendeeName: `${sourceSeat.occupant?.name} <-> ${targetSeat.occupant?.name}`,
                    oldPosition: '',
                    newPosition: '',
                    reason: '拖拽交换',
                    timestamp: new Date().toLocaleString()
                });
            }

            draggedSeat.value = null;

            window.InteractionUtils?.showToast?.('座位调整完成', 'success');
        };

        // ===== 座位详情面板 =====
        const openSeatDetailPanel = (seat) => {
            if (!seat) return;

            seatDetailPanel.show = true;
            seatDetailPanel.seat = seat;
            seatDetailPanel.selectedAttendeeId = seat.occupant?.id ? String(seat.occupant.id) : '';
            seatDetailPanel.isVip = seat.type === 'vip';
            seatDetailPanel.notes = seat.notes || '';

            console.log('[openSeatDetailPanel] 座位:', seat.number, '状态:', seat.status);

            if (seat.status === 'available' && window.InteractionUtils) {
                window.InteractionUtils.showToast('请从下拉列表选择入座人员', 'info');
            }
        };

        const closeSeatDetailPanel = () => {
            seatDetailPanel.show = false;
            seatDetailPanel.seat = null;
        };

        const getSeatAreaName = (seat) => {
            for (const area of seatingAreas.value) {
                for (const row of area.rows) {
                    if (row.seats) {
                        for (const s of row.seats) {
                            if (s.id === seat.id) {
                                return area.name;
                            }
                        }
                    }
                }
            }
            return '未知区域';
        };

        const getUnassignedAttendees = () => {
            const assignedIds = new Set();
            seatingAreas.value.forEach(area => {
                area.rows.forEach(row => {
                    if (row.seats) {
                        row.seats.forEach(seat => {
                            if (seat.occupant) {
                                assignedIds.add(seat.occupant.id);
                            }
                        });
                    }
                });
            });

            return attendees.value.filter(att => !assignedIds.has(att.id));
        };

        const saveSeatDetails = () => {
            const seat = seatDetailPanel.seat;
            if (!seat) {
                console.warn('[saveSeatDetails] 没有选中的座位');
                return;
            }

            const oldOccupant = seat.occupant;
            const newAttendeeId = seatDetailPanel.selectedAttendeeId;

            console.log('[saveSeatDetails] 新选择的人员ID:', newAttendeeId, '原人员ID:', oldOccupant?.id);

            seat.type = seatDetailPanel.isVip ? 'vip' : 'regular';
            seat.notes = seatDetailPanel.notes;

            if (newAttendeeId === '' && seat.occupant) {
                console.log('[saveSeatDetails] 清空座位');
                recordSeatChange({
                    type: 'clear',
                    attendeeName: seat.occupant.name,
                    oldPosition: `${seat.row}排${seat.number}号`,
                    newPosition: '（已清空）',
                    reason: '面板操作清空',
                    timestamp: new Date().toLocaleString()
                });
                seat.status = 'available';
                seat.occupant = null;
            } else if (newAttendeeId && String(newAttendeeId) !== String(oldOccupant?.id)) {
                const newAttendee = attendees.value.find(a => String(a.id) === String(newAttendeeId));
                console.log('[saveSeatDetails] 找到的人员:', newAttendee);

                if (newAttendee) {
                    seatingAreas.value.forEach(area => {
                        area.rows.forEach(row => {
                            if (row.seats) {
                                row.seats.forEach(s => {
                                    if (s.occupant && String(s.occupant.id) === String(newAttendeeId) && s.id !== seat.id) {
                                        console.log('[saveSeatDetails] 从原座位移除:', s.number);
                                        s.status = 'available';
                                        s.occupant = null;
                                        if (s.type === 'vip') s.type = 'regular';
                                    }
                                });
                            }
                        });
                    });

                    if (oldOccupant) {
                        recordSeatChange({
                            type: 'replace',
                            attendeeName: `${oldOccupant.name} -> ${newAttendee.name}`,
                            oldPosition: `${seat.row}排${seat.number}号`,
                            newPosition: `${seat.row}排${seat.number}号`,
                            reason: '面板操作替换',
                            timestamp: new Date().toLocaleString()
                        });
                    } else {
                        recordSeatChange({
                            type: 'assign',
                            attendeeName: newAttendee.name,
                            oldPosition: '未分配',
                            newPosition: `${seat.row}排${seat.number}号`,
                            reason: '面板操作分配',
                            timestamp: new Date().toLocaleString()
                        });
                    }
                    seat.status = 'occupied';
                    seat.occupant = newAttendee;
                    if (newAttendee.isVip) seat.type = 'vip';
                    console.log('[saveSeatDetails] 分配成功:', newAttendee.name);
                } else {
                    console.warn('[saveSeatDetails] 未找到人员，ID:', newAttendeeId);
                }
            } else {
                console.log('[saveSeatDetails] 人员未变更，只更新VIP状态和备注');
            }

            saveData();
            updateStatistics();
            closeSeatDetailPanel();

            window.InteractionUtils?.showToast?.('座位信息已保存', 'success');
        };

        // ===== 批量交换座位 =====
        const batchSwapSeats = () => {
            if (selectedSeats.value.length !== 2) {
                window.InteractionUtils?.showToast?.('请选择两个座位进行交换', 'warning');
                return;
            }

            const [seat1, seat2] = selectedSeats.value;

            if (seat1.status !== 'occupied' || seat2.status !== 'occupied') {
                window.InteractionUtils?.showToast?.('两个座位都需要有人才能交换', 'warning');
                return;
            }

            swapSeats(seat1, seat2);
            recordSeatChange({
                type: 'swap',
                attendeeName: `${seat1.occupant?.name} <-> ${seat2.occupant?.name}`,
                oldPosition: `${seat1.row}排${seat1.number}号`,
                newPosition: `${seat2.row}排${seat2.number}号`,
                reason: '批量交换',
                timestamp: new Date().toLocaleString()
            });

            selectedSeats.value = [];

            window.InteractionUtils?.showToast?.('座位交换成功', 'success');
        };

        // ===== 批量清空座位 =====
        const batchClearSeats = () => {
            if (selectedSeats.value.length === 0) {
                console.warn('请先选择座位');
                return;
            }

            if (!confirm(`确定要清空 ${selectedSeats.value.length} 个座位吗？`)) {
                return;
            }

            selectedSeats.value.forEach(seat => {
                if (seat && seat.occupant) {
                    recordSeatChange({
                        type: 'clear',
                        attendeeName: seat.occupant.name,
                        oldPosition: `${seat.row}排${seat.number}号`,
                        newPosition: '（已清空）',
                        reason: '批量清空',
                        timestamp: new Date().toLocaleString()
                    });

                    seat.status = 'available';
                    seat.occupant = null;
                    if (seat.type === 'vip') seat.type = 'regular';
                }
            });

            saveData();
            updateStatistics();
            selectedSeats.value = [];
        };

        // ===== 清除选择 =====
        const clearSelection = () => {
            selectedSeats.value = [];
        };

        // ===== 导出公开的方法 =====
        return {
            handleSeatClick,
            moveAttendeeToSeat,
            swapSeats,
            assignAttendeeToSeat,
            handleSearchInput,
            locateFirstResult,
            locateSeat,
            highlightSeat,
            toggleEditMode,
            handleDragStart,
            handleDragOver,
            handleDragLeave,
            handleDrop,
            openSeatDetailPanel,
            closeSeatDetailPanel,
            getSeatAreaName,
            getUnassignedAttendees,
            saveSeatDetails,
            batchSwapSeats,
            batchClearSeats,
            clearSelection
        };
    }
};

export default SeatingInteractionsModule;
