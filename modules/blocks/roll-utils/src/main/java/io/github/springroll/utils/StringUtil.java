package io.github.springroll.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class StringUtil extends StringUtils {

    /**
     * 私有化工具类的构造函数，避免对工具类的实例化
     */
    private StringUtil() {
    }

    public static String camelToSnake(String camel) {
        String[] strings = StringUtil.splitByCharacterTypeCamelCase(camel);
        return StringUtil.join(strings, "_").toLowerCase(Locale.ENGLISH);
    }

}
