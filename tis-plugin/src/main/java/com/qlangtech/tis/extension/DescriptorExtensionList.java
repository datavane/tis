/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.extension;

import com.qlangtech.tis.TIS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019-12-31 10:26
 */
public class DescriptorExtensionList<T extends Describable<T>, D extends Descriptor<T>> extends ExtensionList<D> {

    private final Class<T> describableType;

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorExtensionList.class);

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Describable<T>, D extends Descriptor<T>> DescriptorExtensionList<T, D> createDescriptorList(TIS tis, Class<T> describableType) {
        return new DescriptorExtensionList<T, D>(tis, describableType);
    }

    protected DescriptorExtensionList(TIS tis, Class<T> describableType) {
        super(tis, (Class) Descriptor.class);
        this.describableType = describableType;
    }

    public Class<T> getDescribableType() {
        return this.describableType;
    }

    @Override
    protected List<ExtensionComponent<D>> load() {
        return _load(this.tis.getExtensionList(Descriptor.class).getComponents());
    }

    @Override
    protected Collection<ExtensionComponent<D>> load(ExtensionComponentSet delta) {
        return _load(delta.find(Descriptor.class));
    }

    private List<ExtensionComponent<D>> _load(Iterable<ExtensionComponent<Descriptor>> set) {
        List<ExtensionComponent<D>> r = new ArrayList<ExtensionComponent<D>>();
        for (ExtensionComponent<Descriptor> c : set) {
            Descriptor d = c.getInstance();
            try {
                if (d.getT() == describableType) {
                    r.add((ExtensionComponent) c);
                }
            } catch (IllegalStateException e) {
                // skip this one
                LOGGER.error(d.getClass() + " doesn't extend Descriptor with a type parameter", e);
            }
        }
        return r;
    }
}
