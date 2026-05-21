use crate::{
    Result,
    util::{
        LINUX_DLL_PREFIX, LINUX_DLL_SUFFIX, LINUX_RESOURCES_PATH, LINUX_TARGET, PACKAGE_NAME,
        WINDOWS_DLL_PREFIX, WINDOWS_DLL_SUFFIX, WINDOWS_GUN_RESOURCES_PATH, WINDOWS_GUN_TARGET,
        copy_to_resources, run,
    },
};
use camino::Utf8PathBuf;
use cargo_metadata::{CrateType, MetadataCommand};
use snafu::{OptionExt, ResultExt};
use std::{fs, process::Command};
use uniffi::{GenerateOptions, TargetLanguage};

pub fn build(release: bool) -> Result<()> {
    let mut args = if release {
        vec!["build", "--release"]
    } else {
        vec!["build"]
    };
    let mut targets = vec!["--target", LINUX_TARGET, "--target", WINDOWS_GUN_TARGET];
    args.append(&mut targets);
    run(Command::new("cargo"), args).with_whatever_context(|_| "Failed to run cargo build")?;
    Ok(())
}
pub fn copy_dylib() -> Result<()> {
    let metadata = MetadataCommand::new()
        .no_deps()
        .exec()
        .with_whatever_context(|_| "Failed to obtain cargo metadata")?;
    let target_dir = &metadata.target_directory;
    let package = metadata
        .packages
        .iter()
        .find(|p| p.name == PACKAGE_NAME)
        .whatever_context("No typalize package")?;
    let dylib_name = package
        .targets
        .iter()
        .find(|t| t.crate_types.contains(&CrateType::CDyLib))
        .map(|t| t.name.clone())
        .whatever_context("No cdylib target name")?;
    build(true)?;
    copy_to_resources(
        target_dir,
        &dylib_name,
        LINUX_DLL_PREFIX,
        LINUX_DLL_SUFFIX,
        LINUX_TARGET,
        LINUX_RESOURCES_PATH,
    )?;
    copy_to_resources(
        target_dir,
        &dylib_name,
        WINDOWS_DLL_PREFIX,
        WINDOWS_DLL_SUFFIX,
        WINDOWS_GUN_TARGET,
        WINDOWS_GUN_RESOURCES_PATH,
    )
}

pub fn generate(
    out_dir: Option<Utf8PathBuf>,
    config: Option<Utf8PathBuf>,
    crate_name: Option<String>,
    source: Option<Utf8PathBuf>,
    metadata_no_deps: bool,
) -> Result<()> {
    let languages = vec![TargetLanguage::Kotlin];
    let out_dir = out_dir.unwrap_or_else(|| Utf8PathBuf::from("src/main/kotlin"));
    let config = config.or(Some(Utf8PathBuf::from("uniffi.toml")));
    let source = source.unwrap_or_else(|| {
        cfg_select! {
            target_os = "windows" => {
                Utf8PathBuf::from(WINDOWS_GUN_RESOURCES_PATH)
            }
            _ => {
                Utf8PathBuf::from(LINUX_RESOURCES_PATH)
            }
        }
    });
    fs::create_dir_all(&out_dir).with_whatever_context(|_| "Failed to create the output dir")?;
    uniffi::generate(GenerateOptions {
        languages,
        source,
        out_dir,
        config_override: config,
        format: false,
        crate_filter: crate_name,
        metadata_no_deps,
    })
    .with_whatever_context(|_| "Failed to generate kotlin")?;
    let args = vec!["spotlessApply"];
    cfg_select! {
        target_os = "windows" => {
            run(Command::new("./gradlew.bat"), args).with_whatever_context(|_| "Failed to format the kotlin code")?;
        }
        _ => {
            run(Command::new("./gradlew"), args).with_whatever_context(|_| "Failed to format the kotlin code")?;
        }
    }

    Ok(())
}
