package io.github.springroll.export.excel.handler;

public interface DecodeHandler {

    /**
     * Get the unique key of decoder represented by this handler
     *
     * @return unique key
     */
    String getDecoderKey();

    /**
     * Decode obj to string with the content of decoderValue
     *
     * @param  obj object to be decoded
     * @param  decoderValue necessary content used by this handler
     * @return decoded string
     */
    String decode(Object obj, String decoderValue);

}
