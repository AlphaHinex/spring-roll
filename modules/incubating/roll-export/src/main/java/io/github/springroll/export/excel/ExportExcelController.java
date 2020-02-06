package io.github.springroll.export.excel;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/export/excel")
public class ExportExcelController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExportExcelController.class);

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String PARAMS_TOKEN_START = "?";
    private static final String PARAMS_TOKEN_INTERVAL = "&";
    private static final String PARAMS_TOKEN_EQUATION = "=";

    private static final int ROW_INDEX_TITLE = 0;
    private static final int ROW_INDEX_CONTENT = 1;

    private HandlerHolder handlerHolder;

    @Autowired
    public ExportExcelController(HandlerHolder handlerHolder) {
        this.handlerHolder = handlerHolder;
    }

    /**
     * 根据查询请求 url，找到对应方法，查询出全部数据，导出到 excel 中
     *
     * @param  cols              列表中对 columns 的定义
     * @param  url               查询数据请求 url
     * @param  total             导出数据总数
     * @param  title             导出文件标题
     * @param  tomcatUriEncoding tomcat server.xml 中 Connector 设定的 URIEncoding 值，若未设置，默认为 ISO-8859-1
     * @param  request           请求对象
     * @param  response          响应对象
     * @throws Exception         导出过程中可能会出现的各种异常
     */
    @RequestMapping(method = RequestMethod.GET, value = "/all")
    public void exportAll(String cols, String url, String total, String title, String tomcatUriEncoding,
                          HttpServletRequest request, HttpServletResponse response) throws Exception {
        cols = handleEncoding(cols, tomcatUriEncoding);
        url = handleEncoding(url, tomcatUriEncoding);
        title = handleEncoding(title, tomcatUriEncoding);
        LOGGER.debug("Cols string after encoding is {}", cols);

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet(title);

        List<ColumnDef> columnDefs = JsonUtil.parse(cols, new TypeReference<List<ColumnDef>>() {});
        writeTitle(sheet, columnDefs);
        writeContent(request.getContextPath(), url, total, columnDefs, sheet);
        outputToResponse(title, response, wb);
    }

    private String handleEncoding(String str, String encoding) throws UnsupportedEncodingException {
        return DEFAULT_ENCODING.equals(encoding) || StringUtil.isBlank(encoding) ?
                str : new String(str.getBytes(encoding), DEFAULT_ENCODING);
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
        List result = getPageData(contextPath, url, total);
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

    private List getPageData(String contextPath, String url, String total) throws Exception {
        Map<String, String[]> params = new HashMap<>(2);
        params.put("page", new String[] {"1"});
        params.put("rows", new String[] {total});
        params.putAll(parseParams(url));

        url = cleanUri(url, contextPath);

        String servletPath = url.replaceFirst(contextPath, "");

        HttpServletRequest request = new ArtificialHttpServletRequest(contextPath, servletPath, url, params);
        HandlerMethod handlerMethod = handlerHolder.getHandler(request);
        Method method = handlerMethod.getMethod();
        Object result = method.invoke(handlerMethod.getBean(), buildParamForMethod(handlerMethod.getMethodParameters(), request));

        String rowsKey = "rows";
        if (result instanceof Page) {
            return ((Page) result).getRows();
        } else if (result instanceof Map && ((Map) result).containsKey(rowsKey)) {
            return (List) ((Map) result).get(rowsKey);
        } else {
            String resultType = result.getClass().getName();
            LOGGER.error("Not support query data type: {}", resultType);
            throw new RuntimeException("Only support Page and Map type, but here has " + resultType);
        }
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
            params[i] = "javax.servlet.http.HttpServletRequest".equals(methodParameters[i].getParameterType().getName()) ? request : null;
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
