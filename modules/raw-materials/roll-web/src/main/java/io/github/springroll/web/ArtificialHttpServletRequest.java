package io.github.springroll.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
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
        return (arr != null && arr.length > 0 ? arr[0] : null);
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
                if (mediaType.getCharset() != null) {
                    this.characterEncoding = mediaType.getCharset().name();
                }
            }
            catch (IllegalArgumentException ex) {
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
            doAddHeaderValue(HttpHeaders.CONTENT_TYPE, value, true);
        }
    }

    private void doAddHeaderValue(String name, Object value, boolean replace) {
        HeaderValueHolder header = this.headers.get(name);
        Assert.notNull(value, "Header value must not be null");
        if (header == null || replace) {
            header = new HeaderValueHolder();
            this.headers.put(name, header);
        }
        if (value instanceof Collection) {
            header.addValues((Collection<?>) value);
        }
        else if (value.getClass().isArray()) {
            header.addValueArray(value);
        }
        else {
            header.addValue(value);
        }
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public int getContentLength() {
        return (this.content != null ? this.content.length : -1);
    }

    @Override
    public long getContentLengthLong() {
        return getContentLength();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.inputStream != null) {
            return this.inputStream;
        }

        this.inputStream = (this.content != null ?
                new DelegatingServletInputStream(new ByteArrayInputStream(this.content)) :
                EMPTY_SERVLET_INPUT_STREAM);
        return this.inputStream;
    }

    public void setContent(byte[] content) {
        this.content = content;
        this.inputStream = null;
    }

    @Override
    public String getHeader(String name) {
        HeaderValueHolder header = this.headers.get(name);
        return (header != null ? header.getStringValue() : null);
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
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

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

/**
 * Internal helper class that serves as value holder for request headers.
 *
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0.1
 */
class HeaderValueHolder {

    private final List<Object> values = new LinkedList<>();

    public void setValue(Object value) {
        this.values.clear();
        if (value != null) {
            this.values.add(value);
        }
    }

    void addValue(Object value) {
        this.values.add(value);
    }

    void addValues(Collection<?> values) {
        this.values.addAll(values);
    }

    void addValueArray(Object values) {
        CollectionUtils.mergeArrayIntoCollection(values, this.values);
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public List<String> getStringValues() {
        List<String> stringList = new ArrayList<>(this.values.size());
        for (Object value : this.values) {
            stringList.add(value.toString());
        }
        return Collections.unmodifiableList(stringList);
    }

    public Object getValue() {
        return (!this.values.isEmpty() ? this.values.get(0) : null);
    }

    public String getStringValue() {
        return (!this.values.isEmpty() ? String.valueOf(this.values.get(0)) : null);
    }

    @Override
    public String toString() {
        return this.values.toString();
    }

}


class DelegatingServletInputStream extends ServletInputStream {
    private final InputStream sourceStream;

    private boolean finished = false;

    /**
     * Create a DelegatingServletInputStream for the given source stream.
     * @param sourceStream the source stream (never {@code null})
     */
    DelegatingServletInputStream(InputStream sourceStream) {
        Assert.notNull(sourceStream, "Source InputStream must not be null");
        this.sourceStream = sourceStream;
    }

    /**
     * Return the underlying source stream (never {@code null}).
     */
    public final InputStream getSourceStream() {
        return this.sourceStream;
    }


    @Override
    public int read() throws IOException {
        int data = this.sourceStream.read();
        if (data == -1) {
            this.finished = true;
        }
        return data;
    }

    @Override
    public int available() throws IOException {
        return this.sourceStream.available();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.sourceStream.close();
    }

    @Override
    public boolean isFinished() {
        return this.finished;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
    }
}
