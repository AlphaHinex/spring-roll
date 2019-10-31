package io.github.springroll;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharacterEncoding {

    private CharacterEncoding() { }

    private static Charset charset = StandardCharsets.UTF_8;

    public static Charset getCharset() {
        return charset;
    }

    public static void setCharset(Charset charset) {
        CharacterEncoding.charset = charset;
    }

}
