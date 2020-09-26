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
package com.qlangtech.tis.extension.util;

import com.qlangtech.tis.util.Util;
import java.util.*;

/**
 * Traverses a directed graph and if it contains any cycle, throw an exception.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class CyclicGraphDetector<N> {

    private final Set<N> visited = new HashSet<N>();

    private final Set<N> visiting = new HashSet<N>();

    private final Stack<N> path = new Stack<N>();

    private final List<N> topologicalOrder = new ArrayList<N>();

    public void run(Iterable<? extends N> allNodes) throws CycleDetectedException {
        for (N n : allNodes) {
            visit(n);
        }
    }

    /**
     * Returns all the nodes in the topologically sorted order.
     * That is, if there's an edge a->b, b always come earlier than a.
     */
    public List<N> getSorted() {
        return topologicalOrder;
    }

    /**
     * List up edges from the given node (by listing nodes that those edges point to.)
     *
     * @return Never null.
     */
    protected abstract Iterable<? extends N> getEdges(N n);

    private void visit(N p) throws CycleDetectedException {
        if (!visited.add(p))
            return;
        visiting.add(p);
        path.push(p);
        for (N q : getEdges(p)) {
            // ignore unresolved references
            if (q == null)
                continue;
            if (visiting.contains(q))
                detectedCycle(q);
            visit(q);
        }
        visiting.remove(p);
        path.pop();
        topologicalOrder.add(p);
    }

    private void detectedCycle(N q) throws CycleDetectedException {
        int i = path.indexOf(q);
        path.push(q);
        reactOnCycle(q, path.subList(i, path.size()));
    }

    /**
     * React on detected cycles - default implementation throws an exception.
     *
     * @param q
     * @param cycle
     * @throws CycleDetectedException
     */
    protected void reactOnCycle(N q, List<N> cycle) throws CycleDetectedException {
        throw new CycleDetectedException(cycle);
    }

    public static final class CycleDetectedException extends Exception {

        public final List cycle;

        public CycleDetectedException(List cycle) {
            super("Cycle detected: " + Util.join(cycle, " -> "));
            this.cycle = cycle;
        }
    }
}
