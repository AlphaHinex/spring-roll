package io.github.springroll.utils.cipher;

import org.apache.commons.codec.binary.Base64;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

public class Rsa {

    private transient CipherUtil util;

    /**
     * 私有化工具类的构造函数，避免对工具类的实例化
     */
    private Rsa() { }

    public Rsa(String signAlgorithm) {
        util = CipherUtil.newInstance("RSA", signAlgorithm);
    }

    public Map<String, String> generateKeyPair(int keySize) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Map<String, String> map = util.generateKeyPair(keySize);
        RSAPublicKey rsp = (RSAPublicKey) util.getPublicKey(map.get("publicKey"));
        BigInteger bigInt = rsp.getModulus();
        map.put("modulus", Base64.encodeBase64String(bigInt.toByteArray()));
        return map;
    }

    public byte[] encrypt(byte[] data, String publicKey) throws Exception {
        return util.encrypt(data, publicKey);
    }

    public byte[] decrypt(byte[] data, String privateKey) throws Exception {
        return util.decrypt(data, privateKey);
    }

    public String sign(String content, String privateKey) throws Exception {
        return util.sign(content, privateKey);
    }

    public boolean verifySign(String content, String sign, String publicKey) throws Exception {
        return util.verifySign(content, sign, publicKey);
    }

}
