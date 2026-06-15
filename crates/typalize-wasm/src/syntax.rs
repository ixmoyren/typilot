use crate::envelope::{
    ASTNode, Token,
};
use typst_syntax::LinkedNode;

pub trait Flatten {
    fn flatten(&self, offsets: &[usize]) -> Vec<u8>;
}

impl<'a> Flatten for LinkedNode<'a> {
    fn flatten(&self, offsets: &[usize]) -> Vec<u8> {
        let mut tokens = Vec::<Token>::new();
        flatten_into(self, offsets, &mut tokens);
        bcs::to_bytes(&tokens).unwrap()
    }
}

fn flatten_into<'a>(node: &LinkedNode<'a>, offsets: &[usize], tokens: &mut Vec<Token>) {
    let len = node.len();
    let kind = node.kind();
    let offset = node.offset();
    if node.children().peekable().peek().is_none() {
        let start = offsets[offset];
        let end = offsets[offset + len];
        tokens.push(Token::new(kind, start, end));
    }
    for child in node.children() {
        flatten_into(&child, offsets, tokens);
    }
}

pub trait ASTBuilder {
    fn build(&self, offsets: &[usize]) -> Vec<u8>;
}

impl<'a> ASTBuilder for LinkedNode<'a> {
    fn build(&self, offsets: &[usize]) -> Vec<u8> {
        let mut nodes = Vec::new();
        build_ast_nodes(self, offsets,  &mut nodes);
        bcs::to_bytes(&nodes).unwrap()
    }
}

fn build_ast_nodes<'a>(
    node: &LinkedNode,
    offsets: &[usize],
    nodes: &mut Vec<ASTNode>,
) {
    if node.kind().is_trivia() {
        return;
    }
    let kind = node.kind();
    let offset = node.offset();
    let start = offsets[offset];
    let end = offsets[offset + node.len()];
    let is_error = node.kind().is_error();
    let error_message = if is_error {
        node.errors()
            .into_iter()
            .next()
            .map(|e| e.message.to_string())
    } else {
        None
    };
    let is_leaf = node.children().peekable().peek().is_none();
    let children_count = node
        .children()
        .filter(|child| !child.kind().is_trivia())
        .count();
    let ast_node = ASTNode::new(kind, start, end, children_count, is_leaf, is_error, error_message);
    nodes.push(ast_node);
    for child in node.children() {
        if child.kind().is_trivia() {
            continue;
        }
        build_ast_nodes(&child, offsets,nodes);
    }
}
