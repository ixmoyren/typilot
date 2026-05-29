use typst_syntax::{LinkedNode, SyntaxKind};

#[derive(Debug, Copy, Clone, Eq, PartialEq, Hash, uniffi::Record)]
pub struct Token {
    pub kind: SyntaxKind,
    pub start: u32,
    pub end: u32,
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, uniffi::Record)]
pub struct ASTNode {
    pub kind: SyntaxKind,
    pub start: u32,
    pub end: u32,
    pub is_leaf: bool,
    pub is_error: bool,
    pub error_message: Option<String>,
    pub children_count: u32,
}

pub trait Flatten {
    fn flatten(&self, offsets: &[usize]) -> Vec<Token>;
}

impl<'a> Flatten for LinkedNode<'a> {
    fn flatten(&self, offsets: &[usize]) -> Vec<Token> {
        let mut tokens = Vec::new();
        flatten_into(self, &mut tokens, offsets);
        tokens
    }
}

fn flatten_into<'a>(node: &LinkedNode<'a>, tokens: &mut Vec<Token>, offsets: &[usize]) {
    let len = node.len();
    let kind = node.kind();
    let offset = node.offset();
    if node.children().peekable().peek().is_none() {
        tokens.push(Token {
            kind,
            start: offsets[offset] as u32,
            end: offsets[offset + len] as u32,
        });
    }
    for child in node.children() {
        flatten_into(&child, tokens, offsets);
    }
}

pub trait ASTBuilder {
    fn build(&self, offsets: &[usize]) -> Vec<ASTNode>;
}

impl<'a> ASTBuilder for LinkedNode<'a> {
    fn build(&self, offsets: &[usize]) -> Vec<ASTNode> {
        let mut nodes = Vec::new();
        build_ast_nodes(self, &mut nodes, offsets);
        nodes
    }
}

fn build_ast_nodes<'a>(node: &LinkedNode, nodes: &mut Vec<ASTNode>, offsets: &[usize]) {
    if node.kind().is_trivia() {
        return;
    }
    let kind = node.kind();
    let offset = node.offset();
    let start = offsets[offset] as u32;
    let end = offsets[offset + node.len()] as u32;
    let is_error = node.kind().is_error();
    let error_message: Option<String> = if is_error {
        node.errors()
            .into_iter()
            .next()
            .map(|e| e.message.to_string())
    } else {
        None
    };
    let is_leaf = node.children().peekable().peek().is_none();
    nodes.push(ASTNode {
        kind,
        start,
        end,
        is_leaf,
        is_error,
        error_message,
        children_count: node
            .children()
            .filter(|child| !child.kind().is_trivia())
            .count() as u32,
    });
    for child in node.children() {
        if child.kind().is_trivia() {
            continue;
        }
        build_ast_nodes(&child, nodes, offsets);
    }
}
