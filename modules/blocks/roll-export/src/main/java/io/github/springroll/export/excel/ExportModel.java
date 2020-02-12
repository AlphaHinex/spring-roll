package io.github.springroll.export.excel;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExportModel {

    private List<ColumnDef> cols;
    private String url;
    private String total;
    private String tomcatUriEncoding;
    private Map bizReqBody;

}
