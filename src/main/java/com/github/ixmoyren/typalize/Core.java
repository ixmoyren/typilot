package com.github.ixmoyren.typalize;

import com.novi.serde.DeserializationError;
import run.endive.runtime.ImportValues;
import run.endive.runtime.Instance;
import run.endive.wasi.WasiOptions;
import run.endive.wasi.WasiPreview1;

import java.nio.charset.StandardCharsets;

public final class Core implements AutoCloseable {
    private final Instance instance;
    private final WasiPreview1 wasi;
    private final Core_ModuleExports exports;

    private Core() {
        var wasiOpts = WasiOptions.builder().inheritSystem().build();
        this.wasi = WasiPreview1.builder().withOptions(wasiOpts).build();
        var imports = ImportValues.builder().addFunction(wasi.toHostFunctions()).build();
        this.instance = Instance.builder(TypalizeModule.load())
                .withImportValues(imports)
                .withMachineFactory(TypalizeModule::create)
                .build();
        this.exports = new Core_ModuleExports(instance);
    }

    TypalizeResult<String> version() {
        try {
            var resultPtr = exports.version();
            var result = unpackResult(resultPtr);
            return new TypalizeResult<>(new String(result, StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            return new TypalizeResult<>(e);
        }
    }

    TypalizeResult<Tokens> tokenize(byte[] text) {
        try {
            var textPtr = exports.wasmMalloc(text.length);
            exports.memory().write(textPtr, text);
            var resultPtr = exports.tokenize(textPtr, text.length);
            var result = unpackResult(resultPtr);
            return new TypalizeResult<>(Tokens.bcsDeserialize(result));
        } catch (RuntimeException | DeserializationError e) {
            return new TypalizeResult<>(e);
        }
    }

    TypalizeResult<ASTNodes> parse(byte[] text) {
        try {
            var textPtr = exports.wasmMalloc(text.length);
            exports.memory().write(textPtr, text);
            var resultPtr = exports.parse(textPtr, text.length);
            var result = unpackResult(resultPtr);
            return new TypalizeResult<>(ASTNodes.bcsDeserialize(result));
        } catch (RuntimeException | DeserializationError e) {
            return new TypalizeResult<>(e);
        }
    }

    private byte[] unpackResult(long packed) {
        var addr = (int) ((packed >>> 32) & 0xFFFFFFFFL);
        var len = (int) (packed & 0xFFFFFFFFL);
        if (addr < 0 || len < 0) {
            exports.wasmFree(addr);
            throw new IllegalArgumentException(
                    "Invalid WASM result: addr=" + addr + ", len=" + len);
        }
        try {
            return instance.memory().readBytes(addr, len);
        } finally {
            exports.wasmFree(addr);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public Typalize.Builder typalizer() {
        return new Typalize.Builder(this);
    }

    public static final class Builder {
        private Builder() {
        }

        public Core build() {
            return new Core();
        }
    }

    @Override
    public void close() {
        if (wasi != null) {
            wasi.close();
        }
    }
}
