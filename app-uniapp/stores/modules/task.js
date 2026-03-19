import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useTaskStore = defineStore('task', () => {
  // State
  const taskList = ref([])
  const currentTask = ref(null)
  const taskFilter = ref('all') // all, pending, in-progress, completed, overdue
  const taskSortBy = ref('deadline') // deadline, priority, status, created_at
  const isLoading = ref(false)
  const taskStats = ref({
    total: 0,
    pending: 0,
    inProgress: 0,
    completed: 0,
    overdue: 0,
  })

  // Getters
  const filteredTasks = computed(() => {
    let filtered = [...taskList.value]

    // Apply filter
    if (taskFilter.value !== 'all') {
      if (taskFilter.value === 'overdue') {
        const now = new Date()
        filtered = filtered.filter((t) => {
          const deadline = new Date(t.deadline)
          return deadline < now && t.status !== 'completed'
        })
      } else {
        filtered = filtered.filter((t) => t.status === taskFilter.value)
      }
    }

    // Apply sort
    filtered.sort((a, b) => {
      switch (taskSortBy.value) {
        case 'priority':
          const priorityMap = { high: 1, medium: 2, low: 3 }
          return (
            (priorityMap[a.priority] || 3) - (priorityMap[b.priority] || 3)
          )
        case 'status':
          return a.status?.localeCompare(b.status) || 0
        case 'created_at':
          return (
            new Date(b.created_at) - new Date(a.created_at)
          )
        case 'deadline':
        default:
          return new Date(a.deadline) - new Date(b.deadline)
      }
    })

    return filtered
  })

  const tasksPriority = computed(() => {
    const now = new Date()
    return taskList.value
      .map((task) => {
        const deadline = new Date(task.deadline)
        const daysLeft = Math.ceil(
          (deadline - now) / (1000 * 60 * 60 * 24)
        )

        return {
          ...task,
          daysLeft,
          isPriority: daysLeft <= 3 && task.status !== 'completed',
        }
      })
      .sort((a, b) => {
        if (a.isPriority && !b.isPriority) return -1
        if (!a.isPriority && b.isPriority) return 1
        return a.daysLeft - b.daysLeft
      })
  })

  // Actions
  const fetchTasks = async (filters = {}) => {
    isLoading.value = true
    try {
      // const response = await taskAPI.getList(filters)
      // taskList.value = response.data
      updateTaskStats()
      return taskList.value
    } catch (error) {
      console.error('Failed to fetch tasks:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const fetchTaskDetail = async (taskId) => {
    isLoading.value = true
    try {
      // const response = await taskAPI.getDetail(taskId)
      // currentTask.value = response.data
      currentTask.value = taskList.value.find((t) => t.id === taskId)
      return currentTask.value
    } catch (error) {
      console.error('Failed to fetch task detail:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const createTask = async (taskData) => {
    isLoading.value = true
    try {
      // const response = await taskAPI.create(taskData)
      // const newTask = response.data
      const newTask = {
        id: Date.now(),
        ...taskData,
        created_at: new Date().toISOString(),
        status: 'pending',
      }
      taskList.value.unshift(newTask)
      updateTaskStats()
      return newTask
    } catch (error) {
      console.error('Failed to create task:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const updateTask = async (taskId, updates) => {
    isLoading.value = true
    try {
      // const response = await taskAPI.update(taskId, updates)
      // const updatedTask = response.data
      const index = taskList.value.findIndex((t) => t.id === taskId)
      if (index >= 0) {
        taskList.value[index] = {
          ...taskList.value[index],
          ...updates,
          updated_at: new Date().toISOString(),
        }

        if (currentTask.value?.id === taskId) {
          currentTask.value = taskList.value[index]
        }
      }

      updateTaskStats()
      return taskList.value[index]
    } catch (error) {
      console.error('Failed to update task:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const deleteTask = async (taskId) => {
    isLoading.value = true
    try {
      // await taskAPI.delete(taskId)
      taskList.value = taskList.value.filter((t) => t.id !== taskId)
      if (currentTask.value?.id === taskId) {
        currentTask.value = null
      }
      updateTaskStats()
      return true
    } catch (error) {
      console.error('Failed to delete task:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  const updateTaskStatus = async (taskId, status) => {
    return updateTask(taskId, { status })
  }

  const completeTask = async (taskId) => {
    return updateTask(taskId, {
      status: 'completed',
      completed_at: new Date().toISOString(),
    })
  }

  const setCurrentTask = (task) => {
    currentTask.value = task
  }

  const setTaskFilter = (filter) => {
    taskFilter.value = filter
  }

  const setTaskSortBy = (sortBy) => {
    taskSortBy.value = sortBy
  }

  const updateTaskStats = () => {
    const now = new Date()
    taskStats.value = {
      total: taskList.value.length,
      pending: taskList.value.filter((t) => t.status === 'pending').length,
      inProgress: taskList.value.filter((t) => t.status === 'in-progress')
        .length,
      completed: taskList.value.filter((t) => t.status === 'completed')
        .length,
      overdue: taskList.value.filter((t) => {
        const deadline = new Date(t.deadline)
        return deadline < now && t.status !== 'completed'
      }).length,
    }
  }

  const assignTask = async (taskId, assignees) => {
    return updateTask(taskId, { assignees })
  }

  const addTaskComment = async (taskId, comment) => {
    const task = taskList.value.find((t) => t.id === taskId)
    if (task) {
      if (!task.comments) {
        task.comments = []
      }
      task.comments.push({
        id: Date.now(),
        text: comment,
        created_at: new Date().toISOString(),
      })
      return true
    }
    return false
  }

  const getTasksByAssignee = (assigneeId) => {
    return taskList.value.filter((t) =>
      t.assignees?.includes(assigneeId)
    )
  }

  const getOverdueTasks = () => {
    const now = new Date()
    return taskList.value.filter((t) => {
      const deadline = new Date(t.deadline)
      return deadline < now && t.status !== 'completed'
    })
  }

  const getUpcomingTasks = (daysAhead = 7) => {
    const now = new Date()
    const futureDate = new Date(now.getTime() + daysAhead * 24 * 60 * 60 * 1000)

    return taskList.value
      .filter((t) => {
        const deadline = new Date(t.deadline)
        return deadline >= now && deadline <= futureDate && t.status !== 'completed'
      })
      .sort((a, b) => new Date(a.deadline) - new Date(b.deadline))
  }

  return {
    // State
    taskList,
    currentTask,
    taskFilter,
    taskSortBy,
    isLoading,
    taskStats,

    // Getters
    filteredTasks,
    tasksPriority,

    // Actions
    fetchTasks,
    fetchTaskDetail,
    createTask,
    updateTask,
    deleteTask,
    updateTaskStatus,
    completeTask,
    setCurrentTask,
    setTaskFilter,
    setTaskSortBy,
    updateTaskStats,
    assignTask,
    addTaskComment,
    getTasksByAssignee,
    getOverdueTasks,
    getUpcomingTasks,
  }
})
