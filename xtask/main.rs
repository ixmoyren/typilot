mod task;

use camino::Utf8PathBuf;
use clap::{Parser, Subcommand};
use indoc::indoc;
use snafu::{ErrorCompat, WhateverLocal};

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
    #[command(about = "Copy the built dynamic library to the `justGoGo` resource directory.")]
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
        source: Utf8PathBuf,
        #[arg(long)]
        #[arg(
            long_help = indoc!("
                Whether we should exclude dependencies when running \"cargo metadata\".
                This will mean external types may not be resolved if they are implemented in crates outside of this workspace.
                This can be used in environments when all types are in the namespace and fetching all sub-dependencies causes obscure platform specific problems."
        ))]
        metadata_no_deps: bool,
    },
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
    };
    if let Err(e) = result {
        eprintln!("An error occurred: {e}");
        if let Some(bt) = ErrorCompat::backtrace(&e) {
            eprintln!("{bt}");
        }
    }
}
