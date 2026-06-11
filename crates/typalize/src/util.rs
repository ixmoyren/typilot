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
