use typst_syntax::{SyntaxKind, SyntaxNode};

#[derive(Debug, Copy, Clone, Eq, PartialEq, Hash, uniffi::Record)]
pub struct Token {
    pub kind: SyntaxKind,
    pub start: u32,
    pub end: u32,
}

pub trait Flatten {
    fn flatten(&self) -> Vec<Token>;
}

impl Flatten for SyntaxNode {
    fn flatten(&self) -> Vec<Token> {
        let mut tokens = Vec::new();
        let mut offset = 0_usize;
        flatten_into(self, &mut  offset, &mut tokens);
        tokens
    }
}

fn flatten_into(
    node: &SyntaxNode,
    offset: &mut usize,
    tokens: &mut Vec<Token>,
) {
    if node.text().is_empty() {
        for child in node.children() {
            flatten_into(child, offset, tokens);
        }
    } else {
        let len = node.len();
        let kind = SyntaxKind::from(node.kind());
        tokens.push(Token {
            kind,
            start: *offset as u32,
            end: (*offset + len) as u32,
        });
        *offset += len;
    }
}