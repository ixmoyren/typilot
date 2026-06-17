package com.github.ixmoyren.typalize;

public class TypalizeResult<T> {
    private final boolean success;
    private final T result;
    private final Throwable error;

    TypalizeResult(T result) {
        this.success = true;
        this.result = result;
        this.error = null;
    }

    TypalizeResult(Throwable error) {
        this.success = false;
        this.result = null;
        this.error = error;
    }

    public T getResult() {
        return result;
    }

    public boolean success() {
        return success;
    }

    public boolean failure() {
        return !success;
    }

    public Throwable getError() {
        return error;
    }
}
