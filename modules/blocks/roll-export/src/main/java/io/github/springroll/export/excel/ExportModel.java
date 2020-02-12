package io.github.springroll.export.excel;

import lombok.Data;

import java.util.Map;

@Data
public class ExportModel {

    private String cols;
    private String url;
    private String total;
    private String tomcatUriEncoding;
    private Map bizReqBody;

}
