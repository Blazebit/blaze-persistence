package com.blazebit.persistence.impl.util;

public abstract class AbstractPatternFinder implements PatternFinder {

    @Override
    public int indexIn(CharSequence text) {
        return indexIn(text, 0, text.length());
    }

    @Override
    public int indexIn(CharSequence text, int start) {
        return indexIn(text, start, text.length());
    }

    @Override
    public int indexIn(char[] text) {
        return indexIn(text, 0, text.length);
    }

    @Override
    public int indexIn(char[] text, int start) {
        return indexIn(text, start, text.length);
    }
}
