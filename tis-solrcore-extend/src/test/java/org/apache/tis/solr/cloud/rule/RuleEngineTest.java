/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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

/**
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
