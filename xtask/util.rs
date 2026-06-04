use crate::ResourceType;
use camino::Utf8PathBuf;
use cargo_metadata::{CrateType, MetadataCommand};
use indicatif::{ProgressBar, ProgressStyle};
use lzma_rust2::XzReader;
use percent_encoding::percent_decode_str;
use snafu::{OptionExt, ResultExt, ensure_whatever};
use std::{
    env,
    ffi::OsStr,
    fs,
    fs::{File, create_dir_all},
    io::{BufRead, BufReader, BufWriter, Read, Seek, Write},
    path::Path,
    process::{Command, Stdio},
};
use std::fs::remove_file;
use tar::Archive;

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
    create_dir_all(&lib_path).with_whatever_context(|_| "Failed to crate lib dir")?;
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

pub fn get_resource_from(
    resource: &ResourceType,
    install: &Path,
) -> crate::Result<()> {
    if !install.exists() {
        create_dir_all(install).with_whatever_context(|_| {
            format!("Couldn't create the install dir({})", install.display())
        })?;
    }
    let tool = install.join(resource.tool_name());
    if tool.exists() {
        fs::remove_dir_all(&tool).with_whatever_context(|_| {
            format!("Couldn't remove the dir({})", tool.display())
        })?;
    }

    let client = reqwest::blocking::Client::new();
    let mut response = client
        .get(resource.url().as_ref())
        .send()
        .with_whatever_context(|_| format!("Couldn't get the resource({})", resource.url()))?;

    ensure_whatever!(
        response.status().is_success(),
        "Failed to download the resource, status code is {}",
        response.status()
    );

    let total_size = response
        .headers()
        .get(reqwest::header::CONTENT_LENGTH)
        .and_then(|v| v.to_str().ok())
        .and_then(|s| s.parse::<u64>().ok());

    let filename = get_filename(&response, resource.url().as_ref())?;

    let pb = if let Some(size) = total_size {
        ProgressBar::new(size)
    } else {
        ProgressBar::new_spinner()
    };
    pb.set_style(
        ProgressStyle::default_bar()
            .template("{msg}\n{spinner:.green} [{elapsed_precise}] [{bar:40.cyan/blue}] {bytes}/{total_bytes} ({eta})")
            .with_whatever_context(|_| "Couldn't create progress template")?
            .progress_chars("#>-"),
    );
    pb.set_message(format!("Downloading {}", resource.url()));

    let filepath = install.join(filename);
    let mut buf_writer = BufWriter::new(File::create(&filepath).with_whatever_context(|_| "Couldn't create the tmp file")?);
    let mut buffer = [0; 8192];
    loop {
        let bytes = response
            .read(&mut buffer)
            .with_whatever_context(|_| "Failed to read the response")?;
        if bytes == 0 {
            break;
        }
        buf_writer
            .write_all(&buffer[..bytes])
            .with_whatever_context(|_| "Failed to write the file")?;
        pb.inc(bytes as u64);
    }
    buf_writer.flush().with_whatever_context(|_| "Couldn't flush the file")?;
    pb.finish_with_message("Downloading finish!");

    let response = BufReader::new(File::open(&filepath).with_whatever_context(|_| "Couldn't open the tmp file")?);
    match filepath.extension() {
        Some(ext) if ext == "zip" => extract_zip(response, &install)?,
        Some(ext) if ext == "xz" => extract_tar_xz(response, &install)?,
        Some(ext) if ext == "gz" => extract_tar_gz(response, &install)?,
        _ => (),
    }
    remove_file(&filepath).with_whatever_context(|_| "Couldn't remove the tmp file")?;
    Ok(())
}

fn extract_tar_xz(response: impl Read, install: impl AsRef<Path>) -> crate::Result<()> {
    let lzma_reader = XzReader::new(response, true);
    let mut archive = Archive::new(lzma_reader);
    archive
        .unpack(install)
        .with_whatever_context(|_| "Couldn't unpack the archive file")?;
    Ok(())
}

fn extract_tar_gz(response: impl Read, install: impl AsRef<Path>) -> crate::Result<()> {
    let gz_decoder = flate2::read::GzDecoder::new(response);
    let mut archive = Archive::new(gz_decoder);
    archive
        .unpack(install)
        .with_whatever_context(|_| "Couldn't unpack the archive file")?;
    Ok(())
}

fn extract_zip(response: impl Read + Seek, install: impl AsRef<Path>) -> crate::Result<()> {
    let install = install.as_ref();
    let mut archive = zip::ZipArchive::new(response)
        .with_whatever_context(|_| "Couldn't get archive from the zip file")?;
    for i in 0..archive.len() {
        let mut file = archive
            .by_index(i)
            .with_whatever_context(|_| "Couldn't get the file by index from the archive")?;
        let out_path = &install.join(file.name());
        if file.is_dir() {
            create_dir_all(out_path)
                .with_whatever_context(|_| "Couldn't create out path from the archive")?;
        } else {
            if let Some(parent) = out_path.parent() {
                create_dir_all(parent).with_whatever_context(
                    |_| "Couldn't create out parent path from the archive",
                )?;
            }
            let mut out_file = File::create(&out_path)
                .with_whatever_context(|_| "Couldn't create out file from the archive")?;
            std::io::copy(&mut file, &mut out_file)
                .with_whatever_context(|_| "Failed to write the out file")?;
        }
        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt;
            if let Some(mode) = file.unix_mode() {
                fs::set_permissions(&out_path, fs::Permissions::from_mode(mode))
                    .with_whatever_context(|_| "Failed to set the file mode")?;
            }
        }
    }
    Ok(())
}

pub fn get_filename(
    response: &reqwest::blocking::Response,
    fallback_url: &str,
) -> crate::Result<String> {
    if let Some(content_disposition) = response.headers().get("content-disposition")
        && let Ok(disposition_str) = content_disposition.to_str()
        && let Some(filename) = parse_content_disposition_filename(disposition_str)
    {
        return Ok(filename);
    }

    let url =
        reqwest::Url::parse(fallback_url).with_whatever_context(|_| "Failed to parse the URL")?;
    let path = url.path();
    let filename = path
        .split('/')
        .last()
        .whatever_context("The URL does not contain the file name")?;
    ensure_whatever!(
        !filename.is_empty(),
        "The file name cannot be extracted from the URL"
    );
    Ok(filename.to_string())
}

fn parse_content_disposition_filename(disposition: &str) -> Option<String> {
    for part in disposition.split(';') {
        let part = part.trim();
        if part.starts_with("filename*=")
            && let value = &part[9..]
        {
            let parts = value.split('\'').collect::<Vec<&str>>();
            if parts.len() == 3
                && let charset = parts[0]
                && charset.eq_ignore_ascii_case("utf-8")
                && let value = percent_decode_str(parts[2]).decode_utf8_lossy()
            {
                return Some(value.to_string());
            }
        }
    }
    for part in disposition.split(';') {
        let part = part.trim();
        if part.starts_with("filename=")
            && let value = &part[9..]
            && let value = value.trim_matches('"')
        {
            return Some(value.to_string());
        }
    }
    None
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

#[cfg(test)]
mod tests {
    use std::fs::File;
    use std::io::BufReader;
    use lzma_rust2::XzReader;
    use tar::Archive;

    #[test]
    fn test()  {
        let filepath = "/home/imxonren/CreativityProjects/ixmoyren/typilot/.tools/wizer/wasmtime-v45.0.0-x86_64-linux.tar.xz";
        let response = BufReader::new(File::open(filepath).unwrap());
        let lzma_reader = XzReader::new(response, true);
        let mut archive = Archive::new(lzma_reader);
        archive
            .unpack("/home/imxonren/CreativityProjects/ixmoyren/typilot/.tools/wizer").unwrap();
    }
}
