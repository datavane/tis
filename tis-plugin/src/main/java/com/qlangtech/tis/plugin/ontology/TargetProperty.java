package com.qlangtech.tis.plugin.ontology;

import java.util.Objects;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/28
 */
public record TargetProperty(String objectType, String property) {
    public TargetProperty(String objectType, String property) {
        this.objectType = Objects.requireNonNull(objectType);
        this.property = Objects.requireNonNull(property);
    }

    @Override
    public String toString() {
        return "{" +
                "objectType='" + objectType + '\'' +
                ", property='" + property + '\'' +
                '}';
    }
}
