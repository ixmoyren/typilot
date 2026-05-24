uniffi::setup_scaffolding!("Typalize");
pub mod syntax_kind;
pub mod parser;
pub mod token;
mod util;

#[uniffi::export]
pub fn version() -> String {
    "typst 0.14.2".to_owned()
}
