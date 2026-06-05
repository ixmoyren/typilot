extern crate typst_syntax;

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