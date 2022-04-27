package io.github.springroll.utils.http;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 客户端工具类
 */
public class HttpClient extends ClientUtil {

    private static OkHttpClient client = new OkHttpClient();

    /**
     * 私有化工具类的构造函数，避免对工具类的实例化
     */
    protected HttpClient() { }

    /*
     * 静态方法调用私有构造函数，以覆盖对构造函数的测试
     */
    static {
        new HttpClient();
    }

    public static Response get(String url) throws IOException {
        return perform(client, url, GET, null, null, null);
    }

    public static void get(String url, Callback callback) {
        perform(client, url, GET, null, null, null, callback);
    }

    public static void get(String url, long connectionTimeout, Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS).build();
        perform(client, url, GET, null, null, null, callback);
    }

    public static Response get(String url, int timeout) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(timeout, TimeUnit.MILLISECONDS).build();
        return perform(client, url, GET, null, null, null);
    }

    public static Response get(String url, Map<String, String> headers) throws IOException {
        return perform(client, url, GET, headers, null, null);
    }

    public static Response get(String url, int timeout, Map<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(timeout, TimeUnit.MILLISECONDS).build();
        return perform(client, url, GET, headers, null, null);
    }

    public static Response post(String url, MediaType type, String data) throws IOException {
        return perform(client, url, POST, null, type, data);
    }

    public static Response post(String url, MediaType type, String data, int timeout) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().readTimeout(timeout, TimeUnit.MILLISECONDS).build();
        return perform(client, url, POST, null, type, data);
    }

    public static void post(String url, MediaType type, String data, Callback callback) {
        perform(client, url, POST, null, type, data, callback);
    }

    public static void post(String url, MediaType type, String data, long connectionTimeout, Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(connectionTimeout, TimeUnit.MILLISECONDS).build();
        perform(client, url, POST, null, type, data, callback);
    }

    public static Response post(String url, Map<String, String> headers, MediaType type, String data) throws IOException {
        return perform(client, url, POST, headers, type, data);
    }

    public static Response put(String url, MediaType type, String data) throws IOException {
        return perform(client, url, PUT, null, type, data);
    }

    public static Response put(String url, Map<String, String> headers, MediaType type, String data) throws IOException {
        return perform(client, url, PUT, headers, type, data);
    }

    public static Response delete(String url) throws IOException {
        return perform(client, url, DELETE, null, null, null);
    }

    public static Response delete(String url, Map<String, String> headers) throws IOException {
        return perform(client, url, DELETE, headers, null, null);
    }

    public static Response delete(String url, MediaType type, String data) throws IOException {
        return perform(client, url, DELETE, null, type, data);
    }

    public static Response delete(String url, Map<String, String> headers, MediaType type, String data) throws IOException {
        return perform(client, url, DELETE, headers, type, data);
    }

}
