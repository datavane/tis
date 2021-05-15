/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 *   This program is free software: you can use, redistribute, and/or modify
 *   it under the terms of the GNU Affero General Public License, version 3
 *   or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.qlangtech.tis.extension;

/**
 * {@link ExtensionList} listener.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @since 1.614
 */
public abstract class ExtensionListListener {

    /**
     * {@link ExtensionList} contents has changed.
     * <p>
     * This would be called when an entry gets added to or removed from the list for any reason e.g.
     * when a dynamically loaded plugin introduces a new {@link 'ExtensionPoint'} implementation
     * that adds an entry to the {@link ExtensionList} being listened to.
     */
    public abstract void onChange();
}

