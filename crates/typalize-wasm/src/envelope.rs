use serde::{Deserialize, Serialize};
use typst_syntax::SyntaxKind;

#[derive(Debug, Copy, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct Token {
    pub start: u32,
    pub end: u32,
    pub kind: u8,
}

impl Token {
    pub fn new(kind: SyntaxKind, start: usize, end: usize) -> Self {
        Self {
            start: start as u32,
            end: end as u32,
            kind: kind as u8,
        }
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct Tokens(pub Vec<Token>);

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct ASTNode {
    pub start: u32,
    pub end: u32,
    pub children_count: u32,
    pub kind: u8,
    pub is_leaf: bool,
    pub is_error: bool,
    pub error_message: Option<String>,
}

impl ASTNode {
    pub fn new(
        kind: SyntaxKind,
        start: usize,
        end: usize,
        children_count: usize,
        is_leaf: bool,
        is_error: bool,
        error_message: Option<String>,
    ) -> Self {
        Self {
            start: start as u32,
            end: end as u32,
            children_count: children_count as u32,
            kind: kind as u8,
            is_leaf,
            is_error,
            error_message,
        }
    }
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, Serialize, Deserialize)]
pub struct ASTNodes(pub Vec<ASTNode>);
