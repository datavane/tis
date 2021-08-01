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
import org.jvnet.hudson.reactor.Task;

import static com.qlangtech.tis.extension.init.InitMilestone.*;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-11 10:13
 **/
public @interface Initializer {
    /**
     * Indicates that the specified milestone is necessary before executing this initializer.
     *
     * <p>
     * This has the identical purpose as {@link #requires()}, but it's separated to allow better type-safety
     * when using {@link InitMilestone} as a requirement (since enum member definitions need to be constant).
     */
    InitMilestone after() default STARTED;

    /**
     * Indicates that this initializer is a necessary step before achieving the specified milestone.
     *
     * <p>
     * This has the identical purpose as {@link #attains()}. See {@link #after()} for why there are two things
     * to achieve the same goal.
     */
    InitMilestone before() default COMPLETED;

    /**
     * Indicates the milestones necessary before executing this initializer.
     */
    String[] requires() default {};

    /**
     * Indicates the milestones that this initializer contributes to.
     *
     * A milestone is considered attained if all the initializers that attains the given milestone
     * completes. So it works as a kind of join.
     */
    String[] attains() default {};

    /**
     * Key in {@code Messages.properties} that represents what this task is about. Used for rendering the progress.
     * Defaults to "${short class name}.${method Name}".
     */
    String displayName() default "";

    /**
     * Should the failure in this task prevent Hudson from starting up?
     *
     * @see Task#failureIsFatal()
     */
    boolean fatal() default true;
}
