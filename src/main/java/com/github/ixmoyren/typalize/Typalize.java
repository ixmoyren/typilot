package com.github.ixmoyren.typalize;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

public final class Typalize {
    private final Core core;

    private Typalize(Core core) {
        this.core = core;
    }

    public TypalizeResult<String> version() {
        return core.version();
    }

    public TypalizeResult<Tokens> tokenize(String text) {
        return core.tokenize(text.getBytes(StandardCharsets.UTF_8));
    }

    public TypalizeResult<Tokens> tokenize(CharSequence cs) {
        try {
            var bytes = encodeToBytes(cs);
            return core.tokenize(bytes);
        } catch (CharacterCodingException e) {
            return new TypalizeResult<>(e);
        }
    }

    public TypalizeResult<ASTNodes> parse(String text) {
        return core.parse(text.getBytes(StandardCharsets.UTF_8));
    }

    public TypalizeResult<ASTNodes> parse(CharSequence cs) {
        try {
            var bytes = encodeToBytes(cs);
            return core.parse(bytes);
        } catch (CharacterCodingException e) {
            return new TypalizeResult<>(e);
        }
    }

    private static byte[] encodeToBytes(CharSequence cs) throws CharacterCodingException {
        var encoder = StandardCharsets.UTF_8.newEncoder();
        var buffer = encoder.encode(CharBuffer.wrap(cs));
        var bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    public static final class Builder {
        private final Core core;

        Builder(Core core) {
            this.core = core;
        }

        public Typalize build() {
            return new Typalize(core);
        }
    }
}
