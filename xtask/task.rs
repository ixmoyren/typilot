use crate::{Result, util::{
    LINUX_DLL_PREFIX, LINUX_DLL_SUFFIX, LINUX_RESOURCES_PATH, LINUX_TARGET, PACKAGE_NAME,
    WINDOWS_DLL_PREFIX, WINDOWS_DLL_SUFFIX, WINDOWS_GUN_RESOURCES_PATH, WINDOWS_GUN_TARGET,
    copy_to_resources, get_lib_path_and_dylib_name, get_target_dir_and_dylib_name, run,
}, ResourceType};
use camino::Utf8PathBuf;
use snafu::ResultExt;
use std::{fs, process::Command};
use std::path::PathBuf;
use uniffi::{GenerateOptions, TargetLanguage};
use crate::util::get_resource_from;

pub fn build(release: bool) -> Result<()> {
    let mut args = vec!["build", "--package", PACKAGE_NAME];
    if release {
        args.push("--release");
    };
    let mut targets = vec!["--target", LINUX_TARGET, "--target", WINDOWS_GUN_TARGET];
    args.append(&mut targets);
    run(Command::new("cargo"), args).with_whatever_context(|_| "Failed to run cargo build")?;
    Ok(())
}
pub fn copy_dylib() -> Result<()> {
    let (target_dir, dylib) = get_target_dir_and_dylib_name()?;
    build(true)?;
    let (lib_path, dylib_name) = get_lib_path_and_dylib_name(
        &dylib,
        LINUX_DLL_PREFIX,
        LINUX_DLL_SUFFIX,
        LINUX_RESOURCES_PATH,
    )?;
    copy_to_resources(target_dir.as_std_path(), &dylib_name, lib_path.as_std_path(), LINUX_TARGET)?;
    let (lib_path, dylib_name) = get_lib_path_and_dylib_name(
        &dylib,
        WINDOWS_DLL_PREFIX,
        WINDOWS_DLL_SUFFIX,
        WINDOWS_GUN_RESOURCES_PATH,
    )?;
    copy_to_resources(
        &target_dir.as_std_path(),
        &dylib_name,
        &lib_path.as_std_path(),
        WINDOWS_GUN_TARGET,
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
    let (_target_dir, dylib_name) = get_target_dir_and_dylib_name()?;
    let source = if let Some(source) = source {
        source
    } else {
        cfg_select! {
            target_os = "windows" => {
                let (lib_path, dylib_name) = get_lib_path_and_dylib_name(
                    &dylib_name,
                    WINDOWS_DLL_PREFIX,
                    WINDOWS_DLL_SUFFIX,
                    WINDOWS_GUN_RESOURCES_PATH,
                )?;
                lib_path.join(dylib_name)
            }
            _ => {
                let (lib_path, dylib_name) = get_lib_path_and_dylib_name(
                    &dylib_name,
                    LINUX_DLL_PREFIX,
                    LINUX_DLL_SUFFIX,
                    LINUX_RESOURCES_PATH,
                )?;
                lib_path.join(dylib_name)
            }
        }
    };
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

pub fn get_wasm_tool(resource_type: ResourceType, install: Option<PathBuf>) -> Result<()>  {
    let install = install.unwrap_or(PathBuf::from(".tools"));
    get_resource_from(&resource_type, &install)
}
