/**
 * 交互工具库 - 智能会议系统
 * 提供统一的交互反馈、表单验证、确认对话框等功能
 */

class InteractionUtils {
    constructor() {
        // 延迟初始化，等待DOM加载完成
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.init());
        } else {
            // DOM已经加载完成
            this.init();
        }
    }

    init() {
        // 创建全局容器
        this.createContainers();
    }

    createContainers() {
        // 确保 document.body 存在
        if (!document.body) {
            console.warn('document.body not ready, retrying...');
            setTimeout(() => this.createContainers(), 100);
            return;
        }

        // 创建加载遮罩容器
        if (!document.getElementById('loadingOverlay')) {
            const loadingOverlay = document.createElement('div');
            loadingOverlay.id = 'loadingOverlay';
            loadingOverlay.className = 'loading-overlay';
            loadingOverlay.innerHTML = `
                <div>
                    <div class="loading-spinner"></div>
                    <div class="loading-content">处理中...</div>
                </div>
            `;
            document.body.appendChild(loadingOverlay);
        }

        // 创建提示消息容器
        if (!document.getElementById('toastContainer')) {
            const toastContainer = document.createElement('div');
            toastContainer.id = 'toastContainer';
            toastContainer.className = 'toast-container';
            document.body.appendChild(toastContainer);
        }

        // 创建确认对话框容器
        if (!document.getElementById('confirmDialog')) {
            const confirmDialog = document.createElement('div');
            confirmDialog.id = 'confirmDialog';
            confirmDialog.className = 'confirm-dialog';
            confirmDialog.innerHTML = `
                <div class="confirm-dialog-content">
                    <div class="confirm-dialog-header">
                        <div class="confirm-dialog-icon warning">
                            <i class="fas fa-exclamation-triangle"></i>
                        </div>
                        <h3 class="confirm-dialog-title">确认操作</h3>
                    </div>
                    <div class="confirm-dialog-message"></div>
                    <div class="confirm-dialog-actions">
                        <button class="btn-outline" id="confirmCancel">取消</button>
                        <button class="btn-primary" id="confirmOk">确认</button>
                    </div>
                </div>
            `;
            document.body.appendChild(confirmDialog);
        }
    }

    /**
     * 显示加载状态
     * @param {string} message - 加载提示信息
     */
    showLoading(message = '处理中...') {
        // 确保容器已创建
        if (!document.getElementById('loadingOverlay')) {
            this.createContainers();
        }
        const overlay = document.getElementById('loadingOverlay');
        if (!overlay) {
            console.error('Loading overlay not found');
            return;
        }
        const content = overlay.querySelector('.loading-content');
        if (content) {
            content.textContent = message;
        }
        overlay.classList.add('active');
    }

    /**
     * 隐藏加载状态
     */
    hideLoading() {
        const overlay = document.getElementById('loadingOverlay');
        if (overlay) {
            overlay.classList.remove('active');
        }
    }

    /**
     * 显示提示消息
     * @param {string} message - 消息内容
     * @param {string} type - 消息类型: success, error, warning, info
     * @param {number} duration - 显示时长（毫秒），0表示不自动关闭
     */
    showToast(message, type = 'info', duration = 3000) {
        // 确保容器已创建
        if (!document.getElementById('toastContainer')) {
            this.createContainers();
        }
        const container = document.getElementById('toastContainer');
        if (!container) {
            console.error('Toast container not found');
            return;
        }
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        const icons = {
            success: 'fa-check-circle',
            error: 'fa-exclamation-circle',
            warning: 'fa-exclamation-triangle',
            info: 'fa-info-circle'
        };

        toast.innerHTML = `
            <i class="fas ${icons[type] || icons.info} toast-icon"></i>
            <div class="toast-content">
                <div class="toast-message">${message}</div>
            </div>
            <button class="toast-close" onclick="this.parentElement.remove()">
                <i class="fas fa-times"></i>
            </button>
        `;

        container.appendChild(toast);

        // 自动移除
        if (duration > 0) {
            setTimeout(() => {
                toast.style.animation = 'slideInRight 0.3s ease-out reverse';
                setTimeout(() => toast.remove(), 300);
            }, duration);
        }
    }

    /**
     * 显示确认对话框
     * @param {string} message - 确认消息
     * @param {string} title - 对话框标题
     * @param {string} type - 对话框类型: warning, danger, info
     * @returns {Promise<boolean>} - 用户确认结果
     */
    showConfirm(message, title = '确认操作', type = 'warning') {
        return new Promise((resolve) => {
            // 确保容器已创建
            if (!document.getElementById('confirmDialog')) {
                this.createContainers();
            }
            const dialog = document.getElementById('confirmDialog');
            if (!dialog) {
                console.error('Confirm dialog not found');
                resolve(false);
                return;
            }
            const icon = dialog.querySelector('.confirm-dialog-icon');
            const titleEl = dialog.querySelector('.confirm-dialog-title');
            const messageEl = dialog.querySelector('.confirm-dialog-message');
            const cancelBtn = document.getElementById('confirmCancel');
            const okBtn = document.getElementById('confirmOk');

            // 设置图标类型
            icon.className = `confirm-dialog-icon ${type}`;
            const icons = {
                warning: 'fa-exclamation-triangle',
                danger: 'fa-times-circle',
                info: 'fa-info-circle'
            };
            icon.innerHTML = `<i class="fas ${icons[type] || icons.warning}"></i>`;

            titleEl.textContent = title;
            messageEl.textContent = message;

            // 移除旧的事件监听器
            const newCancelBtn = cancelBtn.cloneNode(true);
            const newOkBtn = okBtn.cloneNode(true);
            cancelBtn.parentNode.replaceChild(newCancelBtn, cancelBtn);
            okBtn.parentNode.replaceChild(newOkBtn, okBtn);

            // 添加新的事件监听器
            newCancelBtn.onclick = () => {
                dialog.classList.remove('active');
                resolve(false);
            };

            newOkBtn.onclick = () => {
                dialog.classList.remove('active');
                resolve(true);
            };

            dialog.classList.add('active');
        });
    }

    /**
     * 表单验证
     * @param {HTMLElement} formElement - 表单元素
     * @param {Object} rules - 验证规则
     * @returns {Object} - {valid: boolean, errors: Object}
     */
    validateForm(formElement, rules) {
        const errors = {};
        let valid = true;

        for (const [field, rule] of Object.entries(rules)) {
            const input = formElement.querySelector(`[name="${field}"]`) || 
                         formElement.querySelector(`#${field}`) ||
                         formElement.querySelector(`[v-model="${field}"]`);
            
            if (!input) continue;

            const value = input.value || input.textContent || '';
            const fieldErrors = [];

            // 必填验证
            if (rule.required && !value.trim()) {
                fieldErrors.push(rule.requiredMessage || `${field}不能为空`);
                valid = false;
            }

            // 格式验证
            if (value && rule.pattern && !rule.pattern.test(value)) {
                fieldErrors.push(rule.patternMessage || `${field}格式不正确`);
                valid = false;
            }

            // 长度验证
            if (value && rule.minLength && value.length < rule.minLength) {
                fieldErrors.push(rule.minLengthMessage || `${field}至少需要${rule.minLength}个字符`);
                valid = false;
            }

            if (value && rule.maxLength && value.length > rule.maxLength) {
                fieldErrors.push(rule.maxLengthMessage || `${field}最多${rule.maxLength}个字符`);
                valid = false;
            }

            // 自定义验证
            if (value && rule.validator && typeof rule.validator === 'function') {
                const customError = rule.validator(value);
                if (customError) {
                    fieldErrors.push(customError);
                    valid = false;
                }
            }

            if (fieldErrors.length > 0) {
                errors[field] = fieldErrors;
                this.setFieldError(input, fieldErrors[0]);
            } else {
                this.setFieldSuccess(input);
            }
        }

        return { valid, errors };
    }

    /**
     * 设置字段错误状态
     */
    setFieldError(input, message) {
        const formGroup = input.closest('.form-group');
        if (formGroup) {
            formGroup.classList.remove('success');
            formGroup.classList.add('error');
            
            // 移除旧的错误消息
            const oldError = formGroup.querySelector('.form-error-message');
            if (oldError) {
                oldError.remove();
            }

            // 添加新的错误消息
            const errorEl = document.createElement('div');
            errorEl.className = 'form-error-message';
            errorEl.textContent = message;
            formGroup.appendChild(errorEl);
        }
    }

    /**
     * 设置字段成功状态
     */
    setFieldSuccess(input) {
        const formGroup = input.closest('.form-group');
        if (formGroup) {
            formGroup.classList.remove('error');
            formGroup.classList.add('success');
            
            // 移除错误消息
            const errorMsg = formGroup.querySelector('.form-error-message');
            if (errorMsg) {
                errorMsg.remove();
            }
        }
    }

    /**
     * 清除表单验证状态
     */
    clearFormValidation(formElement) {
        const formGroups = formElement.querySelectorAll('.form-group');
        formGroups.forEach(group => {
            group.classList.remove('error', 'success');
            const errorMsg = group.querySelector('.form-error-message');
            if (errorMsg) {
                errorMsg.remove();
            }
        });
    }

    /**
     * 显示进度条
     * @param {number} percent - 进度百分比 (0-100)
     * @param {string} message - 进度提示信息
     */
    showProgress(percent, message = '') {
        // 如果进度条不存在，创建一个
        let progressContainer = document.getElementById('progressContainer');
        if (!progressContainer) {
            progressContainer = document.createElement('div');
            progressContainer.id = 'progressContainer';
            progressContainer.className = 'loading-overlay active';
            progressContainer.innerHTML = `
                <div style="background: var(--glass-bg); padding: 2rem; border-radius: 1rem; min-width: 300px;">
                    <div class="progress-container">
                        <div class="progress-bar" style="width: ${percent}%"></div>
                    </div>
                    <div class="progress-text">${message || `${percent}%`}</div>
                </div>
            `;
            document.body.appendChild(progressContainer);
        } else {
            const progressBar = progressContainer.querySelector('.progress-bar');
            const progressText = progressContainer.querySelector('.progress-text');
            if (progressBar) {
                progressBar.style.width = `${percent}%`;
            }
            if (progressText) {
                progressText.textContent = message || `${percent}%`;
            }
        }
    }

    /**
     * 隐藏进度条
     */
    hideProgress() {
        const progressContainer = document.getElementById('progressContainer');
        if (progressContainer) {
            progressContainer.classList.remove('active');
            setTimeout(() => {
                if (progressContainer.parentNode) {
                    progressContainer.parentNode.removeChild(progressContainer);
                }
            }, 300);
        }
    }
}

// 创建全局实例（延迟初始化）
let interactionUtilsInstance = null;

// 确保在DOM加载完成后初始化
function initInteractionUtils() {
    if (!interactionUtilsInstance) {
        interactionUtilsInstance = new InteractionUtils();
        window.InteractionUtils = interactionUtilsInstance;
    }
    return interactionUtilsInstance;
}

// 如果DOM已经加载完成，立即初始化
if (document.readyState === 'complete' || document.readyState === 'interactive') {
    initInteractionUtils();
} else {
    document.addEventListener('DOMContentLoaded', initInteractionUtils);
}

// Vue混入，方便在Vue组件中使用
window.InteractionMixin = {
    methods: {
        $showLoading(message) {
            if (!window.InteractionUtils) {
                initInteractionUtils();
            }
            window.InteractionUtils.showLoading(message);
        },
        $hideLoading() {
            if (!window.InteractionUtils) {
                initInteractionUtils();
            }
            window.InteractionUtils.hideLoading();
        },
        $showToast(message, type, duration) {
            if (!window.InteractionUtils) {
                initInteractionUtils();
            }
            window.InteractionUtils.showToast(message, type, duration);
        },
        $showConfirm(message, title, type) {
            if (!window.InteractionUtils) {
                initInteractionUtils();
            }
            return window.InteractionUtils.showConfirm(message, title, type);
        },
        $validateForm(formElement, rules) {
            if (!window.InteractionUtils) {
                initInteractionUtils();
            }
            return window.InteractionUtils.validateForm(formElement, rules);
        },
        $showProgress(percent, message) {
            if (!window.InteractionUtils) {
                initInteractionUtils();
            }
            window.InteractionUtils.showProgress(percent, message);
        },
        $hideProgress() {
            if (!window.InteractionUtils) {
                initInteractionUtils();
            }
            window.InteractionUtils.hideProgress();
        }
    }
};

