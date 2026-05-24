use typst_syntax::{SyntaxKind, SyntaxNode};

#[derive(Debug, Copy, Clone, Eq, PartialEq, Hash, uniffi::Record)]
pub struct Token {
    pub kind: SyntaxKind,
    pub start: u32,
    pub end: u32,
}

#[derive(Debug, Copy, Clone, Eq, PartialEq, Hash, uniffi::Enum)]
pub enum Event {
    Enter { kind: SyntaxKind },
    Leaf { kind: SyntaxKind },
    Exit { kind: SyntaxKind },
}

pub trait Flatten {
    fn flatten(&self, offsets: &[usize]) -> Vec<Token>;
}

impl Flatten for SyntaxNode {
    fn flatten(&self, offsets: &[usize]) -> Vec<Token> {
        let mut tokens = Vec::new();
        flatten_into(self, 0, &mut tokens, offsets);
        tokens
    }
}

fn flatten_into(node: &SyntaxNode, offset: usize, tokens: &mut Vec<Token>, offsets: &[usize]) {
    let len = node.len();
    let kind = node.kind();
    if node.children().peekable().peek().is_none() {
        tokens.push(Token {
            kind,
            start: offsets[offset] as u32,
            end: offsets[offset + len] as u32,
        });
    }
    let mut child_offset = offset;
    for child in node.children() {
        flatten_into(child, child_offset, tokens, offsets);
        child_offset += child.len();
    }
}

pub trait EmitEvent {
    fn emit_event(&self) -> Vec<Event>;
}

impl EmitEvent for SyntaxNode {
    fn emit_event(&self) -> Vec<Event> {
        let mut events = Vec::new();
        for child in self.children() {
            emit_events(&child, &mut events);
        }
        events
    }
}

fn emit_events(node: &SyntaxNode, events: &mut Vec<Event>) {
    if node.text().is_empty() {
        let kind = node.kind();
        events.push(Event::Enter { kind });
        for child in node.children() {
            emit_events(child, events);
        }
        events.push(Event::Exit { kind });
    } else {
        events.push(Event::Leaf {
            kind: node.kind(),
        });
    }
}
