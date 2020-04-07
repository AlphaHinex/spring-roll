package io.github.springroll.export.excel.handler;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class DefaultToStringDecodeHandler implements DecodeHandler {

    @Override
    public String getDecoderKey() {
        return "default_to_string";
    }

    @Override
    public String decode(Object obj, String decoderValue) {
        Assert.notNull(obj, "The object to be decode MUST NOT NULL!");
        return obj.toString();
    }

}
