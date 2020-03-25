package io.github.springroll.export.excel;

import com.alibaba.excel.EasyExcel;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.springroll.base.CharacterEncoding;
import io.github.springroll.export.excel.handler.PaginationHandler;
import io.github.springroll.utils.JsonUtil;
import io.github.springroll.utils.StringUtil;
import io.github.springroll.web.request.ArtificialHttpServletRequest;
import io.github.springroll.web.request.InvokeControllerByRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
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
public class ExportExcelController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportExcelController.class);

    private static final String DEFAULT_ENCODING = "UTF8";
    private static final String PARAMS_TOKEN_START = "?";
    private static final String PARAMS_TOKEN_INTERVAL = "&";
    private static final String PARAMS_TOKEN_EQUATION = "=";
    private static final int PARAMS_PAIR_LEN = 2;

    private transient Collection<PaginationHandler> paginationHandlers;
    private transient InvokeControllerByRequest invokeControllerByRequest;

    @Autowired
    public ExportExcelController(Collection<PaginationHandler> paginationHandlers, InvokeControllerByRequest invokeControllerByRequest) {
        this.paginationHandlers = paginationHandlers;
        this.invokeControllerByRequest = invokeControllerByRequest;
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
        bizRequest.setMethod(HttpMethod.POST.name());
        bizRequest.setContent(JsonUtil.toJsonIgnoreException(model.getBizReqBody()).getBytes(request.getCharacterEncoding()));
        bizRequest.setContentType(request.getContentType());

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
            notes = "‍根据查询请求 url，找到对应方法，查询出全部数据，导出到 excel 中。注意必填参数均需进行 URL encode。")
    @GetMapping("/{title}")
    public void export(@ApiParam(value = "‍导出文件标题", required = true) @PathVariable String title,
                       @ApiParam(value = "‍列表中对 columns 的定义，JSON 格式表示", required = true) @RequestParam String cols,
                       @ApiParam(value = "‍查询数据请求 url", required = true) @RequestParam String url,
                       @ApiParam(value = "‍tomcat server.xml 中 Connector 设定的 URIEncoding 值，若未设置，默认为 ISO-8859-1") String tomcatUriEncoding,
                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        String decodedUrl = urlDecode(url, tomcatUriEncoding);
        String cleanUrl = cleanUrl(decodedUrl);
        String contextPath = request.getContextPath();
        String servletPath = cleanUrl.replaceFirst(contextPath, "");

        Map<String, String[]> params = parseParams(decodedUrl);
        ArtificialHttpServletRequest bizRequest = new ArtificialHttpServletRequest(contextPath, servletPath, cleanUrl);
        bizRequest.setParameters(params);

        String decodedCols = urlDecode(cols, tomcatUriEncoding);
        LOGGER.debug("Cols string after encoding is {}", decodedCols);

        List<ColumnDef> columnDefs = JsonUtil.parse(decodedCols, new TypeReference<List<ColumnDef>>() {});
        export(title, columnDefs, tomcatUriEncoding, response, bizRequest);
    }

    private String urlDecode(String str, String encoding) throws UnsupportedEncodingException {
        String decodedStr = StringUtil.isBlank(encoding) ? URLDecoder.decode(str, CharacterEncoding.DEFAULT_ENCODE_NAME)
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
        String value;
        Map<String, Map<String, String>> decoderMap = new HashMap<>(cols.size());
        for (Object rowData : data) {
            row = new ArrayList<>();
            for (ColumnDef columnDef : cols) {
                if (columnDef.isHidden() || StringUtil.isBlank(columnDef.getName())) {
                    continue;
                }
                if (rowData instanceof Map) {
                    value = ((Map) rowData).get(columnDef.getName()) + "";
                } else {
                    try {
                        value = noNull(new BeanWrapperImpl(rowData).getPropertyValue(columnDef.getName()));
                    } catch (BeansException e) {
                        value = "Could NOT get value from " + rowData.getClass().getName();
                    }
                }
                if (CollectionUtils.isNotEmpty(columnDef.getDecoder())) {
                    if (!decoderMap.containsKey(columnDef.getName())) {
                        decoderMap.put(columnDef.getName(), columnDef.getDecoderMap());
                    }
                    value = decoderMap.get(columnDef.getName()).getOrDefault(value, value);
                }
                row.add(value);
            }
            result.add(row);
        }
        return result;
    }

    private Map<String, String[]> parseParams(String url) {
        if (!url.contains(PARAMS_TOKEN_START)) {
            return Collections.emptyMap();
        }

        String[] params = url.substring(url.indexOf(PARAMS_TOKEN_START) + 1).split(PARAMS_TOKEN_INTERVAL);
        Map<String, String[]> map = new HashMap<>(params.length);
        String[] keyValue;
        for (String param : params) {
            keyValue = param.split(PARAMS_TOKEN_EQUATION);
            map.put(keyValue[0], keyValue.length == PARAMS_PAIR_LEN ? new String[] {keyValue[1]} : null);
        }
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
     * @param  url         URL
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
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + exportFileName + ".xlsx");

        EasyExcel.write(response.getOutputStream()).head(head).sheet(fileName).doWrite(data);
    }

}
