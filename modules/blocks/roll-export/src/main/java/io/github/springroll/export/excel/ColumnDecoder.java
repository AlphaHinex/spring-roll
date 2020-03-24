package io.github.springroll.export.excel;

import lombok.Data;

@Data
class ColumnDecoder {
    /**
     * Value in data
     */
    private String value;
    /**
     * Name of value to display
     */
    private String name;
}
