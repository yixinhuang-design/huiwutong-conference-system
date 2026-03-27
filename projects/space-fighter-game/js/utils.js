// 工具函数集合

// 随机数生成
export function random(min, max) {
    return Math.random() * (max - min) + min;
}

// 随机整数
export function randomInt(min, max) {
    return Math.floor(random(min, max + 1));
}

// 随机颜色
export function randomColor() {
    const colors = ['#0ff', '#f0f', '#ff0', '#0f0', '#f00'];
    return colors[randomInt(0, colors.length - 1)];
}

// 距离计算
export function distance(x1, y1, x2, y2) {
    return Math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2);
}

// 限制值在范围内
export function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
}

// 线性插值
export function lerp(a, b, t) {
    return a + (b - a) * t;
}

// 角度转弧度
export function toRadians(degrees) {
    return degrees * Math.PI / 180;
}

// 弧度转角度
export function toDegrees(radians) {
    return radians * 180 / Math.PI;
}
