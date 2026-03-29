# Interval Pacer

> 专注运动时机控制的极简计时器 App

Interval Pacer 是一款为跑者和健身爱好者设计的计时工具，专注于"提醒"而非"记录"。通过语音和震动提示，让你在运动时无需看手机，专注训练本身。

## 功能概览

### 走跑间歇训练
- 自定义跑步/步行时长，循环滚轮精确到秒
- 快捷预设（30s / 45s / 1:00 / 1:30 / 2:00 / 5:00）
- 语音播报阶段转换（"跑步"、"步行"）
- 震动提醒（户外嘈杂时的补充）
- 大字体倒计时 + 整体进度条
- 暂停 / 恢复 / 跳过阶段 / 停止训练
- 前台服务保证锁屏时持续运行

### 健身组数计时
- 自定义动作名称、目标组数、组间休息时间
- 组间休息循环滚轮选择器（30s / 45s / 1:00 / 1:30 / 2:00 / 3:00）
- 手动确认完成每组 → 自动开始组间倒计时
- 语音播报组完成与休息倒计时

### 训练历史
- 自动保存每次训练记录到本地数据库
- 按日期浏览历史，查看训练详情（类型、时长、参数）

### 设置
- TTS 语音引擎选择、语速/音调调节
- 震动开关
- 配置记忆（下次打开保留上次设置）

## 技术栈

| 层面 | 技术选型 |
|------|---------|
| 语言 | Kotlin |
| UI | Jetpack Compose (Material 3) |
| 架构 | MVVM + Clean Architecture |
| 异步 | Kotlin Coroutines + Flow |
| 本地存储 | Room Database + SharedPreferences |
| 后台服务 | Foreground Service + WorkManager |
| 语音 | Android TTS (Text-to-Speech) |
| 最低版本 | Android 8.0 (API 26) |

## 项目结构

```
app/src/main/java/com/github/intervalpacer/
├── core/              # 核心基础设施
│   ├── tts/           # TTS 语音管理
│   ├── audio/         # 音频焦点管理
│   ├── vibration/     # 震动管理
│   └── notification/  # 通知控制器
├── data/              # 数据层
│   ├── local/         # Room 数据库、SharedPreferences
│   └── repository/    # Repository 实现
├── domain/            # 领域层
│   ├── model/         # 领域模型
│   ├── repository/    # Repository 接口
│   └── service/       # 业务服务（Timer、PhaseManager）
├── presentation/      # 表现层
│   ├── home/          # 首页
│   ├── interval/      # 间歇训练
│   ├── settraining/   # 健身计时
│   ├── history/       # 历史记录
│   ├── settings/      # 设置
│   ├── navigation/    # 导航
│   └── ui/            # 主题、组件、Activity
└── service/           # 前台服务
```

## 快速开始

### 环境要求
- Android Studio 2023.1.1 或更高
- JDK 17
- Android SDK API 26+

### 构建项目
```bash
git clone https://github.com/shinyruo/IntervalPacer.git
cd IntervalPacer
./gradlew assembleDebug
```

### 运行
```bash
./gradlew installDebug
adb shell am start -n com.github.intervalpacer/.MainActivity
```

## 路线图

### v1.0 — 当前版本
- [x] 走跑间歇训练（语音 + 震动 + 前台服务）
- [x] 健身组数计时（手动确认 + 自动休息倒计时）
- [x] 训练历史记录（本地数据库）
- [x] 循环滚轮时间选择器（预设 + 自由调节）
- [x] TTS 设置（引擎、语速、音调）
- [x] 配置记忆
- [x] Material 3 设计语言

### 计划中
- [ ] 手套模式（音量键控制）
- [ ] 音乐集成（训练时自动暂停/恢复音乐）
- [ ] 可穿戴设备支持（Wear OS）
- [ ] 训练统计与趋势图表

## 设计理念

基于 **Don Norman** 的人本设计原则和 **Alan Cooper** 的目标导向设计：

- **语音优先**：运动时不想看手机，用耳朵了解进度
- **极简交互**：快速启动，零思考操作
- **容错设计**：运动中误触是常态，恢复路径简单
- **认知减负**：大字体、高对比度、状态一目了然

详细设计文档请查看 [docs/](docs/) 目录。

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件
