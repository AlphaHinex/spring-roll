package io.github.springroll.export.excel;

import lombok.Data;

import java.util.List;

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
    private List<DecodeBean> decoder;

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

    public List<DecodeBean> getDecoder() {
        return decoder;
    }

    public void setDecoder(List<DecodeBean> decoder) {
        this.decoder = decoder;
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

}

@Data
class DecodeBean {
    /**
     * Value in data
     */
    private String value;
    /**
     * Name of value to display
     */
    private String name;
}
