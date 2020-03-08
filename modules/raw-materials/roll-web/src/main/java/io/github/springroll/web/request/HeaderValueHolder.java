package io.github.springroll.web.request;

import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Borrow from org.springframework.mock.web.HeaderValueHolder in spring-test
 *
 * Internal helper class that serves as value holder for request headers.
 *
 * @author Juergen Hoeller
 * @author Rick Evans
 * @since 2.0.1
 */
class HeaderValueHolder {

    private final List<Object> values = new LinkedList<>();

    public void setValue(Object value) {
        this.values.clear();
        if (value != null) {
            this.values.add(value);
        }
    }

    void addValue(Object value) {
        this.values.add(value);
    }

    void addValues(Collection<?> values) {
        this.values.addAll(values);
    }

    void addValueArray(Object values) {
        CollectionUtils.mergeArrayIntoCollection(values, this.values);
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public List<String> getStringValues() {
        List<String> stringList = new ArrayList<>(this.values.size());
        for (Object value : this.values) {
            stringList.add(value.toString());
        }
        return Collections.unmodifiableList(stringList);
    }

    public Object getValue() {
        return (!this.values.isEmpty() ? this.values.get(0) : null);
    }

    public String getStringValue() {
        return (!this.values.isEmpty() ? String.valueOf(this.values.get(0)) : null);
    }

    @Override
    public String toString() {
        return this.values.toString();
    }

}