<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# typilot 更新日志

所有显著的变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/)，并遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [未发布]

## [0.3.2] - 2026-07-08

### 新增

- 高亮链接文本和链接函数
- 高亮函数标识符

### 移除

- 清理 `Kvasir` 的配色

## [0.3.1] - 2026-07-04

### 新增

- **TypedHandler** — 自动补全配对符号（`*`、`_`、`$`、`` ` ``、`(`、`[`、`{`、`<`）及包裹选中文本
- **连续反引号补全** — 在已配对的反引号之间再次输入 `` ` `` 时，继续补全而非跳过，以支持三重反引号

## [0.3.0] - 2026-07-03

### 新增

- **拼写检查** — 初步实现拼写检查功能
- **Surround With** — 实现选中文本包裹功能（`*`、`_`、`$`、`` ` ``），并优化体验

### 更改

- **PSI 元素类型** — 改进元素类型，为拼写检查提供更准确的支持
- **构建流程** — `endiveCompile` 后自动执行 `spotlessJavaApply`，声明任务依赖关系，优化任务流程（

### 修复

- **语言注入** — 修复语言注入区域对拼写检查的误报
- **构建任务** — 修复构建失败问题

### 更新

- 更新 `serde-generate` 和 `serde-reflection`
- 更新 Gradle 版本
- 重新生成 Wasm 文件

## [0.2.0] - 2026-07-02

### 新增

- **结构视图** — 添加结构视图（Structure View）和代码块支持

### 更改

- **`typalize` 调整** — 调整 malloc/free，移除 Java 端 wasmFree 避免双重释放

### 新增

- 修复无法自动启动问题，启动时自动为打开的 .typ 文件启动服务端
- 修复源文件修改时预览窗口不自动加载的问题
- 加载失败时自动重试加载预览窗口
- 重启时重置预览服务的地址
- 修复无法构建的问题

## [0.0.1] - 2026-05-17

### 新增

- 定义 Typst 语言和文件类型，添加插件图标
- **注释支持** — 为 Typst 提供 Commenter
- **语法解析** — 实现 Lexer 与 Parser（使用 WASM 封装 `typst-syntax` 实现），处理 UTF-16 偏移量、错误节点、空白与注释节点、Ident 节点等
- **序列化方案** — 使用 `serde-reflection` 将 Endive（WASM Runtime) 将字节流反序列化成 ASTNode 列表
- **PSI 树构建** — 借助 PSI 的 `PsiBuilder.Marker` 方式将 Parser 中返回的 ASTNode 列表重建成 PSI 树
- **语言服务器** — 基于 LSP4IJ + Tinymist 实现 Typst 语言服务器，支持自动启动、下载管理、配置项等功能
- **分屏预览** — 提供实时预览窗口，支持 PDF 导出（Tinymist 实现）
- **语法高亮** — 实现 Annotator 语法高亮，迁移 `Kvasir` 插件配色方案
- **语言注入** — 实现语言注入功能，支持嵌入代码区域识别
