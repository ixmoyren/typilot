use crate::{
    ResourceType, Result,
    util::{
        CC_WASM32_WASIP1, LINUX_DLL_PREFIX, LINUX_DLL_SUFFIX, LINUX_RESOURCES_PATH, LINUX_TARGET,
        PACKAGE_NAME, WASM_PACKAGE_NAME, WASM32_WASIP1_TARGET, WINDOWS_DLL_PREFIX,
        WINDOWS_DLL_SUFFIX, WINDOWS_GUN_RESOURCES_PATH, WINDOWS_GUN_TARGET, copy_to_resources,
        get_lib_path_and_dylib_name, get_resource_from, get_target_dir_and_dylib_name, run,
    },
};
use camino::Utf8PathBuf;
use snafu::ResultExt;
use std::{collections::HashMap, fs, path::PathBuf, process::Command};
use uniffi::{GenerateOptions, TargetLanguage};
use crate::util::CFLAGS_WASM32_WASIP1;

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
    copy_to_resources(
        target_dir.as_std_path(),
        &dylib_name,
        lib_path.as_std_path(),
        LINUX_TARGET,
    )?;
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

pub fn get_wasm_tool(resource_type: ResourceType, install: Option<PathBuf>) -> Result<()> {
    let install = install.unwrap_or(PathBuf::from(".tools"));
    get_resource_from(&resource_type, &install)
}

pub fn build_wasm(install: Option<PathBuf>) -> Result<()> {
    let install = install.unwrap_or(PathBuf::from(".tools"));
    let wasi_sdk_path = install.join(ResourceType::WasiSdk.tool_name());
    if !wasi_sdk_path.exists() {
        get_resource_from(&ResourceType::WasiSdk, &install)?;
    }
    let envs = HashMap::from([
        ("WASI_SDK_PATH", wasi_sdk_path.display().to_string()),
        (
            "CC_wasm32_wasip1",
            wasi_sdk_path.join(CC_WASM32_WASIP1).display().to_string(),
        ),
        (
            "CFLAGS_wasm32_wasip1",
            format!(
                "--sysroot={}",
                wasi_sdk_path.join(CFLAGS_WASM32_WASIP1).display()
            ),
        ),
    ]);
    let args = vec![
        "build",
        "--package",
        WASM_PACKAGE_NAME,
        "--target",
        WASM32_WASIP1_TARGET,
        "--release",
    ];
    let mut cmd = Command::new("cargo");
    cmd.envs(envs);
    run(cmd, args).with_whatever_context(|_| "Failed to run cargo build")?;
    Ok(())
}

pub fn generate_code() -> Result<()> {
    let args = vec![
        "--rust",
        "--filename-suffix",
        "",
        "-o",
        "crates/typalize-wasm/src",
        "scheme/envelope.fbs"
    ];
    run(Command::new("flatc"), args).with_whatever_context(|_| "Failed to run flatc to generate code")?;
    Ok(())
}