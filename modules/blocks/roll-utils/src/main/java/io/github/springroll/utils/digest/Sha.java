package io.github.springroll.utils.digest;

import org.apache.commons.codec.digest.DigestUtils;

public class Sha {

    /**
     * 私有化工具类的构造函数，避免对工具类的实例化
     */
    private Sha() { }

    public static String sha256(String content) {
        return DigestUtils.sha256Hex(content);
    }

}
