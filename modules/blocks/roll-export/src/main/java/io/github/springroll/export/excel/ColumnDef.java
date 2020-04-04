package io.github.springroll.export.excel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
class ColumnDef {

    /**
     * 显示名
     */
    private String display;

    /**
     * 对应字段属性名
     */
    private String name;

    /**
     * 是否需要显示（输出），默认为是
     */
    private boolean showTitle = true;

    /**
     * 字段解码器，根据 value 翻译 name
     */
    @Getter
    @Setter
    private List<ColumnDecoder> decoder;

    /**
     * 默认无参构造器，供 Jackson 使用
     */
    public ColumnDef() { }

    public ColumnDef(String display, String name) {
        this.display = display;
        this.name = name;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public String getField() {
        return name;
    }

    public void setField(String field) {
        this.name = field;
    }

    public String getTitle() {
        return display;
    }

    public void setTitle(String title) {
        this.display = title;
    }

    public boolean isHidden() {
        return !showTitle;
    }

    public void setHidden(boolean hidden) {
        this.showTitle = !hidden;
    }

    // Element UI properties: prop, label

    public String getProp() {
        return name;
    }

    public void setProp(String prop) {
        this.name = prop;
    }

    public String getLabel() {
        return display;
    }

    public void setLabel(String label) {
        this.display = label;
    }

    public Map<String, String> getDecoderMap() {
        if (CollectionUtils.isEmpty(decoder)) {
            return Collections.emptyMap();
        } else {
            Map<String, String> map = new HashMap<>(decoder.size());
            for (ColumnDecoder de : decoder) {
                map.put(de.getValue(), de.getName());
            }
            return map;
        }
    }

}