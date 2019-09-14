package io.github.springroll.utils;

public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 私有化工具类的构造函数，避免对工具类的实例化
     */
    private StringUtils() {
    }

    /*
     * 静态方法调用私有构造函数，以覆盖对构造函数的测试
     */
    static {
        new StringUtils();
    }

    public static String camelToSnake(String camel) {
        String[] strings = StringUtils.splitByCharacterTypeCamelCase(camel);
        return StringUtils.join(strings, "_").toLowerCase();
    }

}
