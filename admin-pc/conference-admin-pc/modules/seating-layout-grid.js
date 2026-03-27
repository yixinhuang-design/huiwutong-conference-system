/**
 * Grid多块布局模块（简化版）
 * 
 * 思路：流式布局 + CSS Grid定位，不搞复杂的像素计算。
 * 每个座位区就是一个Grid子项，根据座位列数决定占多少比例宽度。
 * 
 * 注意：此模块为工具类，不直接使用Vue的ref/reactive
 */

/**
 * 从area对象安全提取行数和列数
 * area.rows 可能是数组(行对象[])或数字，area.cols 可能不存在
 */
const getAreaDimensions = (area) => {
    let rowCount = 0;
    let colCount = 0;

    if (Array.isArray(area.rows)) {
        rowCount = area.rows.length;
        // 取第一行的座位数作为列数
        if (area.rows.length > 0 && area.rows[0].seats) {
            colCount = area.rows[0].seats.length;
        }
    } else if (typeof area.rows === 'number') {
        rowCount = area.rows;
    }

    // area.cols 如果存在且是数字，优先用
    if (typeof area.cols === 'number' && area.cols > 0) {
        colCount = area.cols;
    }

    return {
        rowCount: Math.max(1, rowCount),
        colCount: Math.max(1, colCount)
    };
};

/**
 * 获取响应式间距
 */
const getResponsiveGap = () => {
    if (typeof window === 'undefined') return 15;
    const width = window.innerWidth;
    if (width < 768) return 8;
    if (width < 1024) return 10;
    return 15;
};

/**
 * 计算座位区在Grid中应占用的大小
 * 简单逻辑：根据座位列数分成 小/中/大 三档
 */
const calculateAreaGridSize = (area) => {
    // 如果用户手动配置了gridConfig，优先使用
    if (area.gridConfig && area.gridConfig.gridCols && area.gridConfig.gridRows) {
        const cols = Number(area.gridConfig.gridCols);
        const rows = Number(area.gridConfig.gridRows);
        if (!isNaN(cols) && !isNaN(rows) && cols > 0 && rows > 0) {
            return { cols: Math.floor(cols), rows: Math.floor(rows) };
        }
    }

    const { rowCount, colCount } = getAreaDimensions(area);

    // 简单分档：根据座位列数决定占几份Grid列
    // ≤8列 → 占1份，≤15列 → 占2份，>15列 → 占3份
    let gridCols, gridRows;
    if (colCount <= 8) {
        gridCols = 1;
    } else if (colCount <= 15) {
        gridCols = 2;
    } else {
        gridCols = 3;
    }

    // 行数：≤3行 → 占1行，否则占2行
    gridRows = rowCount <= 3 ? 1 : 2;

    return { cols: gridCols, rows: gridRows };
};

/**
 * 自动分配Grid位置（简单流式排列）
 */
const autoLayoutAreas = (areas, gridCols = 4) => {
    let currentCol = 1;
    let currentRow = 1;
    let maxRowHeight = 0;

    areas.forEach((area, index) => {
        const { cols: areaCols, rows: areaRows } = calculateAreaGridSize(area);
        const { rowCount, colCount } = getAreaDimensions(area);

        // 放不下就换行
        if (currentCol + areaCols - 1 > gridCols) {
            currentRow += maxRowHeight;
            currentCol = 1;
            maxRowHeight = 0;
        }

        if (!area.gridLayout) {
            area.gridLayout = {};
        }
        area.gridLayout.gridColumn = `${currentCol} / span ${areaCols}`;
        area.gridLayout.gridRow = `${currentRow} / span ${areaRows}`;
        area.gridLayout.order = index;
        area.gridLayout.displayLayout = 'grid';

        currentCol += areaCols;
        maxRowHeight = Math.max(maxRowHeight, areaRows);

        console.log(`✅ "${area.name}"(${colCount}×${rowCount}座) → Grid列${currentCol - areaCols}~${currentCol - 1}, 行${currentRow}`);
    });

    const totalRows = currentRow + maxRowHeight - 1;
    console.log(`✅ Grid布局完成：${gridCols}列×${totalRows}行`);
    return { cols: gridCols, rows: totalRows };
};

/**
 * 应用布局配置
 */
const applyLayoutConfig = (layoutConfigs, currentLayoutMode, seatingAreas, layoutName = 'flowLayout') => {
    const config = layoutConfigs[layoutName];
    if (!config) {
        console.warn(`布局${layoutName}不存在`);
        return;
    }

    currentLayoutMode.value = layoutName;

    if (layoutName === 'conferenceHall') {
        autoLayoutAreas(seatingAreas.value, 4);
    } else {
        // 流式布局：每个区域自动排列
        seatingAreas.value.forEach(area => {
            if (!area.gridLayout) area.gridLayout = {};
            area.gridLayout.gridColumn = 'auto';
            area.gridLayout.gridRow = 'auto';
            area.gridLayout.displayLayout = 'flow';
        });
    }
};

const assignGridPosition = (area, index) => { /* 兼容 */ };

/**
 * 获取座位区的Grid CSS属性
 */
const getAreaGridStyle = (area) => {
    if (!area.gridLayout || area.gridLayout.displayLayout !== 'grid') {
        return {};
    }
    return {
        'grid-column': area.gridLayout.gridColumn,
        'grid-row': area.gridLayout.gridRow,
    };
};

/**
 * 切换布局模式
 */
const switchLayout = (layoutConfigs, currentLayoutMode, seatingAreas, layoutName) => {
    applyLayoutConfig(layoutConfigs, currentLayoutMode, seatingAreas, layoutName);
};

/**
 * 编辑区域后重新应用布局
 */
const reapplyLayoutAfterEdit = (layoutConfigs, currentLayoutMode, seatingAreas, area) => {
    if (currentLayoutMode.value === 'conferenceHall') {
        autoLayoutAreas(seatingAreas.value, 4);
    }
};

/**
 * 初始化Grid布局
 */
const initializeGridLayout = (seatingAreas) => {
    seatingAreas.value.forEach((area, index) => {
        if (!area.gridLayout) {
            area.gridLayout = {
                gridColumn: 'auto',
                gridRow: 'auto',
                order: index,
                displayLayout: 'flow'
            };
        }
    });
};

// 兼容旧接口，不再需要的函数保留空壳
const getResponsiveGridCols = () => 4;
const calculateRequiredRows = () => 10;

export default {
    getResponsiveGridCols,
    getResponsiveGap,
    calculateRequiredRows,
    calculateAreaGridSize,
    autoLayoutAreas,
    applyLayoutConfig,
    assignGridPosition,
    getAreaGridStyle,
    switchLayout,
    reapplyLayoutAfterEdit,
    initializeGridLayout
};
