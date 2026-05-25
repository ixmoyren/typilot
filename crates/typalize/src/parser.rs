use crate::{
    token::{ASTBuilder, ASTNode, Flatten, Token},
    util::Utf16Ext,
};
use std::sync::Arc;
use typst_syntax::LinkedNode;

#[derive(uniffi::Object)]
pub struct TypstParser;

#[uniffi::export]
impl TypstParser {
    #[uniffi::constructor]
    pub fn new() -> Arc<Self> {
        Arc::new(Self)
    }

    pub fn parse_markup(&self, text: String) -> Vec<Token> {
        let offsets = text.get_offset();
        let root = typst_syntax::parse(&text);
        let linked_node = LinkedNode::new(&root);
        linked_node.flatten(&offsets)
    }

    pub fn parse_markup_events(&self, text: String) -> Vec<ASTNode> {
        let offsets = text.get_offset();
        let root = typst_syntax::parse(&text);
        let linked_node = LinkedNode::new(&root);
        linked_node.build(&offsets)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use typst_syntax::SyntaxKind;
    #[test]
    fn test_simple_text() {
        let parser = TypstParser;
        let tokens = parser.parse_markup("hello".to_owned());
        assert_eq!(tokens.len(), 1);
        let token = tokens.first().unwrap();
        assert_eq!(token.kind, SyntaxKind::Text);
        assert_eq!(token.start, 0);
        assert_eq!(token.end, 5);
    }

    #[test]
    fn test_hashtag_code() {
        let parser = TypstParser;
        let doc = "#let x = 1";
        let tokens = parser.parse_markup(doc.to_owned());
        let token = tokens[0];
        assert_eq!(token.kind, SyntaxKind::Hash);
        let token = tokens[3];
        assert_eq!(token.kind, SyntaxKind::Ident);
        assert_eq!(token.start, 5);
        assert_eq!(token.end, 6);
        let token = tokens[5];
        let start = token.start as usize;
        let end = token.end as usize;
        assert_eq!(Some("="), doc.get(start..end))
    }

    #[test]
    fn test_markup_heading() {
        let parser = TypstParser;
        let tokens = parser.parse_markup("= Heading\nSome text".to_owned());
        let results = vec![
            Token {
                kind: SyntaxKind::HeadingMarker,
                start: 0,
                end: 1,
            },
            Token {
                kind: SyntaxKind::Space,
                start: 1,
                end: 2,
            },
            Token {
                kind: SyntaxKind::Text,
                start: 2,
                end: 9,
            },
            Token {
                kind: SyntaxKind::Space,
                start: 9,
                end: 10,
            },
            Token {
                kind: SyntaxKind::Text,
                start: 10,
                end: 19,
            },
        ];
        assert_eq!(tokens, results);
    }

    #[test]
    fn test_math() {
        let parser = TypstParser;
        let tokens = parser.parse_markup("$x^2$".to_owned());
        let results = vec![
            Token {
                kind: SyntaxKind::Dollar,
                start: 0,
                end: 1,
            },
            Token {
                kind: SyntaxKind::MathText,
                start: 1,
                end: 2,
            },
            Token {
                kind: SyntaxKind::Hat,
                start: 2,
                end: 3,
            },
            Token {
                kind: SyntaxKind::MathText,
                start: 3,
                end: 4,
            },
            Token {
                kind: SyntaxKind::Dollar,
                start: 4,
                end: 5,
            },
        ];
        assert_eq!(tokens, results);
    }
}
