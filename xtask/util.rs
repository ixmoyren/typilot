use camino::Utf8PathBuf;
use cargo_metadata::{CrateType, MetadataCommand};
use snafu::{OptionExt, ResultExt};
use std::{
    env,
    ffi::OsStr,
    fs,
    io::{BufRead, BufReader, Read},
    path::Path,
    process::{Command, Stdio},
};

pub const PACKAGE_NAME: &str = "typalize";

pub const LINUX_TARGET: &str = "x86_64-unknown-linux-gnu";
pub const LINUX_DLL_PREFIX: &str = "lib";
pub const LINUX_DLL_SUFFIX: &str = ".so";
pub const LINUX_RESOURCES_PATH: &str = "src/main/resources/linux-x86-64";

pub const WINDOWS_GUN_TARGET: &str = "x86_64-pc-windows-gnu";
pub const WINDOWS_DLL_PREFIX: &str = "";
pub const WINDOWS_DLL_SUFFIX: &str = ".dll";
pub const WINDOWS_GUN_RESOURCES_PATH: &str = "src/main/resources/win32-x86-64";

pub fn get_target_dir_and_dylib_name() -> crate::Result<(Utf8PathBuf, String)> {
    let metadata = MetadataCommand::new()
        .no_deps()
        .exec()
        .with_whatever_context(|_| "Failed to obtain cargo metadata")?;
    let target_dir = metadata.target_directory;
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
    Ok((target_dir, dylib_name))
}

pub fn copy_to_resources(
    target_dir: &Path,
    dylib_name: &str,
    lib_path: &Path,
    target: &str,
) -> crate::Result<()> {
    fs::create_dir_all(&lib_path).with_whatever_context(|_| "Failed to crate lib dir")?;
    let resources_path = lib_path.join(&dylib_name);
    let dylib_path = target_dir.join(target).join("release").join(&dylib_name);
    fs::copy(dylib_path, resources_path).with_whatever_context(|_| "Failed to copy the dylib")?;
    Ok(())
}

pub fn get_lib_path_and_dylib_name(
    dylib_name: &str,
    prefix: &str,
    suffix: &str,
    resource: &str,
) -> crate::Result<(Utf8PathBuf, String)> {
    let dylib_name = format!("{}{dylib_name}{}", prefix, suffix);
    let lib_path = env::current_dir()
        .with_whatever_context(|_| "Failed to get current dir")?
        .join(resource)
        .to_str()
        .whatever_context("No lib path")?
        .to_owned();
    Ok((Utf8PathBuf::from(lib_path), dylib_name))
}

pub fn run<I, S>(mut cmd: Command, args: I) -> crate::Result<()>
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

fn println_lines<T>(inner: T) -> crate::Result<()>
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
