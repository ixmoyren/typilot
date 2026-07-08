<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# typilot Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.2] - 2026-07-08

### Added

- Highlight link text and link functions
- Highlight function identifiers

### Removed

- Clean up `Kvasir` color scheme

## [0.3.1] - 2026-07-04

### Added

- **TypedHandler** — auto-pairing of matching symbols (`*`, `_`, `$`, `` ` ``, `(`, `[`, `{`, `<`) and surround selected text
- **Consecutive backtick completion** — when typing `` ` `` between already-paired backticks, complete another pair instead of skipping, enabling triple backticks

## [0.3.0] - 2026-07-03

### Added

- **Spell checking** — initial implementation of spell checking
- **Surround With** — implement surround-with functionality for selected text (`*`, `_`, `$`, `` ` ``), with improved experience

### Changed

- **PSI element types** — improved element types for more accurate spell checking support
- **Build process** — automatically execute `spotlessJavaApply` after `endiveCompile`, declare task dependencies, optimize task flow

### Fixed

- **Language injection** — fixed false positives of spell checking in injected regions
- **Build task** — fixed build failures

### Updated

- Updated `serde-generate` and `serde-reflection`
- Updated Gradle version
- Regenerated Wasm files

## [0.2.0] - 2026-07-02

### Added

- **Structure view** — added Structure View and code block support

### Changed

- **`typalize` adjustments** — adjusted malloc/free, removed wasmFree on the Java side to avoid double-free

### Fixed

- Fixed the issue where the language server would not start automatically; now automatically starts the server for open .typ files on startup
- Fixed the issue where the preview window would not automatically load when source files were modified
- Automatically retry loading the preview window on failure
- Reset the preview service address on restart
- Fixed build issues

## [0.0.1] - 2026-05-17

### Added

- Defined Typst language and file type, added plugin icon
- **Commenter** — provided Commenter support for Typst
- **Parsing** — implemented Lexer and Parser (using WASM-wrapped `typst-syntax`), handling UTF-16 offsets, error nodes, whitespace and comment nodes, Ident nodes, etc.
- **Serialization** — using `serde-reflection` with Endive (WASM Runtime) to deserialize byte streams into ASTNode lists
- **PSI tree construction** — rebuilding the ASTNode list returned by the Parser into a PSI tree using `PsiBuilder.Marker`
- **Language server** — Typst language server based on LSP4IJ + Tinymist, supporting auto-startup, download management, configuration, etc.
- **Split-screen preview** — provided real-time preview window with PDF export (powered by Tinymist)
- **Syntax highlighting** — implemented Annotator-based syntax highlighting, migrated color schemes from the `Kvasir` plugin
- **Language injection** — implemented language injection with embedded code region detection
