use crate::{
    ResourceType, Result,
    util::{
        CC_WASM32_WASIP1, CFLAGS_WASM32_WASIP1, WASM_DLL_PREFIX, WASM_DLL_SUFFIX,
        WASM_PACKAGE_NAME, WASM_RESOURCES_PATH, WASM32_WASIP1_TARGET, copy_to_resources,
        get_lib_path_and_dylib_name, get_resource_from,
        get_target_dir_and_wasm_name, run,
    },
};
use snafu::{ResultExt, ensure_whatever};
use std::{collections::HashMap, path::PathBuf, process::Command, str::FromStr};
use serde_generate::SourceInstaller;

pub fn get_wasm_tool(resource_type: ResourceType, install: Option<PathBuf>) -> Result<()> {
    let install = install.unwrap_or(PathBuf::from(".tools"));
    get_resource_from(&resource_type, &install)
}

pub fn generate_code() -> Result<()> {
    let args = vec![
        "run",
        "-p",
        "typalize-wasm",
        "--bin",
        "gen_reflection",
        "--features",
        "gen_reflection",
    ];
    let output = Command::new("cargo")
        .args(args)
        .output()
        .with_whatever_context(|_| "Failed to run gen_reflection to generate reflection")?;
    ensure_whatever!(
        !output.stdout.is_empty(),
        "The gen_reflection output is empty"
    );
    let reflection = String::from_utf8(output.stdout)
        .with_whatever_context(|_| "Failed get the reflection info")?;
    let registry = serde_saphyr::from_str::<serde_reflection::Registry>(&reflection)
        .with_whatever_context(|_| "Failed to parse reflection")?;
    let config =
        serde_generate::CodeGeneratorConfig::new("com.github.ixmoyren.typalize".to_owned())
            .with_sealed_enums(true)
            .with_encodings(vec![serde_generate::Encoding::Bcs]);
    let generator = serde_generate::java::CodeGenerator::new(&config);
    let dir = PathBuf::from_str("src/main/java")
        .with_whatever_context(|_| "Failed to get java src")?;
    generator
        .write_source_files(dir.clone(), &registry)
        .with_whatever_context(|_| "Failed to generate java source files")?;
    let installer = serde_generate::java::Installer::new(dir);
    installer.install_serde_runtime().with_whatever_context(|_| "Failed to install serde runtime")?;
    installer.install_bcs_runtime().with_whatever_context(|_| "Failed to install bcs runtime")?;
    Ok(())
}

pub fn build_wasm(tool: Option<PathBuf>) -> Result<()> {
    generate_code()?;
    let tool = tool.unwrap_or(PathBuf::from(".tools"));
    let wasi_sdk_path = tool.join(ResourceType::WasiSdk.tool_name());
    if !wasi_sdk_path.exists() {
        get_resource_from(&ResourceType::WasiSdk, &tool)?;
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

pub fn copy_wasm() -> Result<()> {
    let (target_dir, dylib) = get_target_dir_and_wasm_name()?;
    build_wasm(None)?;
    let (lib_path, dylib_name) = get_lib_path_and_dylib_name(
        &dylib,
        WASM_DLL_PREFIX,
        WASM_DLL_SUFFIX,
        WASM_RESOURCES_PATH,
    )?;
    copy_to_resources(
        target_dir.as_std_path(),
        &dylib_name,
        lib_path.as_std_path(),
        WASM32_WASIP1_TARGET,
    )
}

pub fn optimize_wasm(tool: Option<PathBuf>) -> Result<()> {
    let tool = tool.unwrap_or(PathBuf::from(".tools"));
    let binaryen_path = tool.join(ResourceType::Binaryen.tool_name());
    if !binaryen_path.exists() {
        get_resource_from(&ResourceType::Binaryen, &tool)?;
    }
    let (_, dylib) = get_target_dir_and_wasm_name()?;
    let (lib_path, dylib_name) = get_lib_path_and_dylib_name(
        &dylib,
        WASM_DLL_PREFIX,
        WASM_DLL_SUFFIX,
        WASM_RESOURCES_PATH,
    )?;
    let resource_path = lib_path.join(&dylib_name);
    let output_name = format!("{}{}-opt{}", WASM_DLL_PREFIX, &dylib, WASM_DLL_SUFFIX);
    let output_path = lib_path.join(&output_name);
    let args = vec![
        "-Oz",
        "--strip-debug",
        resource_path.as_str(),
        "-o",
        output_path.as_str(),
    ];
    let wasm_opt = cfg_select! {
        target_os = "windows" => {
            wasmtime_path.join("bin").join("wasm-opt.exe")
        }
        _ => {
            binaryen_path.join("bin").join("wasm-opt")
        }
    };
    run(Command::new(wasm_opt), args).with_whatever_context(|_| "Failed to run cargo build")?;
    Ok(())
}
