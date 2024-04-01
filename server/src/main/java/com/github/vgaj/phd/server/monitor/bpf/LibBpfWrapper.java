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

import com.github.vgaj.phd.server.messages.MessageData;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class LibBpfWrapper
{
    @Autowired
    private MessageData messages;

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
        int fd = -1;
        try
        {
            int id = 0;
            IntByReference nextId = new IntByReference();
            int count = 1024; // Make sure we exit
            while (count-- > 0)
            {
                LibBpf.INSTANCE.bpf_map_get_next_id(id, nextId);
                id = nextId.getValue();
                fd = LibBpf.INSTANCE.bpf_map_get_fd_by_id(id);
                String mapName = getMapName(fd);
                if (mapName.trim().equalsIgnoreCase(name.trim()))
                {
                    System.out.println("Found id is " + id);
                    break;
                }
            }
        }
        catch (LastErrorException e)
        {
            messages.addError("Native error occurred when looking for map " + name, e);
            fd = -1;
        }
        return fd;
    }

    public void printMap(String mapName)
    {
        try
        {
            System.out.println("Using fd :" + fd);
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

                int ret = LibBpf.INSTANCE.bpf_map_get_next_key(fd,current_key,next_key);
                isLast = (ret != 0);

                if (!isLast)
                {
                    LibBpf.INSTANCE.bpf_map_lookup_elem(fd, next_key, value);
                    long count = value.getLong(0);
                    int octet1 = next_key.getByte(0) & 0xFF;
                    int octet2 = next_key.getByte(1) & 0xFF;
                    int octet3 = next_key.getByte(2) & 0xFF;
                    int octet4 = next_key.getByte(3) & 0xFF;
                    System.out.println(String.format("Address: %d.%d.%d.%d, Bytes: %d", octet1, octet2, octet3, octet4, count));
                }

                if (!isFirst)
                {
                    LibBpf.INSTANCE.bpf_map_delete_elem(fd, current_key);
                }

                current_key.setInt(0, next_key.getInt(0));
                isFirst = false;
            }
        }
        catch (LastErrorException e)
        {
            messages.addError("Native error occurred when querying map " + mapName, e);
            fd = -1;
       }
    }

    int fd = -1;

    @Scheduled(fixedRateString = "1000", initialDelayString = "1000")
    public void doIt()
    {
        if (fd == -1)
        {
            fd = getMapFdByName("ip_to_bytes_map");
        }
        printMap("ip_to_bytes_map");
    }
}
