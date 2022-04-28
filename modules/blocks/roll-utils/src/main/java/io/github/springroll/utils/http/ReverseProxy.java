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

@Slf4j
public class ReverseProxy extends ClientUtil {

    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static void proxyPass(HttpServletRequest request, HttpServletResponse response, String proxyPass, String location) throws IOException {
        proxyPass(request, response, proxyPass, location, null);
    }

    public static void proxyPass(HttpServletRequest request, HttpServletResponse response,
                                 String proxyPass, String location,
                                 Map<String, String> customHeaders) throws IOException {
        Response res = sendProxyReq(location, proxyPass, request, customHeaders);

        // Handle response status
        response.setStatus(res.code());

        // Handle response headers
        res.headers().toMultimap().forEach((key, value) -> value.forEach(v -> response.addHeader(key, v)));
        log.debug("Response headers from proxy target: {}", JsonUtil.toJson(res.headers().toMultimap()));

        // Handle response body
        if (res.body() != null) {
            response.getOutputStream().write(res.body().bytes());
            response.flushBuffer();
        }
    }

    private static Response sendProxyReq(String location, String proxyPass, HttpServletRequest request,
                                         Map<String, String> customHeaders) throws IOException {
        StringBuilder url = new StringBuilder(proxyPass);

        url.append(request.getRequestURI()
                .replace(request.getContextPath(), "")
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
        if (customHeaders != null && customHeaders.size() > 0) {
            headers.putAll(customHeaders);
        }
        log.debug("Request headers to proxy url: {}", JsonUtil.toJson(headers));

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
