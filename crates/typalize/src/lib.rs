uniffi::setup_scaffolding!("Typalize");
pub mod syntax_kind;
mod syntax_mode;

#[uniffi::export]
pub fn version() -> String {
    "typst 0.14.2".to_owned()
}
