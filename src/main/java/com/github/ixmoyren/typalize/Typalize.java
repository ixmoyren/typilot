package com.github.ixmoyren.typalize;

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

    public TypalizeResult<ASTNodes> parse(String text) {
        return core.parse(text.getBytes(StandardCharsets.UTF_8));
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
