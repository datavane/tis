/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.extension;

/**
 * Filters out {@link ExtensionComponent}s discovered by {@link ExtensionFinder}s,
 * as if they were never discovered.
 * <p>
 * This is useful for those who are deploying restricted/simplified version of Jenkins
 * by reducing the functionality.
 * <p>
 * Because of the way {@link ExtensionFinder} works, even when an extension component
 * is rejected by a filter, its instance still gets created first.
 * @see ExtensionComponentSet#filtered()
 * @since 1.472
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class ExtensionFilter {

    /**
     * Checks if a newly discovered extension is allowed to participate into Jenkins.
     *
     * <p>
     * To filter {@link Descriptor}s based on the {@link Describable} subtypes, do as follows:
     *
     * <pre>
     * return !component.isDescriptorOf(Builder.class);
     * </pre>
     *
     * @param type The type of the extension that we are discovering. This is not the actual instance
     *             type, but the contract type, such as {@link Descriptor}, , etc.
     * @return true to let the component into Jenkins. false to drop it and pretend
     * as if it didn't exist. When any one of {@link ExtensionFilter}s veto
     * a component, it gets dropped.
     */
    public abstract <T> boolean allows(Class<T> type, ExtensionComponent<T> component);

    public static <T> boolean isAllowed(Class<T> type, ExtensionComponent<T> component) {
        // return true;
        return true;
    }
    /**
     * All registered {@link ExtensionFilter} instances.
     */
    // public static ExtensionList<ExtensionFilter> all() {
    // return ExtensionList.lookup(ExtensionFilter.class);
    // }
}
