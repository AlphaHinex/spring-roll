package io.github.springroll.export.excel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
