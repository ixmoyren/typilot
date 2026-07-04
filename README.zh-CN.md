# typilot

![Build](https://github.com/ixmoyren/typilot/workflows/Build/badge.svg)

基于 `typst-syntax`、`endive`、`tinymist` 和 `lsp4ij` 构建的 [Typst](https://typst.org/) 语言 IntelliJ IDEA 插件。

## 功能特性

- **语法高亮** — 将 `typst-syntax` 编译为 WASM，再经由 Endive 转换为 Java 类，提供原生级语法高亮
- **PSI 树结构** — 完整的 IntelliJ PSI 树（词法分析器、解析器、元素类型），基于官方 Typst 解析器
- **LSP 集成** — 通过 `tinymist` 语言服务端提供补全、诊断、悬停提示、跳转定义、Code Lens、代码折叠、签名帮助等 LSP 功能
- **实时预览** — 基于 JCEF 浏览器的即时渲染预览，由 tinymist 的预览协议驱动
- **导出 PDF** — 通过 tinymist 将 Typst 文档导出为 PDF
- **语言注入** — 对代码块内嵌语言自动注入 IDE 支持
- **LSP 发现** — 支持自动下载、系统 PATH 查找、手动配置三种方式定位 tinymist

## 架构

```
┌───────────────────────────────────────────────────────┐
│                 IntelliJ IDEA Plugin                  │
│  ┌──────────────┐ ┌───────────┐ ┌──────────────────┐  │
│  │ PSI/词法分析  │ │ 预览       │ │ LSP（via LSP4IJ）│  │
│  │ (WASM/JVM)   │ │ (JCEF)    │ │ (tinymist 进程)  │  │
│  └──────┬───────┘ └───────────┘ └─────────┬─────────┘ │
└─────────┼─────────────────────────────────┼───────────┘
          │                                 │
          ▼                                 ▼
   ┌───────────────┐               ┌──────────────────┐
   │ typalize.wasm │               │  tinymist LSP    │
   │ (typst-syntax)│               │  (外部进程)       │
   └───────────────┘               └──────────────────┘
```

### 分层说明

1. **Rust — `crates/typalize/`** — 封装 `typst-syntax`（v0.15.0）为 `cdylib` 格式，目标平台 `wasm32-wasip1`。通过 C ABI 导出 `tokenize`、`parse`、`version` 函数，数据序列化采用 BCS（Binary Canonical Serialization）格式以保证高效传输。

2. **WASM → Java 桥接** — 使用 [Endive](https://endive.run/)（基于 endive 的 WASM 转 Java 工具链）将 WASM 二进制文件编译为 Java 字节码。 `Core` 接口提供 `tokenize()`、`parse()`、`version()` 方法，可直接在 JVM 语言中调用。

3. **插件** — 调用封装的 Java API 实现词法分析（`TypstLexer`）和语法解析（`TypstParser`），构建完整的 PSI 树。LSP 功能通过 LSP4IJ 委托给外部的 `tinymist` 进程处理。预览功能通过 JCEF 连接 tinymist 的预览端点。

## 环境要求

- **JDK 25**（IntelliJ IDEA 2026.1+ 需要）
- **Rust nightly**（需包含 `wasm32-wasip1` 目标）

## 快速开始

```bash
# 1. 构建 typst-syntax
cargo xtask generate

# 2. 构建并运行插件
./gradlew runIde
```

## 构建任务

将 `typst-syntax`（Rust）编译为 JVM 字节码。

### 分步说明

| 步骤               | 命令                                     | 说明                                                                         |
|------------------|----------------------------------------|----------------------------------------------------------------------------|
| 1. 安装 WASM 工具链   | `cargo xtask get-wasm-tool <type>`     | 下载 WASI SDK、wasmtime 或 binaryen 至 `.tools/` 目录                             |
| 2. 生成序列化代码       | `cargo xtask generate-reflection-code` | 运行 `gen_reflection` 二进制文件生成 serde-reflection YAML → 生成 Java BCS 序列化代码      |
| 3. 编译 WASM       | `cargo xtask build-wasm`               | 使用 WASI SDK 将 `typalize` crate 交叉编译为 `wasm32-wasip1`                       |
| 4. 复制 WASM       | `cargo xtask copy-wasm`                | 将 `.wasm` 复制至 `src/main/resources/wasm/`                                   |
| 5. 优化 WASM       | `cargo xtask optimize-wasm`            | 通过 binaryen 的 `wasm-opt -Oz --strip-debug` 优化                              |
| 6. 生成 Java class | `cargo xtask generate-java-class`      | 运行 Endive 编译器（`./gradlew endiveCompile`）+ 代码格式化（`./gradlew spotlessApply`） |

### 一键构建

```bash
cargo xtask generate
```

此命令依次执行上述全部 6 个步骤。

### 构建流程详解

```
                     ┌──────────────────────────┐
                     │  typst-syntax (Rust)     │
                     │  crate: typalize         │
                     │  (cdylib, rlib)          │
                     └───────────┬──────────────┘
                                 │ cargo build --target wasm32-wasip1
                                 ▼
                     ┌──────────────────────────┐
                     │   typalize.wasm          │
                     │   (wasm32-wasip1)        │
                     └───────────┬──────────────┘
                                 │ wasm-opt (binaryen)
                                 ▼
                     ┌──────────────────────────┐
                     │  typalize.wasm           │
                     │  (已优化)                 │
                     └───────────┬──────────────┘
                                 │ Endive 编译器
                                 ▼
              ┌───────────────────────────────────┐
              │   - 生成代码（Endive Wasm 和 BCS）  │
              │   - 封装成通用 API                  │
              │     - wasm 环境初始化               │
              │     - 加载 wasm 模块                │
              │     - 传参序列化                    │
              │     - 调用函数                     │
              │     - 结果反序列化                  │
              └──────────────────┬────────────────┘
                                 │ compile
                                 ▼
              ┌──────────────────────────────────┐
              │   IntelliJ IDEA Plugin           │
              │   - TypstLexer → tokenize()      │
              │   - TypstParser → parse()        │
              │   - LSP4IJ → tinymist            │
              └──────────────────────────────────┘
```

## 开发

```bash
# 在开发 IDE 实例中运行插件
./gradlew runIde

# 运行测试
./gradlew test

# 运行插件验证
./gradlew verifyPlugin

# 构建插件分发包
./gradlew buildPlugin
```

### WASM 工具链资源

| 工具                                                       | 资源类型       | 版本          |
|----------------------------------------------------------|------------|-------------|
| [WASI SDK](https://github.com/WebAssembly/wasi-sdk)      | `WasiSdk`  | 33.0        |
| [Wasmtime](https://github.com/bytecodealliance/wasmtime) | `Wasmtime` | v46.0.1     |
| [Binaryen](https://github.com/WebAssembly/binaryen)      | `Binaryen` | version_130 |

可通过环境变量 `WASI_SDK_URL`、`WASMTIME_URL`、`BINARYEN_URL` 覆盖下载地址。

## 项目结构

```
.
├── crates/
│   ├── typalize/          # Rust WASM crate（typst-syntax 封装）
│   │   ├── src/
│   │   │   ├── lib.rs         # C ABI 导出
│   │   │   ├── envelope.rs    # 可序列化类型（Token, ASTNode, …）
│   │   │   ├── syntax.rs      # AST 遍历（Flatten, ASTBuilder）
│   │   │   └── util.rs        # UTF-16 映射、内存管理
│   │   └── Cargo.toml
│   └── enum-mirror/       # 过程宏 crate（枚举镜像）
├── xtask/                 # 构建自动化（cargo xtask）
│   ├── main.rs            # CLI 分发
│   ├── task.rs            # 构建步骤实现
│   ├── util.rs            # 公共工具函数（下载、解压等）
│   └── Cargo.toml
├── buildSrc/              # 自定义 Gradle 构建逻辑
│   └── src/main/kotlin/
│       └── EndiveCompilerTask.kt   # Endive Gradle 任务
├── src/
│   ├── main/
│   │   ├── kotlin/…/typilot/   # 插件源代码
│   │   ├── java/               # Java 代码
│   │   └── resources/          # 插件资源文件
│   └── test/                   # 测试代码
├── build.gradle.kts       # Gradle 构建脚本
├── Cargo.toml             # Rust 工作区根目录
├── rust-toolchain.toml    # Rust 工具链配置
└── gradle.properties      # 插件元数据与版本号
```

## 许可证

[MIT](LICENSE)
