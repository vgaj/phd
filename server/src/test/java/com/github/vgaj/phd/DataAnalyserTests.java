package com.github.vgaj.phd;

import com.github.vgaj.phd.server.analysis.AnalyserUtil;
import com.github.vgaj.phd.server.result.TransferCount;
import com.github.vgaj.phd.server.result.TransferIntervalMinutes;
import com.github.vgaj.phd.server.result.TransferSizeBytes;
import com.github.vgaj.phd.server.result.TransferTimestamp;
import org.junit.jupiter.api.Test;

import java.util.*;

class DataAnalyserTests
{
	@Test
	void testSizeFrequencies()
	{
		// Arrange
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

		// Act
		Map<TransferSizeBytes, TransferCount> result = new AnalyserUtil().getDataSizeFrequenciesFromRaw(data);

		// Assert
		assert result.size() == 3;
		assert result.get(size1).getCount() == 2;
		assert result.get(size2).getCount() == 3;
		assert result.get(size3).getCount() == 1;
	}

	@Test
	void getIntervals()
	{
		// Arrange
		List<Map.Entry<TransferTimestamp, TransferSizeBytes>> data = new ArrayList<>();
		TransferSizeBytes size1 = new TransferSizeBytes(100);
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(1L),size1));
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(11L),size1)); // gap = 10
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(21L),size1)); // gap = 10
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(26L),size1)); // gap = 5
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(36L),size1)); // gap = 10

		// Act
		Map<TransferIntervalMinutes, List<TransferSizeBytes>> result = new AnalyserUtil().getIntervalsBetweenData(data);

		// Assert
		assert result.size() == 2;
		assert result.get(TransferIntervalMinutes.of(5)).size() == 1;
		assert result.get(TransferIntervalMinutes.of(10)).size() == 3;
	}

	@Test
	void oneEntrySoNoIntervals()
	{
		// Arrange
		List<Map.Entry<TransferTimestamp, TransferSizeBytes>> data = new ArrayList<>();
		TransferSizeBytes size1 = new TransferSizeBytes(100);
		data.add(new AbstractMap.SimpleEntry<>(new TransferTimestamp(1L),size1));

		// Act
		Map<TransferIntervalMinutes, List<TransferSizeBytes>> result = new AnalyserUtil().getIntervalsBetweenData(data);

		// Assert
		assert result.isEmpty();
	}
}
