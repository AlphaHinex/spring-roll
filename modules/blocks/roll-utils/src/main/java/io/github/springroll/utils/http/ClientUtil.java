package io.github.springroll.utils.http;

import io.github.springroll.utils.StringUtil;
import okhttp3.*;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ClientUtil {

    protected static final String PUT = "PUT";
    protected static final String GET = "GET";
    protected static final String POST = "POST";
    protected static final String DELETE = "DELETE";
    protected static final String PATCH = "PATCH";

    protected static Response perform(OkHttpClient client,
                                                    String url, String method,
                                                    Map<String, String> headers, MediaType type,
                                                    String data) throws IOException {
        return createCall(client, url, method, headers, type, data).execute();
    }

    /**
     * 异步发送请求，需要回调方法
     *
     * @param client    http 客户端
     * @param url       请求 url
     * @param method    请求方法
     * @param headers   请求头
     * @param type      media type
     * @param data      数据
     * @param callback  回调类
     */
    protected static void perform(OkHttpClient client,
                                  String url, String method,
                                  Map<String, String> headers, MediaType type,
                                  String data, final Callback callback) {
        createCall(client, url, method, headers, type, data).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onSuccess(response);
            }
        });
    }

    protected static Call createCall(OkHttpClient client,
                                     String url, String method,
                                     Map<String, String> headers, MediaType type,
                                     String data) {
        return createCall(client, url, method, headers, type,
                StringUtil.isNotBlank(data) ? data.getBytes(StandardCharsets.UTF_8) : new byte[0]);
    }

    protected static Call createCall(OkHttpClient client,
                                     String url, String method,
                                     Map<String, String> headers, MediaType type,
                                     byte[] data) {
        Request.Builder builder = new Request.Builder();
        builder = builder.url(url);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder = builder.addHeader(header.getKey(), header.getValue());
            }
        }
        RequestBody body = null;
        if (ArrayUtils.isNotEmpty(data)) {
            body = RequestBody.create(type, data);
        }
        builder = builder.method(method, body);
        Request request = builder.build();
        return client.newCall(request);
    }

}
