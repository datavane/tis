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

package com.qlangtech.tis.util;

import com.qlangtech.tis.extension.init.InitMilestone;
import org.jvnet.hudson.reactor.Milestone;

/**
 * Discovers initialization tasks from {@link Initializer}.
 *
 * @author Kohsuke Kawaguchi
 */
public class InitializerFinder extends TaskMethodFinder<Initializer> {

    public InitializerFinder(ClassLoader cl) {
        super(Initializer.class, InitMilestone.class,cl);
    }

    public InitializerFinder() {
        this(Thread.currentThread().getContextClassLoader());
    }

    @Override
    protected String displayNameOf(Initializer i) {
        return i.displayName();
    }

    @Override
    protected String[] requiresOf(Initializer i) {
        return i.requires();
    }

    @Override
    protected String[] attainsOf(Initializer i) {
        return i.attains();
    }

    @Override
    protected Milestone afterOf(Initializer i) {
        return i.after();
    }

    @Override
    protected Milestone beforeOf(Initializer i) {
        return i.before();
    }

    @Override
    protected boolean fatalOf(Initializer i) {
        return i.fatal();
    }
}
