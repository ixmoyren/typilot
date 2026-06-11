mod task;
mod util;

use camino::Utf8PathBuf;
use clap::{Parser, Subcommand, ValueEnum};
use indoc::indoc;
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
    #[command(about = "Build the typalize crate")]
    Build {
        #[arg(short)]
        release: bool,
    },
    #[command(about = "Copy the built dynamic library to the resource directory.")]
    CopyDylib,
    #[command(about = "Java bindings generator for Rust")]
    Generate {
        #[arg(
            long,
            short,
            help = "Directory in which to write generated files. Default is same folder as .udl file."
        )]
        out_dir: Option<Utf8PathBuf>,

        #[arg(long, short)]
        #[arg(
            help = "Path to optional uniffi config file. This config is merged with the `uniffi.toml` config present in each crate, with its values taking precedence."
        )]
        config: Option<Utf8PathBuf>,

        #[arg(long = "crate")]
        #[arg(
            long_help = indoc!(
                "When a library is passed as SOURCE, only generate bindings for this crate.
                 When a UDL file is passed, use this as the crate name instead of attempting to locate and parse Cargo.toml."
        ))]
        crate_name: Option<String>,

        #[arg(help = "Path to the UDL file or compiled library (.so, .dll, .dylib, or .a)")]
        source: Option<Utf8PathBuf>,

        #[arg(long)]
        #[arg(
            long_help = indoc!("
                Whether we should exclude dependencies when running \"cargo metadata\".
                This will mean external types may not be resolved if they are implemented in crates outside of this workspace.
                This can be used in environments when all types are in the namespace and fetching all sub-dependencies causes obscure platform specific problems."
        ))]
        metadata_no_deps: bool,
    },
    #[command(about = "Obtain the toolchain of Wasm")]
    GetWasmTool {
        #[arg(value_enum)]
        resource_type: ResourceType,
        #[arg(short, long, value_name = ".tools")]
        install: Option<PathBuf>,
    },
    #[command(about = "Build the typalize-wasm crate")]
    BuildWasm {
        #[arg(short, long, value_name = ".tools")]
        install: Option<PathBuf>,
    },
    #[command(about = "Generate serialization code related to flatbuffers")]
    GenerateCode,
    #[command(about = "Copy the built wasm library to the resource directory.")]
    CopyWasm,
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
                        "wasi-sdk-33.0-x86_64-windows".into()
                    }
                    target_os = "macos" => {
                        "wasi-sdk-33.0-arm64-macos".into()
                    }
                    _ => {
                        "wasi-sdk-33.0-x86_64-linux".into()
                    }
                }
            }
            Self::Wasmtime => {
                cfg_select! {
                        target_os = "windows" => {
                            "wasmtime-v45.0.0-x86_64-windows".into()
                        }
                        target_os = "macos" => {
                            "wasmtime-v45.0.0-aarch64-macos".into()
                        }
                        _ => {
                            "wasmtime-v45.0.0-x86_64-linux".into()
                        }

                }
            }
            Self::Binaryen => {
                cfg_select! {
                    target_os = "windows" => {
                        "binaryen-version_130-x86_64-windows".into()
                    }
                    target_os = "macos" => {
                        "binaryen-version_130-arm64-macos".into()
                    }
                    _ => {
                        "binaryen-version_130-x86_64-linux".into()
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
                if let Ok(var) = std::env::var("WIZER_URL") {
                    Cow::Owned(var)
                } else {
                    cfg_select! {
                        target_os = "windows" => {
                            "https://github.com/bytecodealliance/wasmtime/releases/download/v45.0.0/wasmtime-v45.0.0-x86_64-windows.zip".into()
                        }
                        target_os = "macos" => {
                            "https://github.com/bytecodealliance/wasmtime/releases/download/v45.0.0/wasmtime-v45.0.0-aarch64-macos.tar.xz".into()
                        }
                        _ => {
                            "https://github.com/bytecodealliance/wasmtime/releases/download/v45.0.0/wasmtime-v45.0.0-x86_64-linux.tar.xz".into()
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
        Action::Build { release } => task::build(release),
        Action::CopyDylib => task::copy_dylib(),
        Action::Generate {
            out_dir,
            config,
            crate_name,
            source,
            metadata_no_deps,
        } => task::generate(out_dir, config, crate_name, source, metadata_no_deps),
        Action::GetWasmTool {
            resource_type,
            install,
        } => task::get_wasm_tool(resource_type, install),
        Action::BuildWasm { install } => task::build_wasm(install),
        Action::GenerateCode => task::generate_code(),
        Action::CopyWasm => task::copy_wasm(),
    };
    if let Err(e) = result {
        eprintln!("An error occurred: {e}");
        if let Some(bt) = ErrorCompat::backtrace(&e) {
            eprintln!("{bt}");
        }
    }
}
