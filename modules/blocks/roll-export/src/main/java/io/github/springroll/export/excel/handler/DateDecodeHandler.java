package io.github.springroll.export.excel.handler;

import io.github.springroll.export.excel.ExportExcelProperties;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Date;

@Component
public class DateDecodeHandler implements DecodeHandler {

    private transient ExportExcelProperties properties;

    @Autowired
    public DateDecodeHandler(ExportExcelProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getDecoderKey() {
        return properties.getDateDecoderKey();
    }

    @Override
    public String decode(Object obj, String dateTimePattern) {
        Assert.isInstanceOf(Date.class, obj, "DateDecodeHandler COULD ONLY decode java.util.Date type value!");
        return DateFormatUtils.format((Date) obj, dateTimePattern);
    }

}
