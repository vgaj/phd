/*
MIT License

Copyright (c) 2022-2024 Viru Gajanayake

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.github.vgaj.phd.server.monitor.pcap;

import lombok.Data;
import org.pcap4j.packet.Packet;

import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.MessageData;
import com.github.vgaj.phd.server.data.MonitorData;

import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class PcapNewDataProcessor
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

    @Data
    private class NewDataEvent
    {
        private Packet pcapPacket;
        private long epochMinute;
        private long queuedTime;
    }

    private LongAdder packetCounter = new LongAdder();
    private AtomicLong maxTimeToTranslate = new AtomicLong();
    private AtomicLong maxDelayToHandlerStart = new AtomicLong();
    private AtomicLong maxTimeToHandle = new AtomicLong();


    private void onEvent(NewDataEvent newDataEvent, long sequence, boolean endOfBatch) throws Exception
    {
        long startNs = System.nanoTime();
        updateMax(maxDelayToHandlerStart, startNs - newDataEvent.getQueuedTime());

        if (!pcapHelper.isIpv4(newDataEvent.getPcapPacket()))
        {
            messageData.addMessage("Received data that was not IPv4");
        }
        else
        {
            RemoteAddress host = pcapHelper.getDestHost(newDataEvent.getPcapPacket());
            int length = pcapHelper.getLength(newDataEvent.getPcapPacket());

            if (DEBUG_LOG)
            {
                messageData.addMessage(pcapHelper.getSourceHost(newDataEvent.getPcapPacket()).getAddressString() + " -> " + host.getAddressString() + " (" + length + " bytes)");
            }

            monitorData.addData(host, length, newDataEvent.getEpochMinute());
        }

        updateMax(maxTimeToHandle, System.nanoTime() - startNs);
    }

    private void updateMax( AtomicLong max, long value) {
        long currentMax;
        do {
            currentMax = max.get();
        } while (value > currentMax && !max.compareAndSet(currentMax, value));
    }

    private void translate(NewDataEvent event, long sequence, Packet pcapPacket)
    {
        // Note translate is called from calling thread
        long startNs = System.nanoTime();

        event.setPcapPacket(pcapPacket);
        event.setQueuedTime(startNs);
        event.setEpochMinute(Instant.now().getEpochSecond() / 60);

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
        disruptor.getRingBuffer().publishEvent(this::translate, pcapPacket);
    }

    @Value("${phd.report.interval.ms}")
    private String statsReportRate;

    @Scheduled(fixedRateString = "${phd.report.interval.ms}", initialDelayString = "${phd.report.interval.ms}")
    public void reportStats()
    {
        long packetCount = packetCounter.sumThenReset();
        long startHandleMax = maxDelayToHandlerStart.getAndSet(0);
        long translateMax = maxTimeToTranslate.getAndSet(0);
        long handleMax = maxTimeToHandle.getAndSet(0);
        if (packetCount > 0)
        {
            if (DEBUG_LOG || startHandleMax > 1000000)
            {
                logger.info("\n{} packets in last {} second(s)\n maximum time to START handling {}\n maximum time to PERFORM translation/handling {}/ {}",
                        packetCount, statsReportRate, formatNs(startHandleMax), formatNs(translateMax), formatNs(handleMax));
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
