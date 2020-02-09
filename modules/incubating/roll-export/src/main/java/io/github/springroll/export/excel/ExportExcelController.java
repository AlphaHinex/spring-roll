package io.github.springroll.export.excel;

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.springroll.base.CharacterEncoding;
import io.github.springroll.export.excel.handler.PaginationHandler;
import io.github.springroll.utils.JsonUtil;
import io.github.springroll.utils.StringUtil;
import io.github.springroll.web.ArtificialHttpServletRequest;
import io.github.springroll.web.HandlerHolder;
import org.apache.poi.hssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Controller
@RequestMapping("/export/excel")
public class ExportExcelController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportExcelController.class);

    private static final String DEFAULT_ENCODING = "UTF8";
    private static final String PARAMS_TOKEN_START = "?";
    private static final String PARAMS_TOKEN_INTERVAL = "&";
    private static final String PARAMS_TOKEN_EQUATION = "=";

    private static final int ROW_INDEX_TITLE = 0;
    private static final int ROW_INDEX_CONTENT = 1;

    private transient HandlerHolder handlerHolder;
    private transient Collection<PaginationHandler> paginationHandlers;

    @Autowired
    public ExportExcelController(HandlerHolder handlerHolder, Collection<PaginationHandler> paginationHandlers) {
        this.handlerHolder = handlerHolder;
        this.paginationHandlers = paginationHandlers;
    }

    /**
     * 根据查询请求 url，找到对应方法，查询出全部数据，导出到 excel 中
     * 注意必填参数均需进行 URL encode
     *
     * @param  title             导出文件标题
     * @param  cols              列表中对 columns 的定义，JSON 格式表示
     * @param  url               查询数据请求 url
     * @param  total             导出数据总数
     * @param  tomcatUriEncoding tomcat server.xml 中 Connector 设定的 URIEncoding 值，若未设置，默认为 ISO-8859-1
     * @param  request           请求对象
     * @param  response          响应对象
     * @throws Exception         导出过程中可能会出现的各种异常
     */
    @RequestMapping(method = RequestMethod.GET, value = "/all/{title}")
    public void exportAll(@PathVariable String title, @RequestParam String cols, @RequestParam String url,
                          String total, String tomcatUriEncoding,
                          HttpServletRequest request, HttpServletResponse response) throws Exception {
        String decodedTitle = decode(title, tomcatUriEncoding);
        String decodedCols = decode(cols, tomcatUriEncoding);
        String decodedUrl = decode(url, tomcatUriEncoding);
        LOGGER.debug("Cols string after encoding is {}", decodedCols);

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(decodedTitle);

        List<ColumnDef> columnDefs = JsonUtil.parse(decodedCols, new TypeReference<List<ColumnDef>>() {});
        writeTitle(sheet, columnDefs);
        writeContent(request.getContextPath(), decodedUrl, total, columnDefs, sheet);
        outputToResponse(decodedTitle, response, wb);
    }

    private String decode(String str, String encoding) throws UnsupportedEncodingException {
        String decodedStr = StringUtil.isBlank(encoding) ? URLDecoder.decode(str, CharacterEncoding.DEFAULT_ENCODE_NAME)
                : URLDecoder.decode(str, encoding);
        return isDefaultEncoding(encoding) || StringUtil.isBlank(encoding)
                ? decodedStr : new String(decodedStr.getBytes(encoding), DEFAULT_ENCODING);
    }

    private boolean isDefaultEncoding(String encoding) {
        return StringUtil.isNotBlank(encoding)
                && DEFAULT_ENCODING.equalsIgnoreCase(encoding.replace("-", ""));
    }

    private void writeTitle(HSSFSheet sheet, List<ColumnDef> cols) {
        HSSFRow row = sheet.getRow(ROW_INDEX_TITLE);
        if (null == row) {
            row = sheet.createRow((short) ROW_INDEX_TITLE);
        }
        int col = 0;
        HSSFCell cell;
        for (ColumnDef columnDef : cols) {
            if (columnDef.isHidden()) {
                continue;
            }
            cell = row.createCell(col++);
            cell.setCellValue(new HSSFRichTextString(noNull(columnDef.getDisplay())));
        }
    }

    private String noNull(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    private void writeContent(String contextPath, String url, String total, List<ColumnDef> cols, HSSFSheet sheet) throws Exception {
        int rowNum = ROW_INDEX_CONTENT;
        Collection result = getPageData(contextPath, url, total);
        HSSFRow row;
        String content;
        for (Object rowData : result) {
            row = sheet.createRow((short) (rowNum++));
            int col = 0;
            for (ColumnDef columnDef : cols) {
                if (columnDef.isHidden()) {
                    continue;
                }
                HSSFCell cell = row.createCell(col++);
                Assert.hasText(columnDef.getName(), "Property 'name' or 'filed' in cols string MUST NOT NULL!");
                if (rowData instanceof Map) {
                    content = noNull(((Map) rowData).get(columnDef.getName()));
                } else {
                    content = noNull(new BeanWrapperImpl(rowData).getPropertyValue(columnDef.getName()));
                }
                cell.setCellValue(new HSSFRichTextString(content));
            }
        }
    }

    private Collection getPageData(String contextPath, String url, String total) throws Exception {
        Map<String, String[]> params = new HashMap<>(16);
        params.put("page", new String[] {"1"});
        params.put("rows", new String[] {total});
        params.putAll(parseParams(url));

        String cleanUrl = cleanUri(url, contextPath);

        String servletPath = cleanUrl.replaceFirst(contextPath, "");

        HttpServletRequest request = new ArtificialHttpServletRequest(contextPath, servletPath, cleanUrl, params);
        HandlerMethod handlerMethod = handlerHolder.getHandler(request);
        Method method = handlerMethod.getMethod();
        Object rawObject = method.invoke(handlerMethod.getBean(), buildParamForMethod(handlerMethod.getMethodParameters(), request));

        Optional<Collection> optional;
        for (PaginationHandler handler : paginationHandlers) {
            optional = handler.getPaginationData(rawObject);
            if (optional.isPresent()) {
                return optional.get();
            }
        }
        String resultType = rawObject.getClass().getName();
        throw new RuntimeException("Could not find property PaginationHandler for " + resultType);
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
            if (keyValue.length != 2) {
                continue;
            }
            map.put(keyValue[0], new String[] {keyValue[1]});
        }
        return map;
    }

    private String cleanUri(String url, String contextPath) {
        int len = url.length();
        int startTokenIdx = url.indexOf(PARAMS_TOKEN_START);
        if (startTokenIdx > 0) {
            len = startTokenIdx;
        }
        return url.substring(url.indexOf(contextPath), len).replaceAll("//", "/");
    }

    private Object[] buildParamForMethod(MethodParameter[] methodParameters, HttpServletRequest request) {
        if (methodParameters == null) {
            return null;
        }
        Object[] params = new Object[methodParameters.length];
        for (int i = 0; i < methodParameters.length; i++) {
            if ("javax.servlet.http.HttpServletRequest".equals(methodParameters[i].getParameterType().getName())) {
                params[i] = request;
            }
        }
        return params;
    }

    private void outputToResponse(String fileName, HttpServletResponse response, HSSFWorkbook wb) throws UnsupportedEncodingException {
        String exportFileName = URLEncoder.encode(fileName, DEFAULT_ENCODING);
        response.setContentType("application/x-msdownload;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + exportFileName + ".xls");

        OutputStream os = null;
        try {
            os = response.getOutputStream();
            wb.write(os);
        } catch (IOException e) {
            LOGGER.error("Exception occurs when writing excel data!", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    LOGGER.error("Exception occurs when close output stream!", e);
                }
            }
        }
    }

}
