/**
 * 座位编号系统模块
 * 
 * 功能：
 * - 支持多种座位编号方式（从左到右、从右到左、中心向外等）
 * - 动态生成座位编号标签
 * - 编号模式文本转换
 * 
 * 导出：
 * - NUMBERING_MODES: 编号模式配置对象
 * - generateOptimizedSeatNumber(seat, area): 生成座位号
 * - getNumberingModeLabel(mode): 获取编号方式标签
 */

// ===== 座位编号模式定义 =====
export const NUMBERING_MODES = {
    leftToRight: {
        name: '从左到右',
        description: '按照从左到右的顺序编号'
    },
    rightToLeft: {
        name: '从右到左',
        description: '按照从右到左的顺序编号'
    },
    topToBottom: {
        name: '从上到下',
        description: '按照从上到下的顺序编号'
    },
    bottomToTop: {
        name: '从下到上',
        description: '按照从下到上的顺序编号'
    },
    centerOutward: {
        name: '中心向外',
        description: '从中心向四周辐射编号（适用于舞台）'
    },
    alternatingLTRStart: {
        name: '左右交替（从左开始）',
        description: '奇数行从左到右，偶数行从右到左'
    },
    alternatingRTLStart: {
        name: '左右交替（从右开始）',
        description: '奇数行从右到左，偶数行从左到右'
    },
    custom: {
        name: '自定义',
        description: '按照自定义规则编号'
    }
};

/**
 * 生成优化的座位号
 * 
 * @param {object} seat - 座位对象 {id, row, col, number, ...}
 * @param {object} area - 座位区对象 {id, name, rows, cols, numberingMode, startNumber, ...}
 * @returns {string} 座位号字符串
 */
export const generateOptimizedSeatNumber = (seat, area) => {
    if (!seat || !area) {
        return '?';
    }

    // 处理过道座位
    if (seat.isAisle) {
        return '过';
    }

    const { row, col } = seat;
    const { numberingMode, startNumber = 1, cols, rows } = area;
    
    // 基础座位编号（从startNumber开始计算）
    let seatNumber = startNumber;

    switch (numberingMode) {
        case 'leftToRight':
            // 由左至右：num = (row-1) × cols + col
            seatNumber += (row - 1) * cols + (col - 1);
            break;

        case 'rightToLeft':
            // 由右至左：num = (row-1) × cols + (cols - col + 1)
            seatNumber += (row - 1) * cols + (cols - col);
            break;

        case 'topToBottom':
            // 从上到下：按列编号，每列从上到下
            seatNumber += (col - 1) * rows + (row - 1);
            break;

        case 'bottomToTop':
            // 从下到上：按列编号，每列从下到上
            seatNumber += (col - 1) * rows + (rows - row);
            break;

        case 'centerOutward': {
            // 中心扩展（中间左右）：中心位置编号最小，向两侧递增
            // 参照《座位编号方式对照表.md》
            //   奇数列(7列): 6-4-2-1-3-5-7
            //   偶数列(8列): 7-5-3-1-2-4-6-8
            let colSeatNum;

            if (cols % 2 === 1) {
                const centerCol = Math.ceil(cols / 2);
                if (col === centerCol) {
                    colSeatNum = 1;
                } else if (col < centerCol) {
                    const distance = centerCol - col;
                    colSeatNum = distance * 2;
                } else {
                    const distance = col - centerCol;
                    colSeatNum = distance * 2 + 1;
                }
            } else {
                const centerLeft = Math.floor(cols / 2);
                const centerRight = centerLeft + 1;
                if (col === centerLeft) {
                    colSeatNum = 1;
                } else if (col === centerRight) {
                    colSeatNum = 2;
                } else if (col < centerLeft) {
                    const distance = centerLeft - col;
                    colSeatNum = distance * 2 + 1;
                } else {
                    const distance = col - centerRight;
                    colSeatNum = distance * 2 + 2;
                }
            }

            seatNumber += (row - 1) * cols + colSeatNum - 1;
            break;
        }

        case 'alternatingLTRStart':
            // 左右右左：奇数行L→R，偶数行R→L
            if (row % 2 === 1) {
                seatNumber += (row - 1) * cols + (col - 1);
            } else {
                seatNumber += (row - 1) * cols + (cols - col);
            }
            break;

        case 'alternatingRTLStart':
            // 右左左右：奇数行R→L，偶数行L→R
            if (row % 2 === 1) {
                seatNumber += (row - 1) * cols + (cols - col);
            } else {
                seatNumber += (row - 1) * cols + (col - 1);
            }
            break;

        default:
            // 默认：按照seat.number（如果存在）
            seatNumber = seat.number || seatNumber;
    }

    // 确保编号为正整数
    seatNumber = Math.max(seatNumber, 1);

    return String(seatNumber);
};

/**
 * 获取编号模式的显示标签
 * 
 * @param {string} mode - 编号模式代码
 * @returns {string} 模式显示名称
 */
export const getNumberingModeLabel = (mode) => {
    if (!mode || !NUMBERING_MODES[mode]) {
        return '未知模式';
    }
    return NUMBERING_MODES[mode].name;
};

export default {
    NUMBERING_MODES,
    generateOptimizedSeatNumber,
    getNumberingModeLabel
};
