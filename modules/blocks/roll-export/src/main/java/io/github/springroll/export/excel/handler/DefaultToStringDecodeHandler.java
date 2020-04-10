package io.github.springroll.export.excel.handler;

import org.springframework.stereotype.Component;

@Component
public class DefaultToStringDecodeHandler implements DecodeHandler {

    @Override
    public String getDecoderKey() {
        return "default_to_string";
    }

    @Override
    public String decode(Object obj, String decoderValue) {
        return obj == null ? "" : obj.toString();
    }

}
