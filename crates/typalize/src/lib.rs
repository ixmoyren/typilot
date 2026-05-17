uniffi::setup_scaffolding!("typalize");
pub mod syntax_kind;
mod syntax_mode;

#[uniffi::export(default(name = "World"))]
pub fn hello_name(name: String) -> String {
    format!("Hello, {}!", name)
}

#[uniffi::export]
pub fn add(left: u64, right: u64) -> u64 {
    left + right
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}
