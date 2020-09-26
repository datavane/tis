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
package org.shai.xmodifier.util;

/**
 * Created by Shenghai on 14-11-28.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Cons<L, R> {

    private L left;

    private R right;

    public Cons(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public Cons() {
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public void setRight(R right) {
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "Cons{" + "left=" + left + ", right=" + right + '}';
    }
}
