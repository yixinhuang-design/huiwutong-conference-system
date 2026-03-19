import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useTrainingStore = defineStore('training', () => {
  // State
  const currentTraining = ref(null)
  const trainingList = ref([])
  const schedule = ref([])
  const participants = ref([])
  const trainingSettings = ref({})
  const isLoading = ref(false)
  const searchQuery = ref('')
  const filterStatus = ref('all') // all, pending, in-progress, completed
  const sortBy = ref('created_at') // created_at, name, status

  // Getters
  const filteredTrainingList = computed(() => {
    let filtered = trainingList.value

    // Filter by search query
    if (searchQuery.value) {
      const query = searchQuery.value.toLowerCase()
      filtered = filtered.filter(
        (t) =>
          t.name?.toLowerCase().includes(query) ||
          t.description?.toLowerCase().includes(query)
      )
    }

    // Filter by status
    if (filterStatus.value !== 'all') {
      filtered = filtered.filter((t) => t.status === filterStatus.value)
    }

    // Sort
    filtered.sort((a, b) => {
      switch (sortBy.value) {
        case 'name':
          return a.name?.localeCompare(b.name) || 0
        case 'status':
          return a.status?.localeCompare(b.status) || 0
        default:
          return (
            new Date(b.created_at) - new Date(a.created_at)
          )
      }
    })

    return filtered
  })

  const trainingStats = computed(() => ({
    total: trainingList.value.length,
    pending: trainingList.value.filter((t) => t.status === 'pending').length,
    inProgress: trainingList.value.filter((t) => t.status === 'in-progress')
      .length,
    completed: trainingList.value.filter((t) => t.status === 'completed')
      .length,
  }))

  const currentTrainingSchedule = computed(() => {
    if (!currentTraining.value?.id) return []
    return schedule.value.filter((s) => s.training_id === currentTraining.value.id)
  })

  // Actions
  const fetchTrainingList = async (filters = {}) => {
    isLoading.value = true
    try {
      // 这里将调用API获取训练列表
      // const response = await trainingAPI.getList(filters)
      // trainingList.value = response.data
      return trainingList.value
    } catch (error) {
      console.error('Failed to fetch training list:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const setCurrentTraining = (training) => {
    currentTraining.value = training
  }

  const createTraining = async (trainingData) => {
    isLoading.value = true
    try {
      // const response = await trainingAPI.create(trainingData)
      // const newTraining = response.data
      // trainingList.value.unshift(newTraining)
      // return newTraining
      return trainingData
    } catch (error) {
      console.error('Failed to create training:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const updateTraining = async (id, trainingData) => {
    isLoading.value = true
    try {
      // const response = await trainingAPI.update(id, trainingData)
      // const updatedTraining = response.data
      // const index = trainingList.value.findIndex(t => t.id === id)
      // if (index >= 0) {
      //   trainingList.value[index] = updatedTraining
      // }
      // if (currentTraining.value?.id === id) {
      //   currentTraining.value = updatedTraining
      // }
      // return updatedTraining
      return trainingData
    } catch (error) {
      console.error('Failed to update training:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const deleteTraining = async (id) => {
    isLoading.value = true
    try {
      // await trainingAPI.delete(id)
      trainingList.value = trainingList.value.filter((t) => t.id !== id)
      if (currentTraining.value?.id === id) {
        currentTraining.value = null
      }
      return true
    } catch (error) {
      console.error('Failed to delete training:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const duplicateTraining = async (id) => {
    isLoading.value = true
    try {
      // const response = await trainingAPI.duplicate(id)
      // const duplicatedTraining = response.data
      // trainingList.value.unshift(duplicatedTraining)
      // return duplicatedTraining
      const training = trainingList.value.find((t) => t.id === id)
      return {
        ...training,
        id: null,
        name: `${training?.name} (副本)`,
        status: 'draft',
      }
    } catch (error) {
      console.error('Failed to duplicate training:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const addScheduleEvent = (event) => {
    if (!schedule.value.find((s) => s.id === event.id)) {
      schedule.value.push(event)
    }
  }

  const removeScheduleEvent = (eventId) => {
    schedule.value = schedule.value.filter((s) => s.id !== eventId)
  }

  const updateScheduleEvent = (eventId, updates) => {
    const index = schedule.value.findIndex((s) => s.id === eventId)
    if (index >= 0) {
      schedule.value[index] = {
        ...schedule.value[index],
        ...updates,
      }
    }
  }

  const setParticipants = (participants_list) => {
    participants.value = participants_list
  }

  const addParticipant = (participant) => {
    if (!participants.value.find((p) => p.id === participant.id)) {
      participants.value.push(participant)
    }
  }

  const removeParticipant = (participantId) => {
    participants.value = participants.value.filter(
      (p) => p.id !== participantId
    )
  }

  const setSearchQuery = (query) => {
    searchQuery.value = query
  }

  const setFilterStatus = (status) => {
    filterStatus.value = status
  }

  const setSortBy = (field) => {
    sortBy.value = field
  }

  const clearFilters = () => {
    searchQuery.value = ''
    filterStatus.value = 'all'
    sortBy.value = 'created_at'
  }

  return {
    // State
    currentTraining,
    trainingList,
    schedule,
    participants,
    trainingSettings,
    isLoading,
    searchQuery,
    filterStatus,
    sortBy,

    // Getters
    filteredTrainingList,
    trainingStats,
    currentTrainingSchedule,

    // Actions
    fetchTrainingList,
    setCurrentTraining,
    createTraining,
    updateTraining,
    deleteTraining,
    duplicateTraining,
    addScheduleEvent,
    removeScheduleEvent,
    updateScheduleEvent,
    setParticipants,
    addParticipant,
    removeParticipant,
    setSearchQuery,
    setFilterStatus,
    setSortBy,
    clearFilters,
  }
})
