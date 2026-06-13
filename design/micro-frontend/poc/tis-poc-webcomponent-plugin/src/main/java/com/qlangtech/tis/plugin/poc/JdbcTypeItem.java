package com.qlangtech.tis.plugin.poc;

import java.io.Serializable;

/**
 * JDBC类型数据项
 */
public class JdbcTypeItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 类型名称 */
    private String name;

    /** 类型代码 */
    private String code;

    /** 是否选中 */
    private boolean selected;

    /** 描述信息 */
    private String description;

    public JdbcTypeItem() {
    }

    public JdbcTypeItem(String name, String code, boolean selected, String description) {
        this.name = name;
        this.code = code;
        this.selected = selected;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}