package com.qlangtech.tis.build.metrics;

public class Counters {

	public static enum Counter {
		MAP_ALL_RECORDS, MAP_INPUT_RECORDS, MAP_SKIPPED_RECORDS, MAP_INPUT_BYTES, MAP_OUTPUT_RECORDS, MAP_OUTPUT_BYTES //
		, DOCMAKE_COMPLETE, INDEXMAKE_COMPLETE, MERGE_COMPLETE, DOCMAKE_FAIL, INDEXMAKE_FAIL
	}

	public void incrCounter(Counter counter, int val) {
	}

	public void setCounterValue(Counter type, long val) {
	}

}
