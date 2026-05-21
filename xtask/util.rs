use camino::Utf8PathBuf;
use snafu::ResultExt;
use std::{
    env,
    ffi::OsStr,
    fs,
    io::{BufRead, BufReader, Read},
    process::{Command, Stdio},
};

pub const PACKAGE_NAME: &str = "typalize";

pub const LINUX_TARGET: &str = "x86_64-unknown-linux-gnu";
pub const LINUX_DLL_PREFIX: &str = "lib";
pub const LINUX_DLL_SUFFIX: &str = ".so";
pub const LINUX_RESOURCES_PATH: &str = "src/main/resources/native/linux_x86_64";

pub const WINDOWS_GUN_TARGET: &str = "x86_64-pc-windows-gnu";
pub const WINDOWS_DLL_PREFIX: &str = "";
pub const WINDOWS_DLL_SUFFIX: &str = ".dll";
pub const WINDOWS_GUN_RESOURCES_PATH: &str = "src/main/resources/native/windows_x86_64";

pub fn copy_to_resources(
    target_dir: &Utf8PathBuf,
    dylib_name: &str,
    prefix: &str,
    suffix: &str,
    target: &str,
    resource: &str,
) -> crate::Result<()> {
    let dylib_name = format!(
        "{}{dylib_name}{}",
        prefix,
        suffix
    );
    let dylib_path = target_dir.join(target).join("release").join(&dylib_name);
    let lib_path = env::current_dir()
        .with_whatever_context(|_| "Failed to get current dir")?
        .join(resource);
    fs::create_dir_all(&lib_path).with_whatever_context(|_| "Failed to crate lib dir")?;
    let resources_path = lib_path.join(&dylib_name);
    fs::copy(dylib_path, resources_path).with_whatever_context(|_| "Failed to copy the dylib")?;
    Ok(())
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
