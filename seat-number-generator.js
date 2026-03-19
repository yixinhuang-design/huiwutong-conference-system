/**
 * 座位编号生成器 - 五种编号方式的完整实现
 * 可直接集成到 seating-mgr.html 中的 <script> 标签内
 * 
 * 使用示例：
 * const gen = new SeatNumberGenerator(5, 10, 'left_to_right');
 * const number = gen.generate(2, 5);  // 输出：15
 */

class SeatNumberGenerator {
  /**
   * 初始化座位编号生成器
   * @param {number} rows - 座位行数
   * @param {number} cols - 座位列数
   * @param {string} mode - 编号方式
   *   - 'center_outward': 中间左右
   *   - 'left_to_right': 由左至右
   *   - 'alternating_ltr_start': 左右右左
   *   - 'right_to_left': 由右至左
   *   - 'alternating_rtl_start': 右左左右
   */
  constructor(rows, cols, mode = 'left_to_right') {
    this.rows = rows;
    this.cols = cols;
    this.mode = mode;
  }

  /**
   * 根据行列号生成座位编号
   * @param {number} row - 行号（1-based）
   * @param {number} col - 列号（1-based）
   * @returns {number} 座位编号
   */
  generate(row, col) {
    // 验证行列号有效性
    if (row < 1 || row > this.rows || col < 1 || col > this.cols) {
      throw new Error(`Invalid seat position: row=${row}, col=${col}`);
    }

    switch (this.mode) {
      case 'center_outward':
        return this._centerOutward(row, col);
      case 'left_to_right':
        return this._leftToRight(row, col);
      case 'alternating_ltr_start':
        return this._alternatingLTRStart(row, col);
      case 'right_to_left':
        return this._rightToLeft(row, col);
      case 'alternating_rtl_start':
        return this._alternatingRTLStart(row, col);
      default:
        console.warn(`Unknown mode: ${this.mode}, using left_to_right`);
        return this._leftToRight(row, col);
    }
  }

  /**
   * 方式1：中间左右（Center-Outward）
   * 从中心向两侧扩散
   * 示例（7列）：6-4-2-1-3-5-7
   */
  _centerOutward(row, col) {
    const centerCol = Math.ceil(this.cols / 2);
    const distance = Math.abs(col - centerCol);
    const baseNum = (row - 1) * this.cols;

    if (col === centerCol) {
      // 中心位置
      return baseNum + centerCol;
    } else if (col < centerCol) {
      // 左侧：中心-距离×2+1
      return baseNum + (centerCol - distance * 2 + 1);
    } else {
      // 右侧：中心+距离×2-1
      return baseNum + (centerCol + distance * 2 - 1);
    }
  }

  /**
   * 方式2：由左至右（Left-to-Right）
   * 标准从左到右顺序编号
   * 示例：1-2-3-4-5-6-7, 8-9-10-...
   */
  _leftToRight(row, col) {
    return (row - 1) * this.cols + col;
  }

  /**
   * 方式3：左右右左（Alternating-LTR-Start）
   * 奇数行从左到右，偶数行从右到左
   * 示例：
   *   行1：1-2-3-4-5-6-7
   *   行2：14-13-12-11-10-9-8
   */
  _alternatingLTRStart(row, col) {
    const baseNum = (row - 1) * this.cols;
    if (row % 2 === 1) {
      // 奇数行：从左到右
      return baseNum + col;
    } else {
      // 偶数行：从右到左
      return baseNum + (this.cols - col + 1);
    }
  }

  /**
   * 方式4：由右至左（Right-to-Left）
   * 从右向左的标准编号
   * 示例：7-6-5-4-3-2-1, 14-13-12-11-...
   */
  _rightToLeft(row, col) {
    return (row - 1) * this.cols + (this.cols - col + 1);
  }

  /**
   * 方式5：右左左右（Alternating-RTL-Start）
   * 奇数行从右到左，偶数行从左到右
   * 示例：
   *   行1：7-6-5-4-3-2-1
   *   行2：8-9-10-11-12-13-14
   */
  _alternatingRTLStart(row, col) {
    const baseNum = (row - 1) * this.cols;
    if (row % 2 === 1) {
      // 奇数行：从右到左
      return baseNum + (this.cols - col + 1);
    } else {
      // 偶数行：从左到右
      return baseNum + col;
    }
  }

  /**
   * 批量生成所有座位的编号和信息
   * @returns {Array} 座位对象数组
   */
  generateAll() {
    const seats = [];
    for (let row = 1; row <= this.rows; row++) {
      for (let col = 1; col <= this.cols; col++) {
        const number = this.generate(row, col);
        seats.push({
          row,
          col,
          number,
          displayText: `${row}排${col}列`,
          displayNumber: `第${number}座`,
          id: `seat_${row}_${col}`
        });
      }
    }
    return seats;
  }

  /**
   * 获取编号模式的显示名称
   * @returns {string}
   */
  getModeName() {
    const names = {
      'center_outward': '中间左右',
      'left_to_right': '由左至右',
      'alternating_ltr_start': '左右右左',
      'right_to_left': '由右至左',
      'alternating_rtl_start': '右左左右'
    };
    return names[this.mode] || '未知模式';
  }

  /**
   * 改变编号模式
   * @param {string} newMode
   */
  setMode(newMode) {
    const validModes = [
      'center_outward',
      'left_to_right',
      'alternating_ltr_start',
      'right_to_left',
      'alternating_rtl_start'
    ];
    if (!validModes.includes(newMode)) {
      console.warn(`Invalid mode: ${newMode}`);
      return false;
    }
    this.mode = newMode;
    return true;
  }

  /**
   * 获取所有支持的编号模式列表
   * @returns {Array}
   */
  static getSupportedModes() {
    return [
      {
        key: 'center_outward',
        name: '中间左右',
        desc: '从中间向两侧扩散，适合VIP区'
      },
      {
        key: 'left_to_right',
        name: '由左至右',
        desc: '最标准的编号方式，适合普通区'
      },
      {
        key: 'alternating_ltr_start',
        name: '左右右左',
        desc: '蛇形编号，奇偶行交替，适合剧院'
      },
      {
        key: 'right_to_left',
        name: '由右至左',
        desc: '从右向左编号，适合右侧入口'
      },
      {
        key: 'alternating_rtl_start',
        name: '右左左右',
        desc: '反向蛇形，适合左侧入口'
      }
    ];
  }
}

/**
 * 座位编号转换器
 * 支持同一座位在不同编号方式间的转换
 */
class SeatNumberConverter {
  constructor(rows, cols) {
    this.rows = rows;
    this.cols = cols;
    this.generators = {};
    
    // 初始化所有编号方式的生成器
    const modes = [
      'center_outward',
      'left_to_right',
      'alternating_ltr_start',
      'right_to_left',
      'alternating_rtl_start'
    ];
    
    modes.forEach(mode => {
      this.generators[mode] = new SeatNumberGenerator(rows, cols, mode);
    });
  }

  /**
   * 根据某种方式的座位号，查找其他方式的号码
   * @param {string} fromMode - 原编号方式
   * @param {string} toMode - 目标编号方式
   * @param {number} seatNumber - 原座位号
   * @returns {number|null} 转换后的座位号，或null（未找到）
   */
  convert(fromMode, toMode, seatNumber) {
    if (fromMode === toMode) {
      return seatNumber;
    }

    // 1. 找到座位的行列位置
    let seat = null;
    for (let r = 1; r <= this.rows && !seat; r++) {
      for (let c = 1; c <= this.cols && !seat; c++) {
        if (this.generators[fromMode].generate(r, c) === seatNumber) {
          seat = { row: r, col: c };
        }
      }
    }

    if (!seat) {
      console.warn(`Seat number ${seatNumber} not found in ${fromMode} mode`);
      return null;
    }

    // 2. 用目标方式生成号码
    return this.generators[toMode].generate(seat.row, seat.col);
  }

  /**
   * 批量转换：生成两种方式之间的映射表
   * @param {string} fromMode - 原编号方式
   * @param {string} toMode - 目标编号方式
   * @returns {Object} 映射表 {原号码: 新号码}
   */
  convertAll(fromMode, toMode) {
    if (fromMode === toMode) {
      // 如果是同一方式，直接返回自映射
      const mapping = {};
      for (let i = 1; i <= this.rows * this.cols; i++) {
        mapping[i] = i;
      }
      return mapping;
    }

    const mapping = {};
    for (let r = 1; r <= this.rows; r++) {
      for (let c = 1; c <= this.cols; c++) {
        const fromNum = this.generators[fromMode].generate(r, c);
        const toNum = this.generators[toMode].generate(r, c);
        mapping[fromNum] = toNum;
      }
    }
    return mapping;
  }

  /**
   * 获取座位的行列位置（通过座位号）
   * @param {string} mode - 编号方式
   * @param {number} seatNumber - 座位号
   * @returns {Object|null} {row, col} 或 null
   */
  getSeatPosition(mode, seatNumber) {
    for (let r = 1; r <= this.rows; r++) {
      for (let c = 1; c <= this.cols; c++) {
        if (this.generators[mode].generate(r, c) === seatNumber) {
          return { row: r, col: c };
        }
      }
    }
    return null;
  }
}

/**
 * 座位区域配置助手
 * 集成座位编号生成到座位区域配置
 */
class SeatingAreaBuilder {
  constructor(areaId, areaName, rows, cols, venueType = 'regular') {
    this.areaId = areaId;
    this.areaName = areaName;
    this.rows = rows;
    this.cols = cols;
    this.venueType = venueType;
    this.numberingMode = 'left_to_right';
    this.aisles = [];
    this.style = {};
  }

  /**
   * 设置编号方式
   * @param {string} mode
   */
  setNumberingMode(mode) {
    this.numberingMode = mode;
    return this;
  }

  /**
   * 添加过道
   * @param {string} type - 'horizontal' 或 'vertical'
   * @param {number} position - 位置（0-1之间的比例）
   */
  addAisle(type, position) {
    this.aisles.push({ type, position });
    return this;
  }

  /**
   * 设置样式
   * @param {Object} styles - CSS相关的样式对象
   */
  setStyle(styles) {
    this.style = { ...styles };
    return this;
  }

  /**
   * 生成座位区域配置对象
   * @returns {Object}
   */
  build() {
    const generator = new SeatNumberGenerator(
      this.rows,
      this.cols,
      this.numberingMode
    );
    const seats = generator.generateAll();

    return {
      id: this.areaId,
      name: this.areaName,
      type: this.venueType,
      rows: this.rows,
      cols: this.cols,
      numberingMode: this.numberingMode,
      numberingModeName: generator.getModeName(),
      aisles: this.aisles,
      style: this.style,
      seats: seats,
      totalSeats: seats.length,
      
      // 便捷方法
      getSeat: (row, col) => {
        return seats.find(s => s.row === row && s.col === col);
      },
      
      getSeatByNumber: (number) => {
        return seats.find(s => s.number === number);
      },
      
      getRowSeats: (row) => {
        return seats.filter(s => s.row === row);
      },
      
      getColSeats: (col) => {
        return seats.filter(s => s.col === col);
      }
    };
  }
}

// ============ 使用示例 ============

/**
 * 示例1：生成单个座位号
 */
function example1_generateSingleNumber() {
  const gen = new SeatNumberGenerator(5, 10, 'left_to_right');
  const number = gen.generate(2, 5);
  console.log(`第2行第5列的座位号: ${number}`); // 输出: 15
}

/**
 * 示例2：批量生成座位配置
 */
function example2_generateSeatingArea() {
  const gen = new SeatNumberGenerator(3, 7, 'alternating_ltr_start');
  const seats = gen.generateAll();
  
  console.table(seats.slice(0, 14)); // 显示前两行
  // 输出表格：
  // row | col | number | displayText | displayNumber
  //  1  |  1  |   1    | 1排1列      | 第1座
  //  1  |  2  |   2    | 1排2列      | 第2座
  //  ...
  //  2  |  1  |  14    | 2排1列      | 第14座
}

/**
 * 示例3：编号方式转换
 */
function example3_convertSeatingModes() {
  const converter = new SeatNumberConverter(5, 10);
  
  // 某人在"由左至右"中座位号是42，在"左右右左"中是多少？
  const newNumber = converter.convert(
    'left_to_right',
    'alternating_ltr_start',
    42
  );
  console.log(`座位号42转换后: ${newNumber}`);
  
  // 获取完整的方式转换映射
  const mapping = converter.convertAll('left_to_right', 'alternating_ltr_start');
  console.log('转换映射:', mapping);
}

/**
 * 示例4：使用生成器构建座位区域
 */
function example4_buildSeatingArea() {
  const area = new SeatingAreaBuilder('area_vip', 'VIP区', 5, 10, 'vip')
    .setNumberingMode('center_outward')
    .addAisle('horizontal', 0.5)
    .setStyle({
      color: '#667eea',
      textColor: '#ffffff',
      borderColor: '#5568d3'
    })
    .build();
  
  console.log('座位区域配置:', area);
  console.log('VIP区总座位数:', area.totalSeats);
  console.log('第1行座位:', area.getRowSeats(1));
}

/**
 * 示例5：与Vue配合使用
 */
function example5_vueIntegration() {
  // 在 seating-mgr.html 的 Vue 组件中使用：
  
  const setupSeatingArea = () => {
    // 创建座位生成器
    const generator = new SeatNumberGenerator(5, 10, 'left_to_right');
    
    // 返回给 Vue 数据
    return {
      seats: generator.generateAll(),
      numberingMode: generator.mode,
      numberingModeName: generator.getModeName(),
      
      // 支持切换编号方式
      changeNumberingMode: (newMode) => {
        if (generator.setMode(newMode)) {
          return generator.generateAll();
        }
        return null;
      }
    };
  };
  
  // 在 setup() 中使用
  // const seatingData = setupSeatingArea();
}
