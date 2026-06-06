extern crate typst_syntax;

use typst_syntax::LinkedNode;
use crate::syntax::{ASTBuilder, Flatten};
use crate::util::{consume_string, leak, Utf16Ext};

#[allow(unused)]
mod envelope;
mod util;
mod syntax;

unsafe extern "C" {
    fn malloc(size: usize) -> *mut u8;
    fn free(ptr: *mut u8);
}

#[unsafe(no_mangle)]
pub extern "C" fn wasm_malloc(size: usize) -> *mut u8 {
    unsafe { malloc(size) }
}

#[unsafe(no_mangle)]
pub extern "C" fn wasm_free(ptr: *mut u8) {
    unsafe { free(ptr) }
}

#[unsafe(no_mangle)]
pub extern "C" fn version() -> i64 {
    let version = "typst-syntax 0.14.2".to_owned();
    leak(version.as_bytes())
}

#[unsafe(no_mangle)]
pub extern "C" fn tokenize(text_ptr: i32, text_len: i32) -> i64 {
    let text = consume_string(text_ptr, text_len);
    let offsets = text.get_offset();
    let root = typst_syntax::parse(&text);
    let linked_node = LinkedNode::new(&root);
    let tokens = linked_node.flatten(&offsets);
    leak(tokens.as_slice())
}

#[unsafe(no_mangle)]
pub extern "C" fn parse(text_ptr: i32, text_len: i32) -> i64 {
    let text = consume_string(text_ptr, text_len);
    let offsets = text.get_offset();
    let root = typst_syntax::parse(&text);
    let linked_node = LinkedNode::new(&root);
    let nodes = linked_node.build(&offsets);
    leak(nodes.as_slice())
}
