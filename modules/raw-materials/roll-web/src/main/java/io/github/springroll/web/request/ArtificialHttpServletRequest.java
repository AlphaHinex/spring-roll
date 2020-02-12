package io.github.springroll.web.request;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.*;

/**
 * 人造的 {@link javax.servlet.http.HttpServletRequest} 实现
 *
 * 可用来根据需求构造一个 HttpServletRequest，作为入参传入方法中
 *
 * 仅实现了部分方法，借用自 org.springframework.mock.web.MockHttpServletRequest
 * 调用未实现方法会抛出异常
 */
public class ArtificialHttpServletRequest implements HttpServletRequest {

    private static final String CHARSET_PREFIX = "charset=";

    private transient String contextPath;
    private transient String servletPath;
    private transient String uri;
    private transient Map<String, String[]> params;
    private transient String method = "GET";
    private transient String contentType;
    private transient String characterEncoding;
    private transient byte[] content;
    private transient ServletInputStream inputStream;

    private final Map<String, HeaderValueHolder> headers = new LinkedCaseInsensitiveMap<>();
    private static final ServletInputStream EMPTY_SERVLET_INPUT_STREAM =
            new DelegatingServletInputStream(StreamUtils.emptyInput());

    public ArtificialHttpServletRequest(String contextPath, String servletPath, String uri, Map<String, String[]> params) {
        this.contextPath = contextPath;
        this.servletPath = servletPath;
        this.uri = uri;
        this.params = params;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public String getParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        String[] arr = this.params.get(name);
        return arr != null && arr.length > 0 ? arr[0] : null;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String[] getParameterValues(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        return this.params.get(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.params.keySet());
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                Charset charset = mediaType.getCharset();
                if (charset != null) {
                    this.characterEncoding = charset.name();
                }
            } catch (IllegalArgumentException ex) {
                // Try to get charset value anyway
                int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
                if (charsetIndex != -1) {
                    this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
                }
            }
            updateContentTypeHeader();
        }
    }

    private void updateContentTypeHeader() {
        if (StringUtils.hasLength(this.contentType)) {
            String value = this.contentType;
            if (StringUtils.hasLength(this.characterEncoding) && !this.contentType.toLowerCase().contains(CHARSET_PREFIX)) {
                value += ';' + CHARSET_PREFIX + this.characterEncoding;
            }
            doAddHeaderValue(value);
        }
    }

    private void doAddHeaderValue(Object value) {
        HeaderValueHolder header = new HeaderValueHolder();
        header.setValue(value);
        this.headers.put(HttpHeaders.CONTENT_TYPE, header);
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public int getContentLength() {
        return this.content != null ? this.content.length : -1;
    }

    @Override
    public long getContentLengthLong() {
        return getContentLength();
    }

    @Override
    public ServletInputStream getInputStream() {
        if (this.inputStream != null) {
            return this.inputStream;
        }

        this.inputStream = this.content != null
                ? new DelegatingServletInputStream(new ByteArrayInputStream(this.content))
                : EMPTY_SERVLET_INPUT_STREAM;
        return this.inputStream;
    }

    public void setContent(byte[] content) {
        this.content = content.clone();
        this.inputStream = null;
    }

    @Override
    public String getHeader(String name) {
        HeaderValueHolder header = this.headers.get(name);
        return header != null ? header.getStringValue() : null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        HeaderValueHolder header = this.headers.get(name);
        return Collections.enumeration(header != null ? header.getStringValues() : new LinkedList<>());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        updateContentTypeHeader();
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    // Below methods not implement

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return null;
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    @Deprecated
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

}