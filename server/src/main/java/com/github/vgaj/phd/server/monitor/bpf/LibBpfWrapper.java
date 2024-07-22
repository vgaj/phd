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

package com.github.vgaj.phd.server.monitor.bpf;

import com.github.vgaj.phd.server.data.RemoteAddress;
import com.github.vgaj.phd.server.messages.MessageInterface;
import com.github.vgaj.phd.server.messages.Messages;

import com.github.vgaj.phd.common.util.Pair;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

@Component
public class LibBpfWrapper
{
    private MessageInterface messages = Messages.getLogger(this.getClass());

    public interface LibBpf extends Library {
        LibBpf INSTANCE = Native.load("bpf", LibBpf.class);

        class BpfMapInfo extends Structure
        {
            /*
                struct bpf_map_info {
                    __u32 type;
                    __u32 id;
                    __u32 key_size;
                    __u32 value_size;
                    __u32 max_entries;
                    __u32 map_flags;
                    char  name[BPF_OBJ_NAME_LEN];
                    __u32 ifindex;
                    __u32 btf_vmlinux_value_type_id;
                    __u64 netns_dev;
                    __u64 netns_ino;
                    __u32 btf_id;
                    __u32 btf_key_type_id;
                    __u32 btf_value_type_id;
                } __attribute__((aligned(8)));
             */

            public int type;
            public int id;
            public int key_size;
            public int value_size;
            public int max_entries;
            public int map_flags;
            public byte[] name = new byte[16]; // BPF_OBJ_NAME_LEN is typically 16
            public int ifindex;
            public int btf_vmlinux_value_type_id;
            public long netns_dev;
            public long netns_ino;
            public int btf_id;
            public int btf_key_type_id;
            public int btf_value_type_id;

            @Override
            protected List<String> getFieldOrder()
            {
                return Arrays.asList("type", "id", "key_size", "value_size", "max_entries", "map_flags", "name", "ifindex", "btf_vmlinux_value_type_id", "netns_dev", "netns_ino", "btf_id", "btf_key_type_id", "btf_value_type_id" );
            }
        }

        void bpf_map_get_next_id(int start_id, IntByReference next_id) throws LastErrorException;
        int bpf_map_get_fd_by_id(int id) throws LastErrorException;

        void bpf_obj_get_info_by_fd(int bpf_fd, BpfMapInfo info, IntByReference info_len) throws LastErrorException;
        int bpf_map_get_next_key(int fd, Pointer key, Pointer next_key);
        void bpf_map_lookup_elem(int fd, Pointer key, Pointer value) throws LastErrorException;
        void bpf_map_delete_elem(int fd, Pointer key) throws LastErrorException;
    }

    public String getMapName(int fd) throws LastErrorException
    {
        LibBpf.BpfMapInfo info = new LibBpf.BpfMapInfo();
        IntByReference lenRef = new IntByReference(info.size());

        LibBpf.INSTANCE.bpf_obj_get_info_by_fd(fd, info, lenRef);

        return new String(info.name, StandardCharsets.UTF_8);
    }

    public int getMapFdByName(String name)
    {
        int returnFd = -1;
        try
        {
            int id = 0;
            IntByReference nextId = new IntByReference();
            int count = 1024; // Make sure we exit
            while (count-- > 0)
            {
                LibBpf.INSTANCE.bpf_map_get_next_id(id, nextId);
                id = nextId.getValue();
                int thisFd = LibBpf.INSTANCE.bpf_map_get_fd_by_id(id);
                String mapName = getMapName(thisFd);
                if (mapName.trim().equalsIgnoreCase(name.trim()))
                {
                    messages.addDebug("Found matching map id: " + id);
                    // If there is more than one with the same name then use the last one
                    returnFd = thisFd;
                }
            }
        }
        catch (LastErrorException e)
        {
            if (e.getErrorCode() != 2) // No such file or directory
            {
                messages.addError("Native error occurred when looking for map " + name, e);
            }
        }
        return returnFd;
    }

    public List<Pair<RemoteAddress,Integer>> getAddressToCountData(int mapFd)
    {
        List<Pair<RemoteAddress,Integer>> results = new ArrayList<>();
        BiConsumer<Pointer, Pointer> resultAdder = (key, value) ->
                results.add(Pair.of(makeRemoteAddress(key), value.getInt(0)));
        getData(mapFd, resultAdder);
        return results;
    }

    public List<Pair<RemoteAddress,Integer>> getAddressToPidData(int mapFd)
    {
        List<Pair<RemoteAddress,Integer>> results = new ArrayList<>();
        BiConsumer<Pointer, Pointer> resultAdder = (key, value) ->
                results.add(Pair.of(makeRemoteAddress(key), value.getInt(0)));
        getData(mapFd, resultAdder);
        return results;
    }

    private void getData(int mapFd, BiConsumer<Pointer, Pointer> resultAdder)
    {
        if (mapFd == -1)
        {
            return;
        }

        try
        {
            Pointer current_key = new Memory(Integer.BYTES);
            Pointer next_key = new Memory(Integer.BYTES);
            Pointer value = new Memory(Long.BYTES);

            boolean isFirst = true;
            boolean isLast = false;
            while (!isLast)
            {
                if (isFirst)
                {
                    current_key.setInt(0, 0);
                }

                int ret = LibBpf.INSTANCE.bpf_map_get_next_key(mapFd,current_key,next_key);
                isLast = (ret != 0);

                if (!isLast)
                {
                    LibBpf.INSTANCE.bpf_map_lookup_elem(mapFd, next_key, value);
                    resultAdder.accept(next_key,value);
                }

                if (!isFirst)
                {
                    LibBpf.INSTANCE.bpf_map_delete_elem(mapFd, current_key);
                }

                current_key.setInt(0, next_key.getInt(0));
                isFirst = false;
            }
        }
        catch (LastErrorException e)
        {
            messages.addError("Native error occurred when querying map", e);
        }
    }

    private RemoteAddress makeRemoteAddress(Pointer address)
    {
        int octet1 = address.getByte(0) & 0xFF;
        int octet2 = address.getByte(1) & 0xFF;
        int octet3 = address.getByte(2) & 0xFF;
        int octet4 = address.getByte(3) & 0xFF;
        return new RemoteAddress((byte) octet1, (byte) octet2, (byte) octet3, (byte) octet4);
    }
}
