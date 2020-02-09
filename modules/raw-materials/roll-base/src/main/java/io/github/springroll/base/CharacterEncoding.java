package io.github.springroll.base;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class CharacterEncoding {

    /**
     * The platform default encoding
     */
    public static final String DEFAULT_ENCODE_NAME = System.getProperty("file.encoding");

    private CharacterEncoding() { }

    private static Charset charset = StandardCharsets.UTF_8;

    public static Charset getCharset() {
        return charset;
    }

    public static void setCharset(Charset charset) {
        CharacterEncoding.charset = charset;
    }

}
