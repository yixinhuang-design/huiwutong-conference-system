# Font Awesome图标正确用法修复指南

## 问题说明
在Vue的data数据中不能使用HTML标签字符串，否则会直接显示代码文本在页面上。

## 错误用法 ❌
```javascript
data() {
  return {
    icon: '<text class="fa fa-check"></text>'  // ❌ 错误：会显示代码
  }
}
```

```html
<text>{{ icon }}</text>  <!-- 会显示：'<text class="fa fa-check"></text>' -->
```

## 正确用法 ✅

### 方法1：data中存类名，模板中绑定class
```javascript
data() {
  return {
    icon: 'fa-check'  // ✅ 正确：只存类名
  }
}
```

```html
<text :class="icon"></text>  <!-- 正确渲染图标 -->
```

### 方法2：直接在模板中使用Font Awesome
```html
<text class="fa fa-check"></text>  <!-- 直接使用 -->
```

### 方法3：使用动态类名
```javascript
data() {
  return {
    iconName: 'fa-check'  // 只存类名
  }
}
```

```html
<text :class="iconName"></text>  <!-- 动态绑定 -->
```

## 需要修复的文件
检查所有在data中使用 `'<text class="fa ...'` 的地方，改为只存储类名。

### 批量修复脚本
```bash
cd "G:\huiwutong新版合集\huiwutong-uniapp\pages"

# 查找所有问题文件
grep -rn "icon: '<text class=\"fa" . --include="*.vue"

# 修复策略
# 将 icon: '<text class="fa fa-check"></text>'
# 改为 icon: 'fa-check'
# 然后在模板中使用 <text :class="item.icon"></text>
```

## 常见图标类名参考
```javascript
{
  'fa-check': '勾号',
  'fa-clock': '时钟',
  'fa-lock': '锁',
  'fa-mobile-alt': '手机',
  'fa-eye': '眼睛',
  'fa-eye-slash': '隐藏',
  'fa-bullhorn': '喇叭',
  'fa-chart-bar': '图表',
  'fa-home': '房子',
  'fa-user': '用户',
  'fa-edit': '编辑',
  'fa-cog': '设置',
  'fa-bell': '铃铛',
  'fa-search': '搜索',
  'fa-plus': '加号',
  'fa-times': '叉号',
  'fa-star': '实心星',
  'fa-star-o': '空心星',
  'fa-heart': '实心',
  'fa-heart-o': '空心',
  'fa-camera': '相机',
  'fa-microphone': '麦克风'
}
```
