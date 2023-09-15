package com.github.vgaj.phd;

import com.github.vgaj.phd.logic.AnalyserUtil;
import com.github.vgaj.phd.result.TransferCount;
import com.github.vgaj.phd.result.TransferIntervalMinutes;
import com.github.vgaj.phd.result.TransferSizeBytes;
import com.github.vgaj.phd.result.TransferTimestamp;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

//@SpringBootTest
class DataAnalyserTests
{
	@Test
	void testSizeFrequencies()
	{
		List<Map.Entry<TransferTimestamp, TransferSizeBytes>> data = new ArrayList<>();
		long time = 1L;
		TransferSizeBytes size1 = new TransferSizeBytes(100);
		TransferSizeBytes size2 = new TransferSizeBytes(200);
		TransferSizeBytes size3 = new TransferSizeBytes(300);
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(time++), size1));
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(time++),size2));
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(time++),size3));
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(time++),size1));
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(time++),size2));
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(time++),size2));
		Map<TransferSizeBytes, TransferCount> result = new AnalyserUtil().getDataSizeFrequenciesFromRaw(data);
		assert result.size() == 3;
		assert result.get(size1).getCount() == 2;
		assert result.get(size2).getCount() == 3;
		assert result.get(size3).getCount() == 1;
	}

	@Test
	void getIntervals()
	{
		List<Map.Entry<TransferTimestamp, TransferSizeBytes>> data = new ArrayList<>();
		TransferSizeBytes size1 = new TransferSizeBytes(100);
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(1L),size1));
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(11L),size1)); // gap = 10
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(21L),size1)); // gap = 10
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(26L),size1)); // gap = 5
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(36L),size1)); // gap = 10
		Map<TransferIntervalMinutes, List<TransferSizeBytes>> result = new AnalyserUtil().getIntervalsBetweenData(data);

		assert result.size() == 2;
		assert result.get(new TransferIntervalMinutes(5)).size() == 1;
		assert result.get(new TransferIntervalMinutes(10)).size() == 3;
	}

	@Test
	void oneEntrySoNoIntervals()
	{
		List<Map.Entry<TransferTimestamp, TransferSizeBytes>> data = new ArrayList<>();
		TransferSizeBytes size1 = new TransferSizeBytes(100);
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(1L),size1));
		Map<TransferIntervalMinutes, List<TransferSizeBytes>> result = new AnalyserUtil().getIntervalsBetweenData(data);
		assert result.isEmpty();
	}
}
