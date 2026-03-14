/*
MIT License

Copyright (c) 2022-2025 Viru Gajanayake

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

package com.github.vgaj.phd.uireact.controller;

import com.github.vgaj.phd.common.ipc.DomainSocketComms;
import com.github.vgaj.phd.common.query.*;
import com.github.vgaj.phd.common.util.EpochMinuteUtil;
import com.github.vgaj.phd.common.util.ExecutableDetails;
import com.github.vgaj.phd.common.util.HotSpotModeChecker;
import com.github.vgaj.phd.uireact.IpcService;
import com.github.vgaj.phd.uireact.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final IpcService ipcService;

    public ApiController(IpcService ipcService) {
        this.ipcService = ipcService;
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponseDto> getSummary() {
        try (DomainSocketComms comms = ipcService.connect()) {
            comms.writeSocketMessage(new SummaryResultsQuery());
            SummaryResultsResponse response = comms.readSocketMessage(SummaryResultsResponse.class);
            if (response == null) {
                return ResponseEntity.status(502).build();
            }
            List<SummaryResultDto> results = new ArrayList<>();
            Arrays.stream(response.data().results()).forEach(r -> results.add(toDto(r)));
            List<String> messages = response.data().messages() != null
                    ? Arrays.asList(response.data().messages())
                    : List.of();
            return ResponseEntity.ok(new SummaryResponseDto(results, messages));
        } catch (Exception e) {
            logger.error("IPC error on /api/summary", e);
            return ResponseEntity.status(503).build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<HostHistoryResponseDto> getHistory(
            @RequestParam String source,
            @RequestParam String destination) {
        InetAddress sourceAddr;
        InetAddress destAddr;
        try {
            sourceAddr = InetAddress.getByName(source);
            destAddr = InetAddress.getByName(destination);
        } catch (UnknownHostException e) {
            return ResponseEntity.badRequest().build();
        }
        try (DomainSocketComms comms = ipcService.connect()) {
            comms.writeSocketMessage(new HostHistoryQuery(sourceAddr, destAddr));
            HostHistoryResponse response = comms.readSocketMessage(HostHistoryResponse.class);
            if (response == null) {
                return ResponseEntity.status(502).build();
            }
            List<String> history = response.results() != null
                    ? Arrays.asList(response.results())
                    : List.of();
            return ResponseEntity.ok(new HostHistoryResponseDto(source, destination, history));
        } catch (Exception e) {
            logger.error("IPC error on /api/history", e);
            return ResponseEntity.status(503).build();
        }
    }

    @GetMapping("/debug-log")
    public ResponseEntity<DebugLogResponseDto> getDebugLog() {
        try (DomainSocketComms comms = ipcService.connect()) {
            comms.writeSocketMessage(new DebugLogQuery());
            DebugLogResponse response = comms.readSocketMessage(DebugLogResponse.class);
            if (response == null) {
                return ResponseEntity.status(502).build();
            }
            List<String> log = response.log() != null
                    ? Arrays.asList(response.log())
                    : List.of();
            return ResponseEntity.ok(new DebugLogResponseDto(log));
        } catch (Exception e) {
            logger.error("IPC error on /api/debug-log", e);
            return ResponseEntity.status(503).build();
        }
    }

    private SummaryResultDto toDto(DisplayResult result) {
        String source;
        if (HotSpotModeChecker.isHotSpot()) {
            source = result.sourceIpAddress();
            if (result.sourceAddressExtraDetails() != null) {
                source += " - " + result.sourceAddressExtraDetails();
            }
        } else {
            source = !result.probableExecutableDetails().isBlank()
                    ? ExecutableDetails.getCommand(result.probableExecutableDetails())
                    : "Unknown Source";
        }

        String destination = result.destinationIpAddress();
        if (!result.destinationIpAddress().equals(result.destinationHostName())) {
            destination += " - " + result.destinationHostName();
        }

        List<String> details = new ArrayList<>();
        Arrays.stream(result.resultLines()).forEach(line -> {
            details.add(line.message());
            Arrays.stream(line.subMessages()).forEach(sub -> details.add("- " + sub));
        });

        return new SummaryResultDto(
                source,
                destination,
                result.sourceIpAddress(),
                result.destinationIpAddress(),
                EpochMinuteUtil.toString(result.lastSeenEpochMinute()),
                result.isCurrent() ? "Yes" : "No",
                String.valueOf(result.score()),
                result.totalBytes(),
                result.totalTimes(),
                details
        );
    }
}
