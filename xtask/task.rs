use super::Result;
use camino::Utf8PathBuf;
use cargo_metadata::{CrateType, MetadataCommand};
use snafu::{OptionExt, ResultExt};
use std::{
    env,
    ffi::OsStr,
    fs,
    io::{BufRead, BufReader, Read},
    process::{Command, Stdio},
};
use uniffi::{GenerateOptions, TargetLanguage};

pub fn build(release: bool) -> Result<()> {
    let args = if release {
        vec!["build", "--release"]
    } else {
        vec!["build"]
    };
    run(Command::new("cargo"), args).with_whatever_context(|_| "Failed to run cargo build")?;
    Ok(())
}
pub fn copy_dylib() -> Result<()> {
    let metadata = MetadataCommand::new()
        .no_deps()
        .exec()
        .with_whatever_context(|_| "Failed to obtain cargo metadata")?;
    let target_dir = &metadata.target_directory;
    let release_dir = target_dir.join("release");
    let typilot_package = metadata
        .packages
        .iter()
        .find(|p| p.name == "typalize")
        .whatever_context("No typalize package")?;
    let dylib_name = typilot_package
        .targets
        .iter()
        .find(|t| t.crate_types.contains(&CrateType::CDyLib))
        .map(|t| t.name.clone())
        .whatever_context("No cdylib target name")?;
    build(true)?;
    let dylib_name = format!(
        "{}{dylib_name}{}",
        env::consts::DLL_PREFIX,
        env::consts::DLL_SUFFIX
    );
    let dylib_name_str = dylib_name.as_str();
    let dylib_path = release_dir.join(dylib_name_str);
    let lib_path = env::current_dir()
        .with_whatever_context(|_| "Failed to get current dir")?
        .join("src/main/resources/lib");
    fs::create_dir_all(&lib_path).with_whatever_context(|_| "Failed to crate lib dir")?;
    let resources_path = lib_path.join(dylib_name_str);
    fs::copy(dylib_path, resources_path).with_whatever_context(|_| "Failed to copy the dylib")?;
    Ok(())
}

pub fn generate(
    out_dir: Option<Utf8PathBuf>,
    config: Option<Utf8PathBuf>,
    crate_name: Option<String>,
    source: Utf8PathBuf,
    metadata_no_deps: bool,
) -> Result<()> {
    let languages = vec![TargetLanguage::Kotlin];
    let out_dir = out_dir.unwrap_or_else(|| Utf8PathBuf::from("src/main/kotlin"));
    let config = config.or(Some(Utf8PathBuf::from("uniffi.toml")));
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

pub fn run<I, S>(mut cmd: Command, args: I) -> Result<()>
where
    I: IntoIterator<Item = S>,
    S: AsRef<OsStr>,
{
    let mut child = cmd
        .args(args)
        .stdout(Stdio::piped())
        .stderr(Stdio::piped())
        .spawn()
        .with_whatever_context(|_| "Failed to spawn the command")?;
    if let Some(stdout) = child.stdout.take() {
        println_lines(stdout)
    } else if let Some(stderr) = child.stderr.take() {
        println_lines(stderr)
    } else {
        Ok(())
    }
}

fn println_lines<T>(inner: T) -> Result<()>
where
    T: Read,
{
    let lines = BufReader::new(inner).lines();
    for line in lines {
        println!(
            "{}",
            line.with_whatever_context(|_| "Failed to obtain the command output")?
        );
    }
    Ok(())
}
