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
package org.apache.lucene.spatial.prefix;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.spatial.prefix.tree.Cell;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CellDump {

    /**
     * @param cells
     * @throws Exception
     */
    public static boolean dump(List<Cell> cells) throws Exception {
        if (cells.isEmpty()) {
            return false;
        }
        StringBuffer buffer = new StringBuffer();
        for (Cell c : cells) {
            buffer.append(c.getLevel() + "," + c.isLeaf() + "," + c.toString() + "\n");
        }
        // #write(File, CharSequence, Charset)}
        File f = new File("./celldump.txt");
        FileUtils.write(f, buffer, Charset.defaultCharset());
        return true;
    }

    public static void main(String[] args) {
    }
}
