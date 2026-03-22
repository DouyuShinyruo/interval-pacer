# Interval Pacer

> 专注运动时机控制的极简计时器 App

Interval Pacer 是一款为跑者和健身爱好者设计的计时工具，专注于"提醒"而非"记录"。通过语音和震动提示，让你在运动时无需看手机，专注训练本身。

## ✨ 核心功能

### 🏃 走跑间歇训练
- 自定义跑步/步行时长
- 先跑或先走的顺序选择
- 语音播报阶段转换（"跑步"、"步行"）
- 震动提醒（户外嘈杂时的补充）
- 大字体倒计时显示
- 暂停/恢复/跳过阶段

### 💪 力量训练计时（计划中）
- 组数设置
- 组间休息倒计时
- 组完成语音提示

## 🛠️ 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **异步**: Kotlin Coroutines + Flow
- **语音**: Android TTS (Text-to-Speech)
- **最低版本**: Android 7.0 (API 24)

## 🚧 开发状态

**当前版本**: MVP (Minimum Viable Product)

### ✅ 已完成
- 走跑间歇训练核心功能
- 语音播报 + 震动提醒
- 训练参数配置（时长、组数、顺序）
- 训练中控制（暂停/恢复/跳过/停止）
- 完成总结页面

### 🔄 开发中
- 历史记录保存
- 训练统计页面
- 更多语音选项

### 📋 计划中
- 力量训练模式
- 音乐集成
- 手套模式（音量键控制）
- 可穿戴设备支持

## 🏃 快速开始

### 环境要求
- Android Studio Hedgehog | 2023.1.1 或更高
- JDK 17
- Android SDK API 24+

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

## 📸 截图

> 截图将在 v1.0 发布时添加

## 🎯 设计理念

基于 **Don Norman** 的人本设计原则和 **Alan Cooper** 的目标导向设计：

- **语音优先**：运动时不想看手机，用耳朵了解进度
- **极简交互**：快速启动，零思考操作
- **容错设计**：运动中误触是常态，恢复路径简单
- **认知减负**：大字体、高对比度、状态一目了然

详细设计文档请查看 [docs/](docs/) 目录。

## 🤝 贡献

目前项目处于 MVP 阶段，专注于核心功能开发。欢迎提交 Issue 报告 Bug 或提出建议。

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 📮 联系方式

- 项目主页: [GitHub](https://github.com/shinyruo/IntervalPacer)
- 问题反馈: [Issues](https://github.com/shinyruo/IntervalPacer/issues)

---

**Note**: 本项目正在积极开发中，API 和功能可能会有变动。
