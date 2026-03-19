/**
 * 数据可视化模块
 * 基于Chart.js实现各类数据图表
 */

class DataVisualization {
    constructor(conferenceId) {
        this.conferenceId = conferenceId;
        this.charts = new Map();
        this.chartInstances = new Map();
    }
    
    /**
     * 创建报名趋势图表
     */
    createRegistrationTrendChart(canvasId, data) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) {
            console.error('Canvas元素不存在:', canvasId);
            return null;
        }
        
        const chart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels || ['6月8日', '6月9日', '6月10日', '6月11日', '6月12日', '6月13日', '6月14日'],
                datasets: [{
                    label: '报名人数',
                    data: data.values || [120, 145, 167, 189, 210, 245, 356],
                    borderColor: '#667eea',
                    backgroundColor: 'rgba(102, 126, 234, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointBackgroundColor: '#667eea',
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 5,
                    pointHoverRadius: 7
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleColor: '#fff',
                        bodyColor: '#fff',
                        borderColor: '#667eea',
                        borderWidth: 1
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        },
                        ticks: {
                            color: '#666'
                        }
                    },
                    x: {
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        },
                        ticks: {
                            color: '#666'
                        }
                    }
                },
                interaction: {
                    mode: 'nearest',
                    axis: 'x',
                    intersect: false
                }
            }
        });
        
        this.chartInstances.set(canvasId, chart);
        return chart;
    }
    
    /**
     * 创建任务完成图表
     */
    createTaskCompletionChart(canvasId, data) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return null;
        
        const chart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels || ['张三', '李四', '王五', '赵六', '钱七'],
                datasets: [{
                    label: '完成任务数',
                    data: data.values || [12, 8, 15, 6, 9],
                    backgroundColor: [
                        'rgba(102, 126, 234, 0.8)',
                        'rgba(118, 75, 162, 0.8)',
                        'rgba(240, 147, 251, 0.8)',
                        'rgba(79, 172, 254, 0.8)',
                        'rgba(0, 242, 254, 0.8)'
                    ],
                    borderColor: [
                        '#667eea',
                        '#764ba2',
                        '#f093fb',
                        '#4facfe',
                        '#00f2fe'
                    ],
                    borderWidth: 2,
                    borderRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
        
        this.chartInstances.set(canvasId, chart);
        return chart;
    }
    
    /**
     * 创建考勤分布图表
     */
    createAttendanceChart(canvasId, data) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return null;
        
        const chart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.labels || ['已签到', '未签到'],
                datasets: [{
                    data: data.values || [328, 28],
                    backgroundColor: [
                        'rgba(16, 185, 129, 0.8)',
                        'rgba(245, 158, 11, 0.8)'
                    ],
                    borderColor: [
                        '#10b981',
                        '#f59e0b'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            font: {
                                size: 14
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = context.parsed || 0;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((value / total) * 100).toFixed(1);
                                return `${label}: ${value}人 (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
        
        this.chartInstances.set(canvasId, chart);
        return chart;
    }
    
    /**
     * 创建群组活跃度图表
     */
    createGroupActivityChart(canvasId, data) {
        const ctx = document.getElementById(canvasId);
        if (!ctx) return null;
        
        const chart = new Chart(ctx, {
            type: 'radar',
            data: {
                labels: data.labels || ['消息量', '活跃度', '参与度', '响应速度', '满意度'],
                datasets: data.datasets || [
                    {
                        label: '第一学习小组',
                        data: [85, 78, 92, 88, 90],
                        borderColor: 'rgba(102, 126, 234, 0.8)',
                        backgroundColor: 'rgba(102, 126, 234, 0.2)',
                        pointBackgroundColor: '#667eea',
                        pointBorderColor: '#fff',
                        pointHoverBackgroundColor: '#fff',
                        pointHoverBorderColor: '#667eea'
                    },
                    {
                        label: '第二学习小组',
                        data: [72, 85, 78, 82, 85],
                        borderColor: 'rgba(118, 75, 162, 0.8)',
                        backgroundColor: 'rgba(118, 75, 162, 0.2)',
                        pointBackgroundColor: '#764ba2',
                        pointBorderColor: '#fff',
                        pointHoverBackgroundColor: '#fff',
                        pointHoverBorderColor: '#764ba2'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    r: {
                        beginAtZero: true,
                        max: 100,
                        grid: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        },
                        angleLines: {
                            color: 'rgba(255, 255, 255, 0.1)'
                        },
                        pointLabels: {
                            font: {
                                size: 12
                            }
                        }
                    }
                },
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
        
        this.chartInstances.set(canvasId, chart);
        return chart;
    }
    
    /**
     * 更新图表数据
     */
    updateChart(canvasId, newData) {
        const chart = this.chartInstances.get(canvasId);
        if (!chart) {
            console.error('图表不存在:', canvasId);
            return false;
        }
        
        try {
            if (newData.labels) {
                chart.data.labels = newData.labels;
            }
            
            if (newData.datasets) {
                chart.data.datasets = newData.datasets;
            } else if (newData.values) {
                chart.data.datasets[0].data = newData.values;
            }
            
            chart.update('active');
            return true;
        } catch (error) {
            console.error('更新图表失败:', error);
            return false;
        }
    }
    
    /**
     * 销毁图表
     */
    destroyChart(canvasId) {
        const chart = this.chartInstances.get(canvasId);
        if (chart) {
            chart.destroy();
            this.chartInstances.delete(canvasId);
        }
    }
    
    /**
     * 销毁所有图表
     */
    destroyAllCharts() {
        this.chartInstances.forEach((chart, canvasId) => {
            chart.destroy();
        });
        this.chartInstances.clear();
    }
    
    /**
     * 导出图表为图片
     */
    exportChartAsImage(canvasId) {
        const chart = this.chartInstances.get(canvasId);
        if (!chart) {
            console.error('图表不存在:', canvasId);
            return null;
        }
        
        return chart.toBase64Image();
    }
}

// 导出
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DataVisualization;
}

// 全局实例
window.dataVisualization = null;

/**
 * 初始化数据可视化
 */
function initializeDataVisualization(conferenceId) {
    window.dataVisualization = new DataVisualization(conferenceId);
    return window.dataVisualization;
}
