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
    fn flatten(&self) -> Vec<Token>;
}

impl Flatten for SyntaxNode {
    fn flatten(&self) -> Vec<Token> {
        let mut tokens = Vec::new();
        let mut offset = 0_usize;
        flatten_into(self, &mut offset, &mut tokens);
        tokens
    }
}

fn flatten_into(node: &SyntaxNode, offset: &mut usize, tokens: &mut Vec<Token>) {
    if node.text().is_empty() {
        for child in node.children() {
            flatten_into(child, offset, tokens);
        }
    } else {
        let len = node.len();
        let kind = node.kind();
        tokens.push(Token {
            kind,
            start: *offset as u32,
            end: (*offset + len) as u32,
        });
        *offset += len;
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
