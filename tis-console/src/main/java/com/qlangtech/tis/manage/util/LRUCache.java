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
package com.qlangtech.tis.manage.util;


import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

/*
 * @version $Id: LRUCache.java 1065312 2011-01-30 16:08:25Z rmuir $
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LRUCache<K, V> {

    /*
	 * An instance of this class will be shared across multiple instances of an
	 * LRUCache at the same time. Make sure everything is thread safe.
	 */
    private static class CumulativeStats {

        AtomicLong lookups = new AtomicLong();

        AtomicLong hits = new AtomicLong();

        AtomicLong inserts = new AtomicLong();

        AtomicLong evictions = new AtomicLong();
    }

    private CumulativeStats stats;

    // per instance stats. The synchronization used for the map will also be
    // used for updating these statistics (and hence they are not AtomicLongs
    private long lookups;

    private long hits;

    private long inserts;

    private long evictions;

    private long warmupTime = 0;

    private Map<K, V> map;

    private String name;

    private int autowarmCount;

    private String description = "LRU Cache";

    public Object init(Map args, Object persistence) {
        name = (String) args.get("name");
        String str = (String) args.get("size");
        final int limit = str == null ? 1024 : Integer.parseInt(str);
        str = (String) args.get("initialSize");
        final int initialSize = Math.min(str == null ? 1024 : Integer.parseInt(str), limit);
        str = (String) args.get("autowarmCount");
        autowarmCount = str == null ? 0 : Integer.parseInt(str);
        description = "LRU Cache(maxSize=" + limit + ", initialSize=" + initialSize;
        description += ')';
        map = new LinkedHashMap<K, V>(initialSize, 0.75f, true) {

            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                if (size() > limit) {
                    // increment evictions regardless of state.
                    // this doesn't need to be synchronized because it will
                    // only be called in the context of a higher level
                    // synchronized block.
                    evictions++;
                    stats.evictions.incrementAndGet();
                    return true;
                }
                return false;
            }
        };
        if (persistence == null) {
            // must be the first time a cache of this type is being created
            persistence = new CumulativeStats();
        }
        stats = (CumulativeStats) persistence;
        return persistence;
    }

    public String name() {
        return name;
    }

    public int size() {
        synchronized (map) {
            return map.size();
        }
    }

    public V put(K key, V value) {
        synchronized (map) {
            // increment local inserts regardless of state???
            // it does make it more consistent with the current size...
            inserts++;
            return map.put(key, value);
        }
    }

    public V get(K key) {
        synchronized (map) {
            V val = map.get(key);
            return val;
        }
    }

    public void clear() {
        synchronized (map) {
            map.clear();
        }
    }

    // public void warm(SolrIndexSearcher searcher, SolrCache<K, V> old)
    // throws IOException {
    // if (regenerator == null)
    // return;
    // long warmingStartTime = System.currentTimeMillis();
    // LRUCache<K, V> other = (LRUCache<K, V>) old;
    // 
    // // warm entries
    // if (autowarmCount != 0) {
    // Object[] keys, vals = null;
    // 
    // // Don't do the autowarming in the synchronized block, just pull out
    // // the keys and values.
    // synchronized (other.map) {
    // int sz = other.map.size();
    // if (autowarmCount != -1)
    // sz = Math.min(sz, autowarmCount);
    // keys = new Object[sz];
    // vals = new Object[sz];
    // 
    // Iterator<Map.Entry<K, V>> iter = other.map.entrySet()
    // .iterator();
    // 
    // // iteration goes from oldest (least recently used) to most
    // // recently used,
    // // so we need to skip over the oldest entries.
    // int skip = other.map.size() - sz;
    // for (int i = 0; i < skip; i++)
    // iter.next();
    // 
    // for (int i = 0; i < sz; i++) {
    // Map.Entry<K, V> entry = iter.next();
    // keys[i] = entry.getKey();
    // vals[i] = entry.getValue();
    // }
    // }
    // 
    // // autowarm from the oldest to the newest entries so that the
    // // ordering will be
    // // correct in the new cache.
    // for (int i = 0; i < keys.length; i++) {
    // try {
    // boolean continueRegen = regenerator.regenerateItem(
    // searcher, this, old, keys[i], vals[i]);
    // if (!continueRegen)
    // break;
    // } catch (Throwable e) {
    // SolrException.log(log, "Error during auto-warming of key:"
    // + keys[i], e);
    // }
    // }
    // }
    // 
    // warmupTime = System.currentTimeMillis() - warmingStartTime;
    // }
    public void close() {
    }

    // ////////////////////// SolrInfoMBeans methods //////////////////////
    public String getName() {
        return LRUCache.class.getName();
    }

    // public String getVersion() {
    // return SolrCore.version;
    // }
    public String getDescription() {
        return description;
    }

    // public Category getCategory() {
    // return Category.CACHE;
    // }
    public String getSourceId() {
        return "$Id: LRUCache.java 1065312 2011-01-30 16:08:25Z rmuir $";
    }

    public String getSource() {
        return "$URL: http://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_3_4/solr/core/src/java/org/apache/solr/search/LRUCache.java $";
    }

    public URL[] getDocs() {
        return null;
    }

    // returns a ratio, not a percent.
    private static String calcHitRatio(long lookups, long hits) {
        if (lookups == 0)
            return "0.00";
        if (lookups == hits)
            return "1.00";
        // rounded down
        int hundredths = (int) (hits * 100 / lookups);
        if (hundredths < 10)
            return "0.0" + hundredths;
        return "0." + hundredths;
    /**
     * code to produce a percent, if we want it... int ones = (int)(hits*100
     * / lookups); int tenths = (int)(hits*1000 / lookups) - ones*10; return
     * Integer.toString(ones) + '.' + tenths;
     * *
     */
    }

    public NamedList getStatistics() {
        NamedList lst = new SimpleOrderedMap();
        synchronized (map) {
            lst.add("lookups", lookups);
            lst.add("hits", hits);
            lst.add("hitratio", calcHitRatio(lookups, hits));
            lst.add("inserts", inserts);
            lst.add("evictions", evictions);
            lst.add("size", map.size());
        }
        lst.add("warmupTime", warmupTime);
        long clookups = stats.lookups.get();
        long chits = stats.hits.get();
        lst.add("cumulative_lookups", clookups);
        lst.add("cumulative_hits", chits);
        lst.add("cumulative_hitratio", calcHitRatio(clookups, chits));
        lst.add("cumulative_inserts", stats.inserts.get());
        lst.add("cumulative_evictions", stats.evictions.get());
        return lst;
    }

    @Override
    public String toString() {
        return name + getStatistics().toString();
    }
}
