extern crate proc_macro;

use proc_macro::TokenStream;
use proc_macro2::Span;
use quote::quote;
use syn::{DeriveInput, Meta, Path, parse_macro_input};

#[proc_macro_derive(EnumFrom, attributes(enum_from))]
pub fn enum_from_derive(input: TokenStream) -> TokenStream {
    let input = parse_macro_input!(input as DeriveInput);
    expand_enum_from(input).unwrap_or_else(|e| e.to_compile_error().into())
}

fn expand_enum_from(input: DeriveInput) -> syn::Result<TokenStream> {
    let target_enum = match input.data {
        syn::Data::Enum(data) => data,
        _ => {
            return Err(syn::Error::new_spanned(
                input,
                "EnumFrom can only be derived on enum",
            ));
        }
    };

    let target_variants: Vec<_> = target_enum.variants.iter().map(|v| &v.ident).collect();
    let source_path = get_source_enum_path(&input.attrs)?;

    let match_arms = target_variants.iter().map(|target_ident| {
        let source_ident = target_ident;
        quote! {
            #source_path::#source_ident => Self::#target_ident,
        }
    });

    let name = &input.ident;
    let (impl_generics, ty_generics, where_clause) = input.generics.split_for_impl();
    let expanded = quote! {
        impl #impl_generics From<#source_path> for #name #ty_generics #where_clause {
            fn from(value: #source_path) -> Self {
                match value {
                    #(#match_arms)*
                }
            }
        }
    };

    Ok(expanded.into())
}

fn get_source_enum_path(attrs: &[syn::Attribute]) -> syn::Result<Path> {
    for attr in attrs {
        if !attr.path().is_ident("enum_from") {
            continue;
        }
        let meta = attr.parse_args::<Meta>()?;
        return match meta {
            Meta::Path(path) => Ok(path),
            Meta::List(list) => {
                if list.tokens.is_empty() {
                    return Err(syn::Error::new_spanned(list, "expected one path argument"));
                }
                let path: Path = syn::parse2(list.tokens)?;
                Ok(path)
            }
            Meta::NameValue(_) => {
                Err(syn::Error::new_spanned(
                    meta,
                    "expected path, not name-value",
                ))
            }
        }
    }
    Err(syn::Error::new(
        Span::call_site(),
        "missing #[enum_from(SourceEnum)] attribute",
    ))
}
