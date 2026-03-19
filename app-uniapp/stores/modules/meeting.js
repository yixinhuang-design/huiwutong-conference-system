import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useMeetingStore = defineStore('meeting', () => {
  // State
  const currentMeeting = ref(null)
  const seats = ref([])
  const seatLayout = ref(null)
  const allocations = ref(new Map()) // seatId -> personId
  const groups = ref([])
  const meetingParticipants = ref([])
  const isLoading = ref(false)
  const seatSelectedCount = ref(0)
  const personSelectedCount = ref(0)

  // Getters
  const totalSeats = computed(() => seats.value.length)

  const availableSeats = computed(() =>
    seats.value.filter((s) => s.status === 'available' && !allocations.value.has(s.id))
  )

  const occupiedSeats = computed(() =>
    seats.value.filter((s) => allocations.value.has(s.id))
  )

  const unallocatedPersons = computed(() =>
    meetingParticipants.value.filter(
      (p) => !Array.from(allocations.value.values()).includes(p.id)
    )
  )

  const occupancyRate = computed(() => {
    if (totalSeats.value === 0) return 0
    return ((occupiedSeats.value.length / totalSeats.value) * 100).toFixed(1)
  })

  const seatStats = computed(() => ({
    total: totalSeats.value,
    available: availableSeats.value.length,
    occupied: occupiedSeats.value.length,
    unavailable: seats.value.filter((s) => s.status === 'unavailable').length,
    occupancyRate: occupancyRate.value,
  }))

  const personStats = computed(() => ({
    total: meetingParticipants.value.length,
    allocated: occupiedSeats.value.length,
    unallocated: unallocatedPersons.value.length,
  }))

  // Actions
  const fetchSeats = async (meetingId) => {
    isLoading.value = true
    try {
      // const response = await meetingAPI.getSeats(meetingId)
      // seats.value = response.data.seats
      // seatLayout.value = response.data.layout
      return seats.value
    } catch (error) {
      console.error('Failed to fetch seats:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const fetchMeetingParticipants = async (meetingId) => {
    isLoading.value = true
    try {
      // const response = await meetingAPI.getParticipants(meetingId)
      // meetingParticipants.value = response.data
      return meetingParticipants.value
    } catch (error) {
      console.error('Failed to fetch participants:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const setCurrentMeeting = (meeting) => {
    currentMeeting.value = meeting
  }

  const setSeatLayout = (layout) => {
    seatLayout.value = layout
  }

  const setSeats = (seats_list) => {
    seats.value = seats_list
  }

  const setSeatParticipants = (participants_list) => {
    meetingParticipants.value = participants_list
  }

  const toggleSeatSelection = (seatId) => {
    const seat = seats.value.find((s) => s.id === seatId)
    if (!seat) return

    if (seat.selected) {
      seat.selected = false
      seatSelectedCount.value--
    } else {
      seat.selected = true
      seatSelectedCount.value++
    }
  }

  const togglePersonSelection = (personId) => {
    const person = meetingParticipants.value.find((p) => p.id === personId)
    if (!person) return

    if (person.selected) {
      person.selected = false
      personSelectedCount.value--
    } else {
      person.selected = true
      personSelectedCount.value++
    }
  }

  const selectAllSeats = () => {
    availableSeats.value.forEach((seat) => {
      seat.selected = true
    })
    seatSelectedCount.value = availableSeats.value.length
  }

  const deselectAllSeats = () => {
    seats.value.forEach((seat) => {
      seat.selected = false
    })
    seatSelectedCount.value = 0
  }

  const selectAllPersons = () => {
    unallocatedPersons.value.forEach((person) => {
      person.selected = true
    })
    personSelectedCount.value = unallocatedPersons.value.length
  }

  const deselectAllPersons = () => {
    meetingParticipants.value.forEach((person) => {
      person.selected = false
    })
    personSelectedCount.value = 0
  }

  const allocateSeat = (seatId, personId) => {
    // Remove person from other seat if already allocated
    for (const [seat, person] of allocations.value.entries()) {
      if (person === personId) {
        allocations.value.delete(seat)
        break
      }
    }

    // Allocate new seat
    allocations.value.set(seatId, personId)

    // Update seat status
    const seat = seats.value.find((s) => s.id === seatId)
    if (seat) {
      seat.status = 'occupied'
      seat.personId = personId
    }
  }

  const deallocateSeat = (seatId) => {
    allocations.value.delete(seatId)
    const seat = seats.value.find((s) => s.id === seatId)
    if (seat) {
      seat.status = 'available'
      seat.personId = null
    }
  }

  const smartAllocate = () => {
    // 智能分配算法
    const unallocated = unallocatedPersons.value
    const available = availableSeats.value

    if (available.length === 0 || unallocated.length === 0) {
      return
    }

    // 按优先级排序
    const sorted = unallocated.sort((a, b) => {
      const priorityMap = { leader: 1, vip: 2, normal: 3 }
      return (priorityMap[a.level] || 3) - (priorityMap[b.level] || 3)
    })

    // 分配座位
    sorted.forEach((person, index) => {
      if (index < available.length) {
        allocateSeat(available[index].id, person.id)
      }
    })

    deselectAllSeats()
    deselectAllPersons()
  }

  const batchAllocate = () => {
    // 批量分配选中的座位和人员
    const selectedSeats = seats.value.filter((s) => s.selected)
    const selectedPersons = meetingParticipants.value.filter((p) => p.selected)

    const count = Math.min(selectedSeats.length, selectedPersons.length)

    for (let i = 0; i < count; i++) {
      allocateSeat(selectedSeats[i].id, selectedPersons[i].id)
    }

    deselectAllSeats()
    deselectAllPersons()
  }

  const exportAllocations = () => {
    const result = []
    for (const [seatId, personId] of allocations.value.entries()) {
      const seat = seats.value.find((s) => s.id === seatId)
      const person = meetingParticipants.value.find((p) => p.id === personId)

      if (seat && person) {
        result.push({
          seatId,
          seatNumber: seat.number,
          seatRow: seat.row,
          seatCol: seat.col,
          personId,
          personName: person.name,
          personPhone: person.phone,
          personUnit: person.unit,
        })
      }
    }
    return result
  }

  const createGroup = (groupData) => {
    const group = {
      id: Date.now(),
      ...groupData,
      members: [],
      createdAt: new Date().toISOString(),
    }
    groups.value.push(group)
    return group
  }

  const updateGroup = (groupId, updates) => {
    const index = groups.value.findIndex((g) => g.id === groupId)
    if (index >= 0) {
      groups.value[index] = {
        ...groups.value[index],
        ...updates,
      }
    }
  }

  const deleteGroup = (groupId) => {
    groups.value = groups.value.filter((g) => g.id !== groupId)
  }

  const addMemberToGroup = (groupId, memberId) => {
    const group = groups.value.find((g) => g.id === groupId)
    if (group && !group.members.includes(memberId)) {
      group.members.push(memberId)
    }
  }

  const removeMemberFromGroup = (groupId, memberId) => {
    const group = groups.value.find((g) => g.id === groupId)
    if (group) {
      group.members = group.members.filter((m) => m !== memberId)
    }
  }

  const clearAllocations = () => {
    allocations.value.clear()
    seats.value.forEach((seat) => {
      seat.status = 'available'
      seat.personId = null
      seat.selected = false
    })
    meetingParticipants.value.forEach((person) => {
      person.selected = false
    })
    seatSelectedCount.value = 0
    personSelectedCount.value = 0
  }

  return {
    // State
    currentMeeting,
    seats,
    seatLayout,
    allocations,
    groups,
    meetingParticipants,
    isLoading,
    seatSelectedCount,
    personSelectedCount,

    // Getters
    totalSeats,
    availableSeats,
    occupiedSeats,
    unallocatedPersons,
    occupancyRate,
    seatStats,
    personStats,

    // Actions
    fetchSeats,
    fetchMeetingParticipants,
    setCurrentMeeting,
    setSeatLayout,
    setSeats,
    setSeatParticipants,
    toggleSeatSelection,
    togglePersonSelection,
    selectAllSeats,
    deselectAllSeats,
    selectAllPersons,
    deselectAllPersons,
    allocateSeat,
    deallocateSeat,
    smartAllocate,
    batchAllocate,
    exportAllocations,
    createGroup,
    updateGroup,
    deleteGroup,
    addMemberToGroup,
    removeMemberFromGroup,
    clearAllocations,
  }
})
