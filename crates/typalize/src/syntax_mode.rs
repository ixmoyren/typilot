type TypstSyntaxMode = typst_syntax::SyntaxMode;

#[uniffi::remote(Enum)]
pub enum TypstSyntaxMode {
    /// Text and markup, as in the top level.
    Markup,
    /// Math atoms, operators, etc., as in equations.
    Math,
    /// Keywords, literals and operators, as after hashes.
    Code,
}
