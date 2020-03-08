package io.github.springroll.export.excel;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ExportModel {

    /**
     * 列表中对 columns 的定义
     */
    private List<ColumnDef> cols;
    /**
     * 查询数据请求 url
     */
    private String url;
    /**
     * tomcat server.xml 中 Connector 设定的 URIEncoding 值，若未设置，默认为 ISO-8859-1
     */
    private String tomcatUriEncoding;
    /**
     * 业务请求的 Request Body
     */
    private Map bizReqBody;

}
