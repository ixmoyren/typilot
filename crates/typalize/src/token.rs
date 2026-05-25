use typst_syntax::{highlight, LinkedNode, SyntaxKind, SyntaxNode, Tag};

#[derive(Debug, Copy, Clone, Eq, PartialEq, Hash, uniffi::Record)]
pub struct Token {
    pub kind: SyntaxKind,
    pub start: u32,
    pub end: u32,
    pub tag: Option<Tag>,
}

#[derive(Debug, Clone, Eq, PartialEq, Hash, uniffi::Record)]
pub struct ASTNode {
    pub kind: SyntaxKind,
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
        flatten_into(self, 0, &mut tokens, offsets);
        tokens
    }
}

fn flatten_into<'a>(node: &LinkedNode<'a>, offset: usize, tokens: &mut Vec<Token>, offsets: &[usize]) {
    let len = node.len();
    let kind = node.kind();
    if node.children().peekable().peek().is_none() {
        let tag = highlight(node);
        tokens.push(Token {
            kind,
            start: offsets[offset] as u32,
            end: offsets[offset + len] as u32,
            tag
        });
    }
    let mut child_offset = offset;
    for child in node.children() {
        flatten_into(&child, child_offset, tokens, offsets);
        child_offset += child.len();
    }
}

pub trait ASTBuilder {
    fn build(&self) -> Vec<ASTNode>;
}

impl ASTBuilder for SyntaxNode {
    fn build(&self) -> Vec<ASTNode> {
        let mut nodes = Vec::new();
        for child in self.children() {
            build_ast_nodes(&child, &mut nodes);
        }
        nodes
    }
}

fn build_ast_nodes(node: &SyntaxNode, nodes: &mut Vec<ASTNode>) {
    let kind = node.kind();
    let is_error = node.kind().is_error();
    let error_message: Option<String> = if is_error {
        node.errors()
            .into_iter()
            .next()
            .map(|e| e.message.to_string())
    } else {
        None
    };
    let children = node.children().collect::<Vec<&SyntaxNode>>();
    let is_leaf = children.is_empty();
    nodes.push(ASTNode {
        kind,
        is_leaf,
        is_error,
        error_message,
        children_count: children.len() as u32,
    });
    for child in &children {
        build_ast_nodes(child, nodes);
    }
}
