package io.github.springroll.export.excel;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.springroll.export.excel.handler.DecodeHandler;
import io.github.springroll.export.excel.handler.DefaultToStringDecodeHandler;
import io.github.springroll.export.excel.handler.PaginationHandler;
import io.github.springroll.utils.JsonUtil;
import io.github.springroll.utils.StringUtil;
import io.github.springroll.web.request.ArtificialHttpServletRequest;
import io.github.springroll.web.request.InvokeControllerByRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Controller
@Api(value = "/export/excel", tags = {"‍通用导出 excel"})
@RequestMapping("/export/excel")
@Slf4j
public class ExportExcelController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportExcelController.class);

    private static final String DEFAULT_ENCODING = "UTF8";
    private static final String PARAMS_TOKEN_START = "?";
    private static final String PARAMS_TOKEN_INTERVAL = "&";
    private static final String PARAMS_TOKEN_EQUATION = "=";
    private static final int PARAMS_PAIR_LEN = 2;

    private transient Collection<PaginationHandler> paginationHandlers;
    private transient Collection<DecodeHandler> decodeHandlers;
    private transient DefaultToStringDecodeHandler defaultToStringDecodeHandler;
    private transient InvokeControllerByRequest invokeControllerByRequest;
    private transient ExportExcelProperties properties;

    @Autowired
    public ExportExcelController(Collection<PaginationHandler> paginationHandlers, Collection<DecodeHandler> decodeHandlers,
                                 DefaultToStringDecodeHandler defaultToStringDecodeHandler,
                                 InvokeControllerByRequest invokeControllerByRequest, ExportExcelProperties properties) {
        this.paginationHandlers = paginationHandlers;
        this.decodeHandlers = decodeHandlers;
        this.defaultToStringDecodeHandler = defaultToStringDecodeHandler;
        this.invokeControllerByRequest = invokeControllerByRequest;
        this.properties = properties;
    }

    @ApiOperation("‍导出文件名为 title 参数值的 excel 文件")
    @PostMapping("/{title}")
    public void export(@ApiParam(value = "‍导出文件标题", required = true) @PathVariable String title,
                       @RequestBody ExportModel model,
                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        String decodedUrl = urlDecode(model.getUrl(), model.getTomcatUriEncoding());
        String cleanUrl = cleanUrl(decodedUrl);
        String contextPath = request.getContextPath();
        String servletPath = cleanUrl.replaceFirst(contextPath, "");

        ArtificialHttpServletRequest bizRequest = new ArtificialHttpServletRequest(contextPath, servletPath, cleanUrl);
        bizRequest.setMethod(model.getMethod());
        String reqEncoding = request.getCharacterEncoding();
        if (StringUtil.isBlank(reqEncoding)) {
            reqEncoding = DEFAULT_ENCODING;
        }
        bizRequest.setContent(JsonUtil.toJsonIgnoreException(model.getBizReqBody()).getBytes(reqEncoding));
        bizRequest.setContentType(request.getContentType());
        bizRequest.setParameters(parseParams(decodedUrl));

        export(title, model.getCols(), model.getTomcatUriEncoding(), response, bizRequest);
    }

    private void export(String title, List<ColumnDef> columnDefs, String tomcatUriEncoding,
                        HttpServletResponse response, HttpServletRequest bizReq) throws Exception {
        String decodedTitle = urlDecode(title, tomcatUriEncoding);
        List<List<String>> head = toHead(columnDefs);
        List<List<String>> data = toData(columnDefs, bizReq);
        outputToResponse(decodedTitle, response, head, data);
    }

    @ApiOperation(value = "‍导出文件名为 title 参数值的 excel 文件",
            notes = "‍根据查询请求 url，找到对应方法，查询出全部数据，导出到 excel 中。注意必填参数均需进行 URL Encode。")
    @GetMapping("/{title}")
    public void export(@ApiParam(value = "‍导出文件标题", required = true) @PathVariable String title,
                       @ApiParam(value = "‍列表中对 columns 的定义，JSON 格式表示，需进行 URL Encode", required = true) @RequestParam String cols,
                       @ApiParam(value = "‍查询数据请求 url，需进行 URL Encode", required = true) @RequestParam String url,
                       @ApiParam(value = "‍需匹配 Tomcat 中的 URIEncoding，以免乱码。缺省值为 UTF-8。"
                               + "‍独立运行的 Tomcat 默认 URIEncoding 为 ISO-8859-1，可在 server.xml 的 Connector 中进行设定。") String tomcatUriEncoding,
                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        String decodedUrl = urlDecode(url, tomcatUriEncoding);
        String cleanUrl = cleanUrl(decodedUrl);
        String contextPath = request.getContextPath();
        String servletPath = cleanUrl.replaceFirst(contextPath, "");

        ArtificialHttpServletRequest bizRequest = new ArtificialHttpServletRequest(contextPath, servletPath, cleanUrl);
        bizRequest.setParameters(parseParams(decodedUrl));

        String decodedCols = urlDecode(cols, tomcatUriEncoding);
        LOGGER.debug("Cols string after encoding is {}", decodedCols);

        List<ColumnDef> columnDefs = JsonUtil.parse(decodedCols, new TypeReference<List<ColumnDef>>() {});
        export(title, columnDefs, tomcatUriEncoding, response, bizRequest);
    }

    private String urlDecode(String str, String encoding) throws UnsupportedEncodingException {
        String decodedStr = StringUtil.isBlank(encoding) ? URLDecoder.decode(str, DEFAULT_ENCODING)
                : URLDecoder.decode(str, encoding);
        return isDefaultEncoding(encoding) || StringUtil.isBlank(encoding)
                ? decodedStr : new String(decodedStr.getBytes(encoding), DEFAULT_ENCODING);
    }

    private boolean isDefaultEncoding(String encoding) {
        return StringUtil.isNotBlank(encoding)
                && DEFAULT_ENCODING.equalsIgnoreCase(encoding.replace("-", ""));
    }

    private List<List<String>> toHead(List<ColumnDef> cols) {
        List<List<String>> result = new ArrayList<>();
        List<String> head;
        for (ColumnDef columnDef : cols) {
            if (columnDef.isHidden() || StringUtil.isBlank(columnDef.getName())) {
                continue;
            }
            head = new ArrayList<>(1);
            head.add(noNull(columnDef.getDisplay()));
            result.add(head);
        }
        return result;
    }

    private String noNull(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private List<List<String>> toData(List<ColumnDef> cols, HttpServletRequest request) throws Exception {
        List<List<String>> result = new ArrayList<>();
        Collection data = getPageData(request);
        List<String> row;
        Object value;
        Map<String, DecodeHandler> decodeHandlerMap = new HashMap<>(cols.size());
        for (Object rowData : data) {
            row = new ArrayList<>();
            for (ColumnDef columnDef : cols) {
                if (columnDef.isHidden() || StringUtil.isBlank(columnDef.getName())) {
                    continue;
                }
                if (rowData instanceof Map) {
                    value = ((Map) rowData).get(columnDef.getName());
                } else {
                    try {
                        value = new BeanWrapperImpl(rowData).getPropertyValue(columnDef.getName());
                    } catch (BeansException e) {
                        log.debug("Error occurs when get {} from {}", columnDef.getName(), rowData, e);
                        value = "Could NOT get value from " + rowData.getClass().getName();
                    }
                }
                if (columnDef.getDecoder() != null) {
                    String decoderKey = columnDef.getDecoder().getKey();
                    if (!decodeHandlerMap.containsKey(decoderKey)) {
                        decodeHandlerMap.put(decoderKey, findDecodeHandlerOrDefault(decoderKey));
                    }
                    DecodeHandler decodeHandler = decodeHandlerMap.get(decoderKey);
                    value = decodeHandler.decode(value, columnDef.getDecoder().getValue());
                }
                row.add(noNull(value));
            }
            result.add(row);
        }
        return result;
    }

    private DecodeHandler findDecodeHandlerOrDefault(String key) {
        for (DecodeHandler decodeHandler : decodeHandlers) {
            if (StringUtil.equals(key, decodeHandler.getDecoderKey())) {
                return decodeHandler;
            }
        }
        return defaultToStringDecodeHandler;
    }

    private Map<String, String[]> parseParams(String url) {
        Map<String, String[]> map = new HashMap<>(url.split(PARAMS_TOKEN_INTERVAL).length + 2);
        if (url.contains(PARAMS_TOKEN_START)) {
            String[] params = url.substring(url.indexOf(PARAMS_TOKEN_START) + 1).split(PARAMS_TOKEN_INTERVAL);
            String[] keyValue;
            for (String param : params) {
                keyValue = param.split(PARAMS_TOKEN_EQUATION);
                map.put(keyValue[0], keyValue.length == PARAMS_PAIR_LEN ? new String[]{keyValue[1]} : null);
            }
        }
        map.putIfAbsent(properties.getPageNumber(), new String[]{"1"});
        map.putIfAbsent(properties.getPageSize(), new String[]{properties.getMaxRows() + ""});
        return map;
    }

    private Collection getPageData(HttpServletRequest request) throws Exception {
        Object rawObject = invokeControllerByRequest.invoke(request);
        Assert.notNull(rawObject, "Could not get return value from " + request.getRequestURI());

        Optional<Collection> optional;
        for (PaginationHandler handler : paginationHandlers) {
            optional = handler.getPaginationData(rawObject);
            if (optional.isPresent()) {
                return optional.get();
            }
        }
        throw new RuntimeException("Could not find property PaginationHandler for " + rawObject.getClass().getName());
    }

    /**
     * 仅保留 url 中从 context path 到请求参数之前的部分，如：
     * 传入：http://localhost:8080/demo/foo/bar?v=2000
     * 返回：/demo/foo/bar
     *
     * @param  url URL
     * @return 处理之后的 url
     */
    private String cleanUrl(String url) {
        String cleanUrl = url.replaceFirst("https?://[^/]*/", "/");

        int len = cleanUrl.length();
        int startTokenIdx = cleanUrl.indexOf(PARAMS_TOKEN_START);
        if (startTokenIdx > 0) {
            len = startTokenIdx;
        }
        return cleanUrl.substring(0, len);
    }

    private void outputToResponse(String fileName, HttpServletResponse response, List<List<String>> head, List data) throws IOException {
        String exportFileName = URLEncoder.encode(fileName, DEFAULT_ENCODING);
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding(DEFAULT_ENCODING);
        response.setHeader("Content-Disposition", "attachment;filename=" + exportFileName + ".xlsx");

        EasyExcel.write(response.getOutputStream()).head(head).sheet(fileName).doWrite(data);
    }

}
