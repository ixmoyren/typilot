use crate::{wasm_free, wasm_malloc};

pub trait Utf16Ext {
    fn get_offset(&self) -> Vec<usize>;
}

impl Utf16Ext for str {
    fn get_offset(&self) -> Vec<usize> {
        let mut dic = vec![0_usize; self.len() + 1];
        let mut utf16_pos = 0_usize;
        for (pos, ch) in self.char_indices() {
            dic[pos] = utf16_pos;
            utf16_pos += ch.len_utf16();
        }
        dic[self.len()] = utf16_pos;
        dic
    }
}

impl Utf16Ext for String {
    fn get_offset(&self) -> Vec<usize> {
        self.as_str().get_offset()
    }
}

pub fn leak(bytes: &[u8]) -> i64 {
    let len = bytes.len();

    unsafe {
        let ptr = wasm_malloc(len);

        if ptr.is_null() {
            eprintln!("error: allocation failed");
            panic!("error: allocation failed");
        }

        std::ptr::copy_nonoverlapping(bytes.as_ptr(), ptr, len);

        (((ptr as u32 as u64) << 32) | (len as u32 as u64)) as i64
    }
}

pub fn consume_string(ptr: i32, len: i32) -> String {
    let result = read_string(ptr, len);
    wasm_free(ptr as *mut u8);
    result
}

fn read_string(ptr: i32, len: i32) -> String {
    unsafe {
        let bytes = std::slice::from_raw_parts(ptr as *const u8, len as usize);
        std::str::from_utf8(bytes)
            .expect("Invalid UTF-8")
            .to_string()
    }
}