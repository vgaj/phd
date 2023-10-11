package com.github.vgaj.phd.server.monitor;

import com.github.vgaj.phd.server.data.RemoteAddress;
import lombok.Data;
import org.pcap4j.packet.Packet;

import com.github.vgaj.phd.server.messages.MessageData;
import com.github.vgaj.phd.server.data.MonitorData;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Consumes new captured data via a Distruptor
 */
@Component
public class NewDataProcessor
{
    private static final int BUFFER_SIZE = 65536;
    private static final boolean DEBUG_LOG = false;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private Disruptor<NewDataEvent> disruptor;

    @Autowired
    private MessageData messageData;

    @Autowired
    private MonitorData monitorData;

    @Autowired
    private PcapPacketHelper pcapHelper;

    // TODO: remove class
    @Data
    private class NewDataEvent
    {
        // TODO: Can the data be stored here?
        private RemoteAddress host;
        private int length;
        private long epochMinute;
        private long queuedTime;
    }


    private class DisruptorQueueItem
    {
        private DisruptorQueueItem(Packet pcapPacket)
        {
            this.pcapPacket = pcapPacket;
            queuedTimeNs = System.nanoTime();
            epochMinute = Instant.now().getEpochSecond() / 60;
        }
        private Packet pcapPacket;
        private long queuedTimeNs;
        private long epochMinute;
    }
    private LongAdder packetCounter = new LongAdder();
    private AtomicLong maxDelayToTranslateStart = new AtomicLong();
    private AtomicLong maxTimeToTranslate = new AtomicLong();
    private AtomicLong maxDelayToHandlerStart = new AtomicLong();
    private AtomicLong maxTimeToHandle = new AtomicLong();


    private void onEvent(NewDataEvent newDataEvent, long sequence, boolean endOfBatch) throws Exception
    {
        long startNs = System.nanoTime();
        updateMax(maxDelayToHandlerStart, startNs - newDataEvent.getQueuedTime());

        monitorData.addData(newDataEvent.getHost(), newDataEvent.getLength(), newDataEvent.getEpochMinute());

        updateMax(maxTimeToHandle, System.nanoTime() - startNs);
    }

    private void updateMax( AtomicLong max, long value) {
        long currentMax;
        do {
            currentMax = max.get();
        } while (value > currentMax && !max.compareAndSet(currentMax, value));
    }

    private void translate(NewDataEvent event, long sequence, DisruptorQueueItem pcapPacketData)
    {
        long startNs = System.nanoTime();
        updateMax(maxDelayToTranslateStart, startNs - pcapPacketData.queuedTimeNs);

        event.setQueuedTime(pcapPacketData.queuedTimeNs);
        event.setEpochMinute(pcapPacketData.epochMinute);
        if (!pcapHelper.isIpv4(pcapPacketData.pcapPacket))
        {
            // TODO is this exiting?
            messageData.addMessage("Received data that was not IPv4");
            return;
        }
        if (DEBUG_LOG)
        {
            messageData.addMessage(pcapHelper.getSourceHost(pcapPacketData.pcapPacket).getAddressString() + " -> " + pcapHelper.getDestHost(pcapPacketData.pcapPacket).getAddressString() + " (" + pcapHelper.getLength(pcapPacketData.pcapPacket) + " bytes)");
        }
        event.setHost(pcapHelper.getDestHost(pcapPacketData.pcapPacket));
        event.setLength(pcapHelper.getLength(pcapPacketData.pcapPacket));
        updateMax(maxTimeToTranslate, System.nanoTime() - startNs);
    }

    /**
     * Creates and starts the disruptor
     */
    @PostConstruct
    public void init()
    {
        disruptor = new Disruptor<>(NewDataEvent::new, BUFFER_SIZE, DaemonThreadFactory.INSTANCE);
        disruptor.handleEventsWith(this::onEvent);
        disruptor.start();
        messageData.addMessage("Started new data processor");
    }

    /**
     * Queue a captured packet to be processed
     * @param pcapPacket Data that was captured
     */
    public void processNewData(Packet pcapPacket)
    {
        packetCounter.increment();
        disruptor.getRingBuffer().publishEvent(this::translate, new DisruptorQueueItem(pcapPacket));
    }

    private final int statsReportRate = 1;
    @Scheduled(fixedRate = statsReportRate * 1000)
    public void reportStats()
    {
        long packetCount = packetCounter.sumThenReset();
        long startTranslateMax = maxDelayToTranslateStart.getAndSet(0);
        long startHandleMax = maxDelayToHandlerStart.getAndSet(0);
        long translateMax = maxTimeToTranslate.getAndSet(0);
        long handleMax = maxTimeToHandle.getAndSet(0);
        if (DEBUG_LOG)
        {
            if (packetCount > 0)
            {
                logger.info("\n{} packets in last {} second(s)\n maximum time to START translate/handling {}/ {}\n maximum time to PERFORM translation/handling {}/ {}",
                        packetCount, statsReportRate, formatNs(startTranslateMax), formatNs(startHandleMax), formatNs(translateMax), formatNs(handleMax));
            }
        }
    }

    private String formatNs(long timeNs)
    {
        if (timeNs < 1000000) return timeNs/1000 + " us";
        else if (timeNs < 1000000000) return timeNs/1000000 + " MS";
        else return timeNs/1000000000 + " SECONDS";
    }

}
