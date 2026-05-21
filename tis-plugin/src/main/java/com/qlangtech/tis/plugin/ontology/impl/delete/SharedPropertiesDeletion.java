/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.ontology.impl.delete;

import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.items.manager.ListItemsManagerAction;
import com.qlangtech.tis.plugin.ontology.Ontology;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/18
 */
public class SharedPropertiesDeletion extends BasicOntologyResourceDeletion {

    @Override
    protected Ontology.OntologyEnum getOntologyEnum() {
        return Ontology.OntologyEnum.SharedProperty;
    }


    @TISExtension
    public static class DefaultDesc extends BasicDeletionDesc {
        @Override
        public final ListItemsManagerTargetResType parseTargetResType() {
            return ListItemsManagerTargetResType.OntologySharedProperty;
        }
    }
}
