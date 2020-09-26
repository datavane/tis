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
package org.apache.hadoop.fs;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import java.io.File;
import java.util.stream.Collectors;

/**
 *  hadoop artifact have been excluded
 * for stub process in ElectionContext<br>
 *     ops.add(Op.check(new Path(leaderPath).getParent().toString(), leaderZkNodeParentVersion));
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-06-18 12:47
 */
public class Path {

    private final String path;

    private final String[] paths;

    public Path(String path) {
        this.path = path;
        this.paths = StringUtils.split(this.path, File.separator);
    }

    public Path getParent() {
        if (paths.length < 2) {
            throw new IllegalStateException("path:" + this.path + " depth can not small than 2");
        }
        return new Path(File.separator + Lists.newArrayList(paths).subList(0, paths.length - 1).stream().collect(Collectors.joining(File.separator)));
    }

    @Override
    public String toString() {
        return path;
    }

    public static void main(String[] args) {
        Path path = new Path("/aa/b/");
        System.out.println(path.getParent());
    }
}
