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

/* *
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
					throw new IllegalStateException(
							"you are intending to use sorting,facet,group or other statistic feature,please set field:["
									+ key.field + "] docValue property 'true'");
				}
			};

			// caches.clear();

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
