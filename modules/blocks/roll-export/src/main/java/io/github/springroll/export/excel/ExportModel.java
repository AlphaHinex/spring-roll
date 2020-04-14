package io.github.springroll.export.excel;

import io.github.springroll.utils.StringUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Getter
@Setter
class ExportModel {

    @ApiModelProperty(value = "‍列表中对 columns 的定义", required = true)
    private List<ColumnDef> cols;

    @ApiModelProperty(value = "‍查询数据请求 url", required = true)
    private String url;

    @ApiModelProperty(value = "‍HTTP Method，默认为 GET，不区分大小写")
    private String method;

    @ApiModelProperty(value = "‍tomcat server.xml 中 Connector 设定的 URIEncoding 值，若未设置，默认为 ISO-8859-1")
    private String tomcatUriEncoding;

    @ApiModelProperty(value = "‍业务请求的请求体")
    private Map bizReqBody;

    String getMethod() {
        return StringUtil.isBlank(method) ? "GET" : method.toUpperCase(Locale.ENGLISH);
    }

}
