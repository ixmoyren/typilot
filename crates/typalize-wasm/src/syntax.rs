use crate::envelope::{
    ASTNode, ASTNodeArgs, ASTNodeList, ASTNodeListArgs, Token, TokenList, TokenListArgs,
};
use flatbuffers::FlatBufferBuilder;
use typst_syntax::LinkedNode;

pub trait Flatten {
    fn flatten(&self, offsets: &[usize]) -> Vec<u8>;
}

impl<'a> Flatten for LinkedNode<'a> {
    fn flatten(&self, offsets: &[usize]) -> Vec<u8> {
        let mut tokens = Vec::<Token>::new();
        flatten_into(self, offsets, &mut tokens);
        let mut builder = FlatBufferBuilder::with_capacity(tokens.len() * 12 + 64);
        let token_vec = builder.create_vector(&tokens);
        let token_list = TokenList::create(
            &mut builder,
            &TokenListArgs {
                tokens: Some(token_vec),
            },
        );
        builder.finish(token_list, None);
        builder.finished_data().to_vec()
    }
}

fn flatten_into<'a>(node: &LinkedNode<'a>, offsets: &[usize], tokens: &mut Vec<Token>) {
    let len = node.len();
    let kind = node.kind();
    let offset = node.offset();
    if node.children().peekable().peek().is_none() {
        let start = offsets[offset] as u32;
        let end = offsets[offset + len] as u32;
        tokens.push(Token::new(kind as u8, start, end));
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
        let mut builder = FlatBufferBuilder::new();
        let mut nodes = Vec::new();
        build_ast_nodes(self, offsets, &mut builder, &mut nodes);
        let node_vec = builder.create_vector(&nodes);
        let node_list = ASTNodeList::create(
            &mut builder,
            &ASTNodeListArgs {
                nodes: Some(node_vec),
            },
        );
        builder.finish(node_list, None);
        builder.finished_data().to_vec()
    }
}

fn build_ast_nodes<'a>(
    node: &LinkedNode,
    offsets: &[usize],
    builder: &mut FlatBufferBuilder<'a>,
    nodes: &mut Vec<flatbuffers::WIPOffset<ASTNode<'a>>>,
) {
    if node.kind().is_trivia() {
        return;
    }
    let kind = node.kind() as u8;
    let offset = node.offset();
    let start = offsets[offset] as u32;
    let end = offsets[offset + node.len()] as u32;
    let is_error = node.kind().is_error();
    let error_message = if is_error {
        node.errors()
            .into_iter()
            .next()
            .map(|e| builder.create_string(&e.message.to_string()))
    } else {
        None
    };
    let is_leaf = node.children().peekable().peek().is_none();
    let children_count = node
        .children()
        .filter(|child| !child.kind().is_trivia())
        .count() as u32;
    let ast_node = ASTNode::create(
        builder,
        &ASTNodeArgs {
            kind,
            start,
            end,
            is_leaf,
            is_error,
            children_count,
            error_message,
        },
    );
    nodes.push(ast_node);
    for child in node.children() {
        if child.kind().is_trivia() {
            continue;
        }
        build_ast_nodes(&child, offsets, builder, nodes);
    }
}
