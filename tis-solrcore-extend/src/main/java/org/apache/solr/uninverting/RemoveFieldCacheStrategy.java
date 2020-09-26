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
