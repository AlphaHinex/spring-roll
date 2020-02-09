package io.github.springroll.export.excel.handler;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Component
public class MapPaginationHandler implements PaginationHandler {

    private static final String ROWS_KEY = "rows";

    @Override
    public Optional<Collection> getPaginationData(Object rawObject) {
        Optional<Collection> result = Optional.empty();
        if (rawObject instanceof Map && ((Map) rawObject).containsKey(ROWS_KEY)) {
            result = Optional.ofNullable((Collection)((Map) rawObject).get(ROWS_KEY));
        }
        return result;
    }

}
