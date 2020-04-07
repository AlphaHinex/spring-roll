package io.github.springroll.export.excel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class ColumnDecoder {

    /**
     * Unique key of the decoder.
     */
    private String key;
    /**
     * Concrete value of the decoder,
     * maybe code type or date format etc.
     */
    private String value;

}
