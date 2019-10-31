package io.github.springroll.utils.cipher;

public class Aes {

    private transient CipherUtil util;

    /**
     * 私有化工具类的构造函数，避免对工具类的实例化
     */
    private Aes() { }

    public Aes(String mode, String padding, String key) {
        util = CipherUtil.newInstance("AES", mode, padding, key);
    }

    public byte[] encrypt(byte[] data) throws Exception {
        return util.encrypt(data);
    }

    public byte[] decrypt(byte[] data) throws Exception {
        return util.decrypt(data);
    }

}
