use serde_reflection::{Tracer, TracerConfig};
use typalize_wasm::{ASTNode, Token};

fn main() {
    let mut tracer = Tracer::new(TracerConfig::default());
    tracer.trace_simple_type::<ASTNode>().unwrap();
    tracer.trace_simple_type::<Token>().unwrap();
    let registry = tracer.registry().unwrap();
    let option = serde_saphyr::ser_options! {
            compact_list_indent: false,
     };
    let yaml = serde_saphyr::to_string_with_options(&registry, option).unwrap();
    println!("{yaml}");
}
