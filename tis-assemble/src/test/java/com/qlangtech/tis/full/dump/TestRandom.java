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
package com.qlangtech.tis.full.dump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月21日
 */
public class TestRandom extends TestCase {

    /**
     * @param args
     */
    public void test() {
        List<String> allPerson = new ArrayList<String>();
        allPerson.add("何XX");
        allPerson.add("李XX");
        allPerson.add("屈XX");
        allPerson.add("刘XX");
        allPerson.add("王XX");
        allPerson.add("涂XX");
        allPerson.add("张XX");
        allPerson.add("方XX");
        allPerson.add("何XX");
        allPerson.add("周XX");
        List<String> aGroup = new ArrayList<String>();
        List<String> bGroup = new ArrayList<String>();
        Collections.shuffle(allPerson);
        vist(allPerson.iterator(), aGroup, bGroup);
        System.out.println(aGroup);
        System.out.println(bGroup);
    }

    private void vist(Iterator<String> it, List<String> aGroup, List<String> bGroup) {
        if (!it.hasNext()) {
            return;
        }
        aGroup.add(it.next());
        vist(it, bGroup, aGroup);
    }
}
