package io.github.springroll.export.excel;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ExportModel {

    private List<ColumnDef> cols;
    private String url;
    private String total;
    private String tomcatUriEncoding;
    private Map bizReqBody;

}
