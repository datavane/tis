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
package org.apache.solr.uninverting;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.util.Accountable;
import org.apache.solr.uninverting.FieldCacheImpl.Cache;
import org.apache.solr.uninverting.FieldCacheImpl.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RemoveFieldCacheStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RemoveFieldCacheStrategy.class);

    @SuppressWarnings("all")
    public static void removeFieldCache() {
        try {
            FieldCacheImpl fieldCacheManager = (FieldCacheImpl) FieldCache.DEFAULT;
            Field cacheField = FieldCacheImpl.class.getDeclaredField("caches");
            cacheField.setAccessible(true);
            // 防止启动的时候在schema中没有设置 docvalue属性的时候,字段设置了indexed=true
            // 将doc的term的值预先加载到内存中，防止業務方不適當設置query對象導致服務端OOM
            Map<Class<?>, Cache> caches = (Map<Class<?>, Cache>) cacheField.get(fieldCacheManager);
            FieldCacheImpl.Cache disable = new FieldCacheImpl.Cache(null) {

                @Override
                protected Accountable createValue(LeafReader reader, CacheKey key) throws IOException {
                    return null;
                }

                @Override
                public Object get(LeafReader reader, CacheKey key) throws IOException {
                    throw new IllegalStateException("you are intending to use sorting,facet,group or other statistic feature,please set field:[" + key.field + "] docValue property 'true'");
                }
            };
            for (Map.Entry<Class<?>, Cache> entry : caches.entrySet()) {
                entry.setValue(disable);
            }
            // caches.put(Long.TYPE, disable);
            // caches.put(BinaryDocValues.class, disable);
            // caches.put(SortedDocValues.class, disable);
            // caches.put(DocTermOrds.class, disable);
            // caches.put(DocsWithFieldCache.class, disable);
            logger.info("successful remove fieldcaches");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
