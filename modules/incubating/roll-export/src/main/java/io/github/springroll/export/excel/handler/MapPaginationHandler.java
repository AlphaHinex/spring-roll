package io.github.springroll.export.excel.handler;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MapPaginationHandler implements PaginationHandler {

    private static final String ROWS_KEY = "rows";

    @Override
    public Optional<List> getPaginationData(Object rawObject) {
        Optional<List> result = Optional.empty();
        if (rawObject instanceof Map && ((Map) rawObject).containsKey(ROWS_KEY)) {
            result = Optional.ofNullable((List)((Map) rawObject).get(ROWS_KEY));
        }
        return result;
    }

}
