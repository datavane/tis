package com.qlangtech.tis.build.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Counters {

	public static enum Counter {
		MAP_ALL_RECORDS, MAP_INPUT_RECORDS, MAP_SKIPPED_RECORDS, MAP_INPUT_BYTES, MAP_OUTPUT_RECORDS, MAP_OUTPUT_BYTES //
		, DOCMAKE_COMPLETE, DOCMAKE_QUEUE_PUT_TIME, INDEXMAKE_COMPLETE, MERGE_COMPLETE, DOCMAKE_FAIL, INDEXMAKE_FAIL
	}

	private final Map<Counter, AtomicLong> counters = new HashMap<>();

	public void incrCounter(Counter counter, int val) {
		AtomicLong aval = getCounter(counter);
		aval.addAndGet(val);
	}

	public void setCounterValue(Counter type, long val) {
		AtomicLong aval = getCounter(type);
		aval.set(val);
	}

	public AtomicLong getCounter(Counter type) {
		AtomicLong aval = counters.get(type);
		if (aval == null) {
			synchronized (counters) {
				aval = counters.get(type);
				if (aval == null) {
					aval = new AtomicLong();
					counters.put(type, aval);
				}
			}
		}
		return aval;
	}

}
