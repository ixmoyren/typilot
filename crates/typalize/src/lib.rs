extern crate typst_syntax;

pub use crate::envelope::{ASTNode, ASTNodes, Token, Tokens, TypstSyntaxKind};
use std::ffi::c_void;

use crate::{
    syntax::{ASTBuilder, Flatten},
    util::{Utf16Ext, consume_string, leak},
};
use typst_syntax::LinkedNode;

#[allow(unused)]
mod envelope;
mod syntax;
mod util;

unsafe extern "C" {
    fn malloc(size: usize) -> *mut c_void;
    fn free(ptr: *mut c_void);
}

/// Allocates `size` bytes of memory and returns a pointer to the allocated block.
///
/// This is a thin wrapper around the C standard library `malloc`, exported for
/// use by the host environment (e.g., a WebAssembly runtime such as Endive).
///
/// # Returns
///
/// - A non-null pointer to the allocated memory on success.
/// - A null pointer if allocation fails.
///
/// # Safety
///
/// The caller is responsible for:
/// - Passing the same pointer to [`wasm_free`] exactly once when done.
/// - Not using the pointer after it has been freed.
/// - Not reading from the returned memory before writing to it (it is uninitialized).
#[unsafe(no_mangle)]
pub unsafe extern "C" fn wasm_malloc(size: usize) -> *mut c_void {
    unsafe { malloc(size) }
}

/// Frees a memory block previously allocated by [`wasm_malloc`].
///
/// This is a thin wrapper around the C standard library `free`, exported for
/// use by the host environment (e.g., a WebAssembly runtime such as Chicory).
///
/// # Safety
///
/// The caller must ensure that:
/// - `ptr` was returned by a prior call to [`wasm_malloc`].
/// - `ptr` has not already been freed (double-free is undefined behavior).
/// - No references to the memory exist after this call.
/// - Passing a null pointer is safe (no-op, matching C `free` semantics).
#[unsafe(no_mangle)]
pub unsafe extern "C" fn wasm_free(ptr: *mut c_void) {
    unsafe { free(ptr) }
}

#[unsafe(no_mangle)]
pub extern "C" fn version() -> i64 {
    let version = "typst-syntax 0.15.0".to_owned();
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

#[cfg(test)]
mod tests {
    use crate::{
        ASTNode, Token, TypstSyntaxKind,
        syntax::{build_ast_nodes, flatten_into},
        util::Utf16Ext,
    };
    use typst_syntax::LinkedNode;

    #[test]
    fn test() {
        let text = "text\n ";
        let offsets = text.get_offset();
        let root = typst_syntax::parse(text);
        let linked_node = LinkedNode::new(&root);
        let mut tokens = Vec::<Token>::new();
        flatten_into(&linked_node, &offsets, &mut tokens);
        let results = vec![
            Token {
                start: 0,
                end: 4,
                kind: TypstSyntaxKind::Text,
            },
            Token {
                start: 4,
                end: 6,
                kind: TypstSyntaxKind::Space,
            },
        ];
        assert_eq!(results, tokens);
        let mut nodes = Vec::new();
        build_ast_nodes(&linked_node, &offsets, &mut nodes);
        let results = vec![
            ASTNode {
                start: 0,
                end: 6,
                children_count: 1,
                kind: TypstSyntaxKind::Markup,
                is_leaf: false,
                is_error: false,
                error_message: None,
            },
            ASTNode {
                start: 0,
                end: 4,
                children_count: 0,
                kind: TypstSyntaxKind::Text,
                is_leaf: true,
                is_error: false,
                error_message: None,
            },
        ];
        assert_eq!(results, nodes);
    }
}
