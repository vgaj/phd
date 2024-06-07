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

#include <linux/bpf.h>
#include <bpf/bpf_helpers.h>

#define MAP_NAME phd_pid_to_time
struct
{
    __uint(type, BPF_MAP_TYPE_HASH);
    __type(key, __u32);   // pid
    __type(value, __u64); // time
    __uint(max_entries, 1000000);
} MAP_NAME SEC(".maps");

SEC("tracepoint/syscalls/sys_enter_connect")
int phone_home_detector_bpf_time_func(struct trace_event_raw_sys_enter *ctx)
{
    __u32 pid = bpf_get_current_pid_tgid() >> 32;
    __u64 connect_time = bpf_ktime_get_ns();
    bpf_map_update_elem(&MAP_NAME, &pid, &connect_time, BPF_NOEXIST);
    return 0;
}
