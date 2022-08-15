package io.github.springroll.utils.http;

import io.github.springroll.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ReverseProxy extends ClientUtil {

    /*
     * 静态方法调用私有构造函数，以覆盖对构造函数的测试
     */
    static {
        new ReverseProxy();
    }

    private static final OkHttpClient CLIENT = new OkHttpClient();

    /**
     * 将请求转发至代理，并从代理获得响应
     *
     * @param  request     原始请求
     * @param  response    原始响应
     * @param  proxyPass   代理地址，类似 Nginx 中的 proxy_pass
     * @param  location    转发请求时，可将原始请求地址中与 location 值相同的部分抹去
     * @throws IOException 发生 IO 错误
     */
    public static void proxyPass(HttpServletRequest request, HttpServletResponse response, String proxyPass, String location) throws IOException {
        proxyPass(request, response, proxyPass, location, null);
    }

    /**
     * 将请求转发至代理，并从代理获得响应，支持添加请求头
     *
     * @param  request       原始请求
     * @param  response      原始响应
     * @param  proxyPass     代理地址，类似 Nginx 中的 proxy_pass
     * @param  location      转发请求时，可将原始请求地址中与 location 值相同的部分抹去
     * @param  customHeaders 需在代理时，向代理发送的自定义请求头，会与原始请求头合并
     * @throws IOException   发生 IO 错误
     */
    public static void proxyPass(HttpServletRequest request, HttpServletResponse response,
                                 String proxyPass, String location,
                                 Map<String, String> customHeaders) throws IOException {
        try (Response res = sendProxyReq(location, proxyPass, request, customHeaders)) {

            // Handle response status
            response.setStatus(res.code());

            // Handle response headers
            res.headers().toMultimap().forEach((key, value) -> value.forEach(v -> response.addHeader(key, v)));
            log.debug("Response headers from proxy target: {}", JsonUtil.toJson(res.headers().toMultimap()));

            // Handle response body, because this response is returned from Call.execute(), body() always returns a non-null value
            response.getOutputStream().write(Objects.requireNonNull(res.body()).bytes());
            response.flushBuffer();
        }
    }

    private static Response sendProxyReq(String location, String proxyPass, HttpServletRequest request,
                                         Map<String, String> customHeaders) throws IOException {
        StringBuilder url = new StringBuilder(proxyPass);

        url.append(request.getRequestURI()
                .replaceFirst(request.getContextPath(), "")
                .replace(StringUtils.isBlank(location) ? "" : location, ""));

        // Handle request parameters
        if (!request.getParameterMap().isEmpty()) {
            url.append("?");
            request.getParameterMap().forEach((key, value) -> {
                for (String v : value) {
                    url.append(key).append("=").append(v).append("&");
                }
            });
            url.deleteCharAt(url.length() - 1);
        }
        log.debug("Proxy pass to {}", url.toString().replaceAll("[\r\n]", ""));

        // Handle request headers
        Enumeration<String> headerEnum = request.getHeaderNames();
        Map<String, String> headers = new HashMap<>(16);
        while (headerEnum.hasMoreElements()) {
            String name = headerEnum.nextElement();
            headers.put(name, request.getHeader(name));
        }
        if (customHeaders != null && !customHeaders.isEmpty()) {
            headers.putAll(customHeaders);
        }
        log.debug("Request headers to proxy url: {}", JsonUtil.toJsonIgnoreException(headers));

        // Handle request content type
        MediaType type = null;
        String contentType = request.getContentType();
        if (StringUtils.isNotBlank(contentType)) {
            type = MediaType.parse(contentType);
        }

        // Handle request body
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        InputStream is = request.getInputStream();
        int l = is.read(buffer);
        while (l > 0) {
            os.write(buffer, 0, l);
            l = is.read(buffer);
        }

        // Send proxy request
        return createCall(CLIENT, url.toString(), request.getMethod(), headers, type, os.toByteArray()).execute();
    }

}
