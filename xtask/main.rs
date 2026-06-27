mod task;
mod util;

use clap::{Parser, Subcommand, ValueEnum};
use snafu::{ErrorCompat, WhateverLocal};
use std::{borrow::Cow, path::PathBuf};

type Result<T, E = WhateverLocal> = std::result::Result<T, E>;

#[derive(Parser, Debug)]
#[command(version, about, long_about = None)]
struct Cli {
    #[command(subcommand)]
    action: Action,
}

#[derive(Subcommand, Debug)]
enum Action {
    #[command(about = "Obtain the toolchain of Wasm")]
    GetWasmTool {
        #[arg(value_enum)]
        resource_type: ResourceType,
        #[arg(short, long, value_name = ".tools")]
        install: Option<PathBuf>,
    },
    #[command(about = "Generate serialization code related by serde-reflection")]
    GenerateReflectionCode,
    #[command(about = "Build the typalize crate to wasm")]
    BuildWasm {
        #[arg(short, long, value_name = ".tools")]
        tool: Option<PathBuf>,
    },
    #[command(about = "Copy the built wasm dylib to the resource directory.")]
    CopyWasm,
    #[command(about = "Optimize wasm")]
    OptimizeWasm {
        #[arg(short, long, value_name = ".tools")]
        tool: Option<PathBuf>,
    },
    #[command(about = "Convert wasm to Java class")]
    GenerateJavaClass,
    #[command(
        about = "Complete the entire process of generating the code, GenerateReflectionCode -> BuildWasm -> CopyWasm -> OptimizeWasm -> GenerateJavaClass"
    )]
    Generate {
        #[arg(short, long, value_name = ".tools")]
        tool: Option<PathBuf>,
    },
}

#[derive(Copy, Clone, Debug, PartialEq, Default, ValueEnum)]
enum ResourceType {
    #[default]
    WasiSdk,
    Wasmtime,
    Binaryen,
}

impl ResourceType {
    pub fn tool_name(&self) -> &'static str {
        match self {
            Self::WasiSdk => {
                cfg_select! {
                    target_os = "windows" => {
                        "wasi-sdk-33.0-x86_64-windows"
                    }
                    target_os = "macos" => {
                        "wasi-sdk-33.0-arm64-macos"
                    }
                    _ => {
                        "wasi-sdk-33.0-x86_64-linux"
                    }
                }
            }
            Self::Wasmtime => {
                cfg_select! {
                        target_os = "windows" => {
                            "wasmtime-v46.0.1-x86_64-windows"
                        }
                        target_os = "macos" => {
                            "wasmtime-v46.0.1-aarch64-macos"
                        }
                        _ => {
                            "wasmtime-v46.0.1-x86_64-linux"
                        }

                }
            }
            Self::Binaryen => {
                cfg_select! {
                    target_os = "windows" => {
                        "binaryen-version_130"
                    }
                    target_os = "macos" => {
                        "binaryen-version_130"
                    }
                    _ => {
                        "binaryen-version_130"
                    }
                }
            }
        }
    }
    pub fn url(&self) -> Cow<'_, str> {
        match self {
            Self::WasiSdk => {
                if let Ok(var) = std::env::var("WASI_SDK_URL") {
                    Cow::Owned(var)
                } else {
                    cfg_select! {
                        target_os = "windows" => {
                            "https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-33/wasi-sdk-33.0-x86_64-windows.tar.gz".into()
                        }
                        target_os = "macos" => {
                            "https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-33/wasi-sdk-33.0-arm64-macos.tar.gz".into()
                        }
                        _ => {
                            "https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-33/wasi-sdk-33.0-x86_64-linux.tar.gz".into()
                        }
                    }
                }
            }
            Self::Wasmtime => {
                if let Ok(var) = std::env::var("WASMTIME_URL") {
                    Cow::Owned(var)
                } else {
                    cfg_select! {
                        target_os = "windows" => {
                            "https://github.com/bytecodealliance/wasmtime/releases/download/v46.0.1/wasmtime-v46.0.1-x86_64-windows.zip".into()
                        }
                        target_os = "macos" => {
                            "https://github.com/bytecodealliance/wasmtime/releases/download/v46.0.1/wasmtime-v45.0.1-aarch64-macos.tar.xz".into()
                        }
                        _ => {
                            "https://github.com/bytecodealliance/wasmtime/releases/download/v46.0.1/wasmtime-v45.0.1-x86_64-linux.tar.xz".into()
                        }
                    }
                }
            }
            Self::Binaryen => {
                if let Ok(var) = std::env::var("BINARYEN_URL") {
                    Cow::Owned(var)
                } else {
                    cfg_select! {
                        target_os = "windows" => {
                            "https://github.com/WebAssembly/binaryen/releases/download/version_130/binaryen-version_130-x86_64-windows.tar.gz".into()
                        }
                        target_os = "macos" => {
                            "https://github.com/WebAssembly/binaryen/releases/download/version_130/binaryen-version_130-arm64-macos.tar.gz".into()
                        }
                        _ => {
                            "https://github.com/WebAssembly/binaryen/releases/download/version_130/binaryen-version_130-x86_64-linux.tar.gz".into()
                        }
                    }
                }
            }
        }
    }
}

fn main() {
    let cli = Cli::parse();
    let result = match cli.action {
        Action::GetWasmTool {
            resource_type,
            install,
        } => task::get_wasm_tool(resource_type, install),
        Action::GenerateReflectionCode => task::generate_reflection_code(),
        Action::BuildWasm { tool } => task::build_wasm(tool),
        Action::CopyWasm => task::copy_wasm(),
        Action::OptimizeWasm { tool } => task::optimize_wasm(tool),
        Action::GenerateJavaClass => task::generate_java_class(),
        Action::Generate { tool } => task::generate(tool),
    };
    if let Err(e) = result {
        eprintln!("An error occurred: {e}");
        if let Some(bt) = ErrorCompat::backtrace(&e) {
            eprintln!("{bt}");
        }
    }
}
