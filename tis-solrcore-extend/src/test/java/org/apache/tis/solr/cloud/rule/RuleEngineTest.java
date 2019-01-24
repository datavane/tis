/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.apache.tis.solr.cloud.rule;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.cloud.rule.ReplicaAssigner;
import org.apache.solr.cloud.rule.Rule;
import org.apache.solr.common.util.Utils;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RuleEngineTest extends TestCase {

    public void testPlacement3() {
        String s = "{" + "  '127.0.0.1:49961_':{" + "    'node':'127.0.0.1:49961_'," + "    }," + "  '127.0.0.2:49955_':{" + "    'node':'127.0.0.1:49955_'," + "    }," + "  '127.0.0.3:49952_':{" + "    'node':'127.0.0.1:49952_'," + "    }," + "  '127.0.0.1:49947_':{" + "    'node':'127.0.0.1:49947_'," + "    }," + "  '127.0.0.2:49958_':{" + "    'node':'127.0.0.1:49958_'," + "    }}";
        Map nodeVsTags = (Map) Utils.fromJSON(s.getBytes(StandardCharsets.UTF_8));
        Map shardVsReplicaCount = Utils.makeMap("shard1", 2, "shard2", 2);
        List<Rule> rules = parseRules("[{'cores':'<4'}, {" + "'replica':'1',shard:'*','node':'*'}," + " {'freedisk':'>1'}]");
    // ReplicaAssigner mapping = new ReplicaAssigner(
    // rules,
    // shardVsReplicaCount, singletonList(MockSnitch.class.getName()),
    // new HashMap(), new ArrayList<>(MockSnitch.nodeVsTags.keySet()), null, null).getNodeMappings();
    // assertNotNull(mapping);
    // assertFalse(mapping.containsValue("127.0.0.1:49947_"));
    }

    private List<Rule> parseRules(String s) {
        List maps = (List) Utils.fromJSON(s.getBytes(StandardCharsets.UTF_8));
        List<Rule> rules = new ArrayList<>();
        for (Object map : maps) rules.add(new Rule((Map) map));
        return rules;
    }
}
