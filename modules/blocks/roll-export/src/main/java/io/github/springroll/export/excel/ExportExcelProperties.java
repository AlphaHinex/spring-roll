package io.github.springroll.export.excel;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "roll.export.excel")
@Getter
@Setter
public class ExportExcelProperties {

    /**
     * 默认在请求中添加一个代表当前页的参数，供分页查询使用，参数名默认为 pageNumber
     */
    private String pageNumber = "pageNumber";

    /**
     * 默认在请求中添加一个代表每页数据总数的参数，供分页查询使用，参数名默认为 pageSize
     */
    private String pageSize = "pageSize";

    /**
     * 日期类型解码器标识，默认为 date
     */
    private String dateDecoderKey = "date";

    /**
     * 通用导出功能导出的 Excel 最大行数，默认 5000，设置过大可能会导致导出时间过长或无响应
     */
    private int maxRows = 5000;

}
