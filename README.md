# typilot

![Build](https://github.com/ixmoyren/typilot/workflows/Build/badge.svg)

An IntelliJ IDEA plugin for the [Typst](https://typst.org/) language, built on `typst-syntax`, `tinymist`, `endive`, and `lsp4ij`.

## Features

- **Syntax Highlighting** — Compiles `typst-syntax` to WASM, then converts to Java classes via Endive for native-level syntax highlighting
- **PSI Tree** — Full IntelliJ PSI tree (lexer, parser, element types) backed by the official Typst parser
- **LSP Integration** — Language intelligence via the `tinymist` language server: completions, diagnostics, hover, go-to-definition, code lens, folding, signature help, and more
- **Live Preview** — Real-time rendering via JCEF browser, powered by tinymist's preview protocol
- **Export to PDF** — Export Typst documents to PDF via tinymist
- **Language Injection** — Automatic IDE support injection for embedded languages in code blocks
- **LSP Discovery** — Supports three strategies to locate tinymist: auto-download, system PATH lookup, and manual configuration

## Architecture

```
┌────────────────────────────────────────────────────────┐
│                 IntelliJ IDEA Plugin                   │
│  ┌──────────────┐ ┌───────────┐ ┌───────────────────┐  │
│  │ PSI / Lexing │ │ Preview   │ │ LSP (via LSP4IJ)  │  │
│  │ (WASM/JVM)   │ │ (JCEF)    │ │ (tinymist proc)   │  │
│  └──────┬───────┘ └───────────┘ └─────────┬─────────┘  │
└─────────┼─────────────────────────────────┼────────────┘
          │                                 │
          ▼                                 ▼
   ┌───────────────┐               ┌──────────────────┐
   │ typalize.wasm │               │  tinymist LSP    │
   │ (typst-syntax)│               │  (external proc) │
   └───────────────┘               └──────────────────┘
```

### Layers

1. **Rust — `crates/typalize/`** — Wraps `typst-syntax` (v0.15.0) as a `cdylib` targeting `wasm32-wasip1`. Exposes C ABI functions (`tokenize`, `parse`, `version`) using BCS (Binary Canonical Serialization) for efficient data transfer.

2. **WASM → Java Bridge** — Uses [Endive](https://endive.run/) (an endive-based WASM-to-Java toolchain) to compile the WASM binary to Java bytecode. The `Core` interface provides `tokenize()`, `parse()`, and `version()` methods directly callable from JVM languages.

3. **Plugin** — Consumes the wrapped Java API for lexing (`TypstLexer`) and parsing (`TypstParser`), building a full PSI tree. LSP features are handled by LSP4IJ delegating to an external `tinymist` process. Preview uses JCEF to connect to tinymist's preview endpoint.

## Prerequisites

- **JDK 25** (required by IntelliJ IDEA 2026.1+)
- **Rust nightly** (with `wasm32-wasip1` target)

## Quick Start

```bash
# 1. Build typst-syntax
cargo xtask generate

# 2. Build and run the plugin
./gradlew runIde
```

## Build Tasks

Compiles `typst-syntax` (Rust) into JVM bytecode.

### Step-by-step

| Step                           | Command                                | Description                                                                                         |
|--------------------------------|----------------------------------------|-----------------------------------------------------------------------------------------------------|
| 1. Install WASM tools          | `cargo xtask get-wasm-tool <type>`     | Download WASI SDK, wasmtime, or binaryen to `.tools/`                                               |
| 2. Generate serialization code | `cargo xtask generate-reflection-code` | Run `gen_reflection` binary to produce serde-reflection YAML → generate Java BCS serialization code |
| 3. Build WASM                  | `cargo xtask build-wasm`               | Cross-compile `typalize` crate to `wasm32-wasip1` using WASI SDK                                    |
| 4. Copy WASM                   | `cargo xtask copy-wasm`                | Copy `.wasm` to `src/main/resources/wasm/`                                                          |
| 5. Optimize WASM               | `cargo xtask optimize-wasm`            | Run `wasm-opt -Oz --strip-debug` via binaryen                                                       |
| 6. Generate Java class         | `cargo xtask generate-java-class`      | Run Endive compiler (`./gradlew endiveCompile`) + code formatter (`./gradlew spotlessApply`)        |

### Single Command

```bash
cargo xtask generate
```

This runs all 6 steps above sequentially.

### Build Flow

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
                     │  (optimized)             │
                     └───────────┬──────────────┘
                                 │ Endive compiler
                                 ▼
              ┌───────────────────────────────────┐
              │   - Generates code (Endive WASM   │
              │     and BCS)                      │
              │   - Wraps into a general API      │
              │     - WASM environment init       │
              │     - Load WASM module            │
              │     - Serialize parameters        │
              │     - Call functions              │
              │     - Deserialize results         │
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

## Development

```bash
# Run the plugin in a development IDE instance
./gradlew runIde

# Run tests
./gradlew test

# Run verifications
./gradlew verifyPlugin

# Build the plugin distribution
./gradlew buildPlugin
```

### WASM Toolchain Resources

| Tool                                                     | Resource Type | Version     |
|----------------------------------------------------------|---------------|-------------|
| [WASI SDK](https://github.com/WebAssembly/wasi-sdk)      | `WasiSdk`     | 33.0        |
| [Wasmtime](https://github.com/bytecodealliance/wasmtime) | `Wasmtime`    | v46.0.1     |
| [Binaryen](https://github.com/WebAssembly/binaryen)      | `Binaryen`    | version_130 |

Download URLs can be overridden via environment variables: `WASI_SDK_URL`, `WASMTIME_URL`, `BINARYEN_URL`.

## Project Structure

```
.
├── crates/
│   ├── typalize/          # Rust WASM crate (typst-syntax wrapper)
│   │   ├── src/
│   │   │   ├── lib.rs         # C ABI exports
│   │   │   ├── envelope.rs    # Serializable types (Token, ASTNode, …)
│   │   │   ├── syntax.rs      # AST traversal (Flatten, ASTBuilder)
│   │   │   └── util.rs        # UTF-16 mapping, memory management
│   │   └── Cargo.toml
│   └── enum-mirror/       # Proc-macro crate (enum mirroring)
├── xtask/                 # Build automation (cargo xtask)
│   ├── main.rs            # CLI dispatch
│   ├── task.rs            # Build step implementations
│   ├── util.rs            # Shared utilities (download, extraction, etc.)
│   └── Cargo.toml
├── buildSrc/              # Custom Gradle build logic
│   └── src/main/kotlin/
│       └── EndiveCompilerTask.kt   # Endive Gradle task
├── src/
│   ├── main/
│   │   ├── kotlin/…/typilot/   # Plugin source code
│   │   ├── java/               # Java code
│   │   └── resources/          # Plugin resources
│   └── test/                   # Tests
├── build.gradle.kts       # Gradle build script
├── Cargo.toml             # Rust workspace root
├── rust-toolchain.toml    # Rust toolchain configuration
└── gradle.properties      # Plugin metadata & versions
```

## License

[MIT](LICENSE)
