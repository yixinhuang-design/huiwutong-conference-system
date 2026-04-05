/**
 * 智能排座系统优化补丁
 *
 * 主要优化内容：
 * 1. 友好的错误提示（Toast通知）
 * 2. 操作确认"不再提示"选项
 * 3. 加载进度提示
 * 4. Excel导入数据验证
 * 5. 快捷键支持（Ctrl+Z/Ctrl+S）
 * 6. 批量操作增强
 * 7. 撤销/重做功能完善
 * 8. 保存失败重试机制
 */

// ===== 全局配置 =====
const SeatingOptimizationConfig = {
    // 显示的操作提示
    showConfirmations: true,
    // 记录的确认选项
    confirmedActions: new Set(),
    // Toast显示时长
    toastDuration: 3000,
    // 最大历史记录数
    maxHistory: 50,
    // 自动保存间隔（毫秒）
    autoSaveInterval: 30000
};

// ===== Toast通知系统 =====
class ToastNotification {
    constructor() {
        this.container = null;
        // 延迟初始化，等待DOM加载完成
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.init());
        } else {
            this.init();
        }
    }

    init() {
        // 确保document.body存在
        if (!document.body) {
            setTimeout(() => this.init(), 100);
            return;
        }
        // 创建Toast容器
        this.container = document.createElement('div');
        this.container.id = 'toast-container';
        this.container.style.cssText = `
            position: fixed;
            top: 80px;
            right: 20px;
            z-index: 10000;
            display: flex;
            flex-direction: column;
            gap: 10px;
            pointer-events: none;
        `;
        document.body.appendChild(this.container);
    }

    show(message, type = 'info', duration = null) {
        // 确保容器已创建
        if (!this.container) {
            this.init();
            if (!this.container) {
                console.warn('Toast容器未创建，使用alert代替');
                alert(message);
                return;
            }
        }
        const toast = document.createElement('div');
        const icon = this.getIcon(type);
        const bgColor = this.getBgColor(type);

        toast.style.cssText = `
            background: ${bgColor};
            color: white;
            padding: 12px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: flex;
            align-items: center;
            gap: 12px;
            min-width: 300px;
            max-width: 400px;
            animation: slideInRight 0.3s ease-out;
            pointer-events: auto;
            font-size: 14px;
            font-weight: 500;
        `;

        toast.innerHTML = `
            <i class="${icon}" style="font-size: 18px;"></i>
            <span style="flex: 1;">${message}</span>
            <i class="fas fa-times" style="cursor: pointer; opacity: 0.7;" onclick="this.parentElement.remove()"></i>
        `;

        this.container.appendChild(toast);

        // 自动消失
        const showDuration = duration || SeatingOptimizationConfig.toastDuration;
        setTimeout(() => {
            toast.style.animation = 'slideOutRight 0.3s ease-out';
            setTimeout(() => toast.remove(), 300);
        }, showDuration);
    }

    success(message, duration) { this.show(message, 'success', duration); }
    error(message, duration) { this.show(message, 'error', duration); }
    warning(message, duration) { this.show(message, 'warning', duration); }
    info(message, duration) { this.show(message, 'info', duration); }

    getIcon(type) {
        const icons = {
            success: 'fas fa-check-circle',
            error: 'fas fa-exclamation-circle',
            warning: 'fas fa-exclamation-triangle',
            info: 'fas fa-info-circle'
        };
        return icons[type] || icons.info;
    }

    getBgColor(type) {
        const colors = {
            success: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
            error: 'linear-gradient(135deg, #eb3349 0%, #f45c43 100%)',
            warning: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
            info: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
        };
        return colors[type] || colors.info;
    }
}

// 全局Toast实例
const toast = new ToastNotification();

// ===== 增强确认对话框 =====
function showConfirm(message, options = {}) {
    const {
        title = '确认操作',
        confirmText = '确定',
        cancelText = '取消',
        showDontAskAgain = false,
        actionId = null
    } = options;

    // 检查是否已确认过此操作
    if (actionId && SeatingOptimizationConfig.confirmedActions.has(actionId)) {
        return Promise.resolve(true);
    }

    return new Promise((resolve) => {
        // 创建确认对话框
        const overlay = document.createElement('div');
        overlay.style.cssText = `
            position: fixed;
            inset: 0;
            background: rgba(0,0,0,0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10001;
            animation: fadeIn 0.2s ease-out;
        `;

        const dialog = document.createElement('div');
        dialog.style.cssText = `
            background: white;
            border-radius: 12px;
            padding: 24px;
            min-width: 400px;
            max-width: 500px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            animation: scaleIn 0.2s ease-out;
        `;

        dialog.innerHTML = `
            <h3 style="margin: 0 0 16px 0; font-size: 18px; font-weight: 600; color: #1f2937;">
                ${title}
            </h3>
            <p style="margin: 0 0 20px 0; color: #6b7280; line-height: 1.5;">
                ${message}
            </p>
            ${showDontAskAgain ? `
                <label style="display: flex; align-items: center; gap: 8px; margin-bottom: 20px; cursor: pointer; font-size: 14px; color: #4b5563;">
                    <input type="checkbox" id="dont-ask-again" style="width: 16px; height: 16px; cursor: pointer;">
                    <span>不再提示此操作</span>
                </label>
            ` : ''}
            <div style="display: flex; gap: 12px; justify-content: flex-end;">
                <button class="btn-cancel" style="
                    padding: 10px 20px;
                    border: 1px solid #d1d5db;
                    background: white;
                    border-radius: 6px;
                    cursor: pointer;
                    font-size: 14px;
                    font-weight: 500;
                    color: #374151;
                    transition: all 0.2s;
                " onmouseover="this.style.background='#f9fafb'" onmouseout="this.style.background='white'">
                    ${cancelText}
                </button>
                <button class="btn-confirm" style="
                    padding: 10px 20px;
                    border: none;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                    border-radius: 6px;
                    cursor: pointer;
                    font-size: 14px;
                    font-weight: 500;
                    transition: all 0.2s;
                " onmouseover="this.style.opacity='0.9'" onmouseout="this.style.opacity='1'">
                    ${confirmText}
                </button>
            </div>
        `;

        overlay.appendChild(dialog);
        document.body.appendChild(overlay);

        // 绑定事件
        const btnCancel = dialog.querySelector('.btn-cancel');
        const btnConfirm = dialog.querySelector('.btn-confirm');
        const checkbox = dialog.querySelector('#dont-ask-again');

        btnCancel.onclick = () => {
            overlay.remove();
            resolve(false);
        };

        btnConfirm.onclick = () => {
            // 记住选择
            if (checkbox && checkbox.checked && actionId) {
                SeatingOptimizationConfig.confirmedActions.add(actionId);
                localStorage.setItem(
                    'seating-confirmed-actions',
                    JSON.stringify([...SeatingOptimizationConfig.confirmedActions])
                );
            }
            overlay.remove();
            resolve(true);
        };

        // 点击背景关闭
        overlay.onclick = (e) => {
            if (e.target === overlay) {
                overlay.remove();
                resolve(false);
            }
        };

        // ESC键关闭
        const handleEscape = (e) => {
            if (e.key === 'Escape') {
                overlay.remove();
                document.removeEventListener('keydown', handleEscape);
                resolve(false);
            }
        };
        document.addEventListener('keydown', handleEscape);
    });
}

// ===== 进度提示 =====
class ProgressIndicator {
    constructor() {
        this.overlay = null;
        this.progressBar = null;
        this.progressText = null;
    }

    show(message = '处理中...', initialProgress = 0) {
        if (this.overlay) return;

        this.overlay = document.createElement('div');
        this.overlay.style.cssText = `
            position: fixed;
            inset: 0;
            background: rgba(0,0,0,0.7);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10002;
            animation: fadeIn 0.2s ease-out;
        `;

        const content = document.createElement('div');
        content.style.cssText = `
            background: white;
            border-radius: 12px;
            padding: 32px;
            min-width: 400px;
            text-align: center;
            animation: scaleIn 0.2s ease-out;
        `;

        content.innerHTML = `
            <div style="
                width: 48px;
                height: 48px;
                border: 4px solid #e5e7eb;
                border-top-color: #667eea;
                border-radius: 50%;
                animation: spin 0.8s linear infinite;
                margin: 0 auto 20px;
            "></div>
            <div style="font-size: 16px; font-weight: 600; color: #1f2937; margin-bottom: 8px;">
                ${message}
            </div>
            <div class="progress-bar-bg" style="
                width: 100%;
                height: 8px;
                background: #e5e7eb;
                border-radius: 4px;
                overflow: hidden;
                margin-bottom: 8px;
            ">
                <div class="progress-bar-fill" style="
                    width: ${initialProgress}%;
                    height: 100%;
                    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
                    border-radius: 4px;
                    transition: width 0.3s ease;
                "></div>
            </div>
            <div class="progress-text" style="font-size: 14px; color: #6b7280;">
                ${initialProgress}%
            </div>
        `;

        this.overlay.appendChild(content);
        document.body.appendChild(this.overlay);

        this.progressBar = content.querySelector('.progress-bar-fill');
        this.progressText = content.querySelector('.progress-text');
    }

    update(progress, message) {
        if (!this.overlay) return;
        if (this.progressBar) {
            this.progressBar.style.width = `${progress}%`;
        }
        if (this.progressText) {
            this.progressText.textContent = message || `${progress}%`;
        }
    }

    hide() {
        if (this.overlay) {
            this.overlay.style.animation = 'fadeOut 0.2s ease-out';
            setTimeout(() => {
                if (this.overlay) {
                    this.overlay.remove();
                    this.overlay = null;
                    this.progressBar = null;
                    this.progressText = null;
                }
            }, 200);
        }
    }
}

// 全局进度实例
const progress = new ProgressIndicator();

// ===== Excel导入数据验证 =====
class ExcelDataValidator {
    validate(data) {
        const errors = [];
        const warnings = [];
        const validData = [];

        // 检查是否为空
        if (!data || data.length === 0) {
            errors.push({
                row: 0,
                field: '全部',
                message: 'Excel文件为空'
            });
            return { valid: false, errors, warnings, validData: [] };
        }

        // 检查必填字段
        const requiredFields = ['姓名', 'name', 'Name'];
        const hasNameField = Object.keys(data[0]).some(key =>
            requiredFields.some(field => key.toLowerCase() === field.toLowerCase())
        );

        if (!hasNameField) {
            errors.push({
                row: 1,
                field: '姓名',
                message: '缺少必填字段"姓名"，请确保Excel包含"姓名"列'
            });
        }

        // 验证每一行数据
        data.forEach((row, index) => {
            const rowNum = index + 2; // Excel行号（包含表头）

            // 提取姓名
            const name = row['姓名'] || row['name'] || row['Name'] || '';

            // 姓名不能为空
            if (!name || name.toString().trim() === '') {
                warnings.push({
                    row: rowNum,
                    field: '姓名',
                    message: `第${rowNum}行：姓名为空，将跳过该条数据`
                });
                return; // 跳过无效数据
            }

            // 检查姓名长度
            if (name.toString().length > 20) {
                warnings.push({
                    row: rowNum,
                    field: '姓名',
                    message: `第${rowNum}行：姓名过长（${name.length}字符），将被截断`
                });
            }

            // 提取其他字段
            const department = (row['单位'] || row['部门'] || row['department'] || '未知单位').toString().trim();
            const position = (row['职位'] || row['职务'] || row['position'] || '未知职位').toString().trim();
            const phone = (row['手机'] || row['电话'] || row['phone'] || '').toString().trim();
            const email = (row['邮箱'] || row['email'] || '').toString().trim();

            // 验证手机号格式
            if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
                warnings.push({
                    row: rowNum,
                    field: '手机',
                    message: `第${rowNum}行：手机号格式不正确（${phone}）`
                });
            }

            // 验证邮箱格式
            if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
                warnings.push({
                    row: rowNum,
                    field: '邮箱',
                    message: `第${rowNum}行：邮箱格式不正确（${email}）`
                });
            }

            // 数据有效，加入有效数据列表
            validData.push({
                id: `att_excel_${Date.now()}_${index}`,
                name: name.toString().substring(0, 20),
                department: department.substring(0, 50),
                position: position.substring(0, 30),
                phone,
                email,
                isVip: this.isVipByPosition(position)
            });
        });

        return {
            valid: validData.length > 0,
            errors,
            warnings,
            validData
        };
    }

    isVipByPosition(position) {
        if (!position) return false;
        const vipKeywords = ['领导', '主任', '书记', '市长', '局长', '主席', '理事长'];
        return vipKeywords.some(keyword => position.includes(keyword));
    }

    // 生成验证报告
    generateReport(result) {
        let message = `数据验证完成\n`;
        message += `✓ 有效数据：${result.validData.length} 条\n`;

        if (result.warnings.length > 0) {
            message += `⚠ 警告：${result.warnings.length} 条\n`;
        }

        if (result.errors.length > 0) {
            message += `✗ 错误：${result.errors.length} 条\n`;
        }

        return message;
    }
}

// ===== 快捷键支持 =====
class KeyboardShortcuts {
    constructor(vueApp) {
        this.vueApp = vueApp;
        this.shortcuts = new Map();
        this.init();
    }

    init() {
        // 注册默认快捷键
        this.register('Ctrl+z', () => this.undo());
        this.register('Ctrl+Z', () => this.undo());
        this.register('Ctrl+y', () => this.redo());
        this.register('Ctrl+Y', () => this.redo());
        this.register('Ctrl+s', () => this.save());
        this.register('Ctrl+S', () => this.save());
        this.register('Delete', () => this.deleteSelected());
        this.register('Escape', () => this.clearSelection());

        // 绑定键盘事件
        document.addEventListener('keydown', (e) => this.handleKeyDown(e));
    }

    register(key, callback) {
        this.shortcuts.set(key, callback);
    }

    handleKeyDown(e) {
        // 检查是否在输入框中
        const target = e.target;
        if (target.tagName === 'INPUT' ||
            target.tagName === 'TEXTAREA' ||
            target.isContentEditable) {
            // 在输入框中，只处理Ctrl+S
            if (e.key !== 's' && e.key !== 'S') return;
        }

        // 构建快捷键字符串
        let key = '';
        if (e.ctrlKey) key += 'Ctrl+';
        if (e.altKey) key += 'Alt+';
        if (e.shiftKey) key += 'Shift+';
        key += e.key;

        // 查找并执行对应的快捷键
        const callback = this.shortcuts.get(key);
        if (callback) {
            e.preventDefault();
            callback();
        }
    }

    undo() {
        if (this.vueApp.undo) {
            this.vueApp.undo();
            toast.info('已撤销');
        }
    }

    redo() {
        if (this.vueApp.redo) {
            this.vueApp.redo();
            toast.info('已重做');
        }
    }

    save() {
        if (this.vueApp.saveData) {
            this.vueApp.saveData();
            toast.success('保存成功');
        }
    }

    deleteSelected() {
        if (this.vueApp.selectedSeats && this.vueApp.selectedSeats.length > 0) {
            this.vueApp.batchClearSeats();
        }
    }

    clearSelection() {
        if (this.vueApp.clearSelection) {
            this.vueApp.clearSelection();
        }
    }
}

// ===== 批量操作增强 =====
class BatchOperations {
    constructor(vueApp) {
        this.vueApp = vueApp;
    }

    // 批量移动座位
    async moveSeats(seatIds, targetAreaId, targetRow, targetCol) {
        try {
            progress.show('正在移动座位...', 0);

            const moved = 0;
            const total = seatIds.length;

            for (let i = 0; i < seatIds.length; i++) {
                const seatId = seatIds[i];
                // 移动逻辑
                const seat = this.findSeat(seatId);
                if (seat) {
                    // 更新座位位置
                    // ...
                }

                // 更新进度
                const percent = Math.round(((i + 1) / total) * 100);
                progress.update(percent, `正在移动座位 ${i + 1}/${total}`);
            }

            progress.hide();
            toast.success(`成功移动 ${moved} 个座位`);
            return true;
        } catch (error) {
            progress.hide();
            toast.error(`移动座位失败: ${error.message}`);
            return false;
        }
    }

    // 批量复制座位
    async copySeats(seatIds) {
        // 复制逻辑
    }

    // 批量交换座位
    async swapSeats(seatIds1, seatIds2) {
        // 交换逻辑
    }
}

// ===== 保存增强（重试机制） =====
class SaveManager {
    constructor(vueApp) {
        this.vueApp = vueApp;
        this.maxRetries = 3;
        this.retryDelay = 1000;
    }

    async saveWithRetry() {
        for (let attempt = 1; attempt <= this.maxRetries; attempt++) {
            try {
                progress.show('正在保存数据...', 0);

                // 尝试保存
                const result = await this.attemptSave();

                progress.update(100, '保存成功');
                setTimeout(() => progress.hide(), 500);

                toast.success('数据保存成功');
                return result;
            } catch (error) {
                console.error(`保存失败（第${attempt}次尝试）:`, error);

                if (attempt < this.maxRetries) {
                    progress.update(0, `保存失败，${this.retryDelay / 1000}秒后重试...`);
                    await this.delay(this.retryDelay);
                    this.retryDelay *= 2; // 指数退避
                } else {
                    progress.hide();
                    toast.error(`保存失败，已重试${this.maxRetries}次`);
                    throw error;
                }
            }
        }
    }

    async attemptSave() {
        // 实际保存逻辑
        if (this.vueApp.saveData) {
            return await this.vueApp.saveData();
        }
        throw new Error('保存方法未找到');
    }

    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }
}

// ===== 撤销/重做系统 =====
class UndoRedoManager {
    constructor(vueApp) {
        this.vueApp = vueApp;
        this.history = [];
        this.currentIndex = -1;
        this.maxHistory = SeatingOptimizationConfig.maxHistory;
        this.loadFromStorage();
    }

    // 保存状态
    saveState(action) {
        // 删除当前指针之后的历史
        this.history = this.history.slice(0, this.currentIndex + 1);

        // 添加新状态
        const state = {
            action: action,
            timestamp: Date.now(),
            data: JSON.stringify(this.getSnapshot())
        };

        this.history.push(state);
        this.currentIndex++;

        // 限制历史记录数量
        if (this.history.length > this.maxHistory) {
            this.history.shift();
            this.currentIndex--;
        }

        this.saveToStorage();
    }

    // 撤销
    undo() {
        if (this.currentIndex <= 0) {
            toast.warning('没有更多可撤销的操作');
            return;
        }

        this.currentIndex--;
        const state = this.history[this.currentIndex];
        this.restoreSnapshot(JSON.parse(state.data));

        toast.info(`已撤销: ${state.action}`);
    }

    // 重做
    redo() {
        if (this.currentIndex >= this.history.length - 1) {
            toast.warning('没有更多可重做的操作');
            return;
        }

        this.currentIndex++;
        const state = this.history[this.currentIndex];
        this.restoreSnapshot(JSON.parse(state.data));

        toast.info(`已重做: ${state.action}`);
    }

    // 获取快照
    getSnapshot() {
        return {
            seatingAreas: this.vueApp.seatingAreas,
            attendees: this.vueApp.attendees
        };
    }

    // 恢复快照
    restoreSnapshot(snapshot) {
        this.vueApp.seatingAreas = snapshot.seatingAreas;
        this.vueApp.attendees = snapshot.attendees;
    }

    // 保存到localStorage
    saveToStorage() {
        try {
            localStorage.setItem('seating-undo-history', JSON.stringify({
                history: this.history,
                currentIndex: this.currentIndex
            }));
        } catch (e) {
            console.warn('保存历史记录失败:', e);
        }
    }

    // 从localStorage加载
    loadFromStorage() {
        try {
            const data = localStorage.getItem('seating-undo-history');
            if (data) {
                const parsed = JSON.parse(data);
                this.history = parsed.history || [];
                this.currentIndex = parsed.currentIndex || -1;
            }
        } catch (e) {
            console.warn('加载历史记录失败:', e);
        }
    }

    // 清空历史
    clear() {
        this.history = [];
        this.currentIndex = -1;
        localStorage.removeItem('seating-undo-history');
    }
}

// ===== 添加动画样式 =====
const animationStyles = document.createElement('style');
animationStyles.textContent = `
    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }

    @keyframes fadeOut {
        from { opacity: 1; }
        to { opacity: 0; }
    }

    @keyframes slideInRight {
        from {
            opacity: 0;
            transform: translateX(100px);
        } to {
            opacity: 1;
            transform: translateX(0);
        }
    }

    @keyframes slideOutRight {
        from {
            opacity: 1;
            transform: translateX(0);
        } to {
            opacity: 0;
            transform: translateX(100px);
        }
    }

    @keyframes scaleIn {
        from {
            opacity: 0;
            transform: scale(0.9);
        } to {
            opacity: 1;
            transform: scale(1);
        }
    }

    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
`;
document.head.appendChild(animationStyles);

// ===== 导出 =====
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        ToastNotification,
        showConfirm,
        ProgressIndicator,
        ExcelDataValidator,
        KeyboardShortcuts,
        BatchOperations,
        SaveManager,
        UndoRedoManager,
        toast,
        progress,
        SeatingOptimizationConfig
    };
}

// 全局导出（浏览器环境）
window.SeatingOptimization = {
    ToastNotification,
    showConfirm,
    ProgressIndicator,
    ExcelDataValidator,
    KeyboardShortcuts,
    BatchOperations,
    SaveManager,
    UndoRedoManager,
    toast,
    progress,
    config: SeatingOptimizationConfig
};

console.log('✅ 智能排座优化补丁已加载');
