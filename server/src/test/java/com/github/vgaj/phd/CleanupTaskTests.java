package com.github.vgaj.phd;

import com.github.vgaj.phd.server.analysis.AnalyserInterface;
import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.housekeeping.CleanupTask;
import com.github.vgaj.phd.server.messages.MessageData;
import com.github.vgaj.phd.server.monitor.MonitorTaskFilterUpdateInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CleanupTaskTests
{
    @Mock
    private AnalyserInterface analyser;

    @Mock
    private MessageData messageData;

    @Spy
    private MonitorTaskFilterUpdateInterface monitorTaskFilterUpdate;

    @InjectMocks
    private CleanupTask cleanupTask;

    // Test data
    private RemoteAddress address1 = new RemoteAddress((byte) 1, (byte) 0, (byte) 0,(byte)  0);
    private RemoteAddress address2 = new RemoteAddress((byte) 2, (byte) 0, (byte) 0,(byte)  0);
    private RemoteAddress address3 = new RemoteAddress((byte) 3, (byte) 0, (byte) 0,(byte)  0);
    private RemoteAddress address4 = new RemoteAddress((byte) 4, (byte) 0, (byte) 0,(byte)  0);
    private RemoteAddress address5 = new RemoteAddress((byte) 5, (byte) 0, (byte) 0,(byte)  0);
    private RemoteAddress address6 = new RemoteAddress((byte) 6, (byte) 0, (byte) 0,(byte)  0);
    private RemoteAddress address7 = new RemoteAddress((byte) 7, (byte) 0, (byte) 0,(byte)  0);
    private RemoteAddress address8 = new RemoteAddress((byte) 8, (byte) 0, (byte) 0,(byte)  0);
    private RemoteAddress address9 = new RemoteAddress((byte) 9, (byte) 0, (byte) 0,(byte)  0);

    @Test
    public void firstCall()
    {
        // Arrange
        Set<RemoteAddress> addresses = new HashSet<>();
        addresses.add(address3);
        addresses.add(address1);
        when(analyser.getAddressesToIgnore()).thenReturn(addresses);

        // Act
        cleanupTask.removeFrequentAddresses();

        // Assert
        ArgumentCaptor<Set<RemoteAddress>> argumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(monitorTaskFilterUpdate).updateFilter(argumentCaptor.capture());
        assert argumentCaptor.getValue().size() == 2;
        assert argumentCaptor.getValue().contains(address1);
        assert argumentCaptor.getValue().contains(address3);
    }

    @Test
    public void addNewAddressesToIgnore() throws NoSuchFieldException, IllegalAccessException
    {
        // Arrange
        Map<RemoteAddress,Long> currentlyIgnoredAddresses = new HashMap<>();
        currentlyIgnoredAddresses.put(address5, 5L);
        currentlyIgnoredAddresses.put(address4, 4L);
        currentlyIgnoredAddresses.put(address3, 3L);
        Field addressesField = CleanupTask.class.getDeclaredField("currentlyIgnoredAddresses");
        addressesField.setAccessible(true);
        addressesField.set(cleanupTask, currentlyIgnoredAddresses);

        Set<RemoteAddress> addresses = new HashSet<>();
        addresses.add(address2);
        addresses.add(address3);
        when(analyser.getAddressesToIgnore()).thenReturn(addresses);

        // Act
        cleanupTask.removeFrequentAddresses();

        // Assert
        ArgumentCaptor<Set<RemoteAddress>> argumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(monitorTaskFilterUpdate).updateFilter(argumentCaptor.capture());
        assert argumentCaptor.getValue().size() == 4;
        assert argumentCaptor.getValue().contains(address2);
        assert argumentCaptor.getValue().contains(address3);
        assert argumentCaptor.getValue().contains(address4);
        assert argumentCaptor.getValue().contains(address5);
    }

    @Test
    public void removeOldAddressesAndAddNew() throws NoSuchFieldException, IllegalAccessException
    {
        // Arrange
        Map<RemoteAddress,Long> currentlyIgnoredAddresses = new HashMap<>();
        currentlyIgnoredAddresses.put(address5, 5L); // 1 to 5
        currentlyIgnoredAddresses.put(address1, 1L);
        currentlyIgnoredAddresses.put(address4, 4L);
        currentlyIgnoredAddresses.put(address2, 2L);
        currentlyIgnoredAddresses.put(address3, 3L);
        Field addressesField = CleanupTask.class.getDeclaredField("currentlyIgnoredAddresses");
        addressesField.setAccessible(true);
        addressesField.set(cleanupTask, currentlyIgnoredAddresses);

        Field maxCountField = CleanupTask.class.getDeclaredField("maxAddressesToIgnore");
        maxCountField.setAccessible(true);
        maxCountField.set(cleanupTask, 6);



        Set<RemoteAddress> addresses = new HashSet<>();
        addresses.add(address1); // 1 is the oldest in the current list, so it will be 2 and 3 to be removed
        addresses.add(address6); // 6 to 8 are 3 new ones
        addresses.add(address7);
        addresses.add(address8);
        when(analyser.getAddressesToIgnore()).thenReturn(addresses);

        // Act
        cleanupTask.removeFrequentAddresses();

        // Assert
        // 5 existing, adding 3, with max of 6 so 2 oldest should get removed
        ArgumentCaptor<Set<RemoteAddress>> argumentCaptor = ArgumentCaptor.forClass(Set.class);
        verify(monitorTaskFilterUpdate).updateFilter(argumentCaptor.capture());
        assert argumentCaptor.getValue().size() == 6;
        assert argumentCaptor.getValue().contains(address1);
        assert argumentCaptor.getValue().contains(address4);
        assert argumentCaptor.getValue().contains(address5);
        assert argumentCaptor.getValue().contains(address6);
        assert argumentCaptor.getValue().contains(address7);
        assert argumentCaptor.getValue().contains(address8);
    }

}
