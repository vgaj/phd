/*
Copyright (c) 2022-2024 Viru Gajanayake

This program is dual-licensed under the MIT License and the GNU General Public License (GPL), at your option. You may choose either license or both.

MIT License:
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

GPL License:
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This package is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>

You may choose to license your copy of the Software under either the MIT License, the GPL License, or both.
*/

#include <linux/bpf.h>
#include <bpf/bpf_helpers.h>
#include <linux/ptrace.h>
#include <linux/in.h>

struct sys_enter_connect_args {
    char _[16];
    long fd;
    long addr_ptr;
    long addrlen;
};

#define MAP_NAME phd_ip_to_pid
struct
{
    __uint(type, BPF_MAP_TYPE_HASH);
    __type(key, __u32);   // addr
    __type(value, __u32); // pid
    __uint(max_entries, 1000000);
} MAP_NAME SEC(".maps");

SEC("tracepoint/syscalls/sys_enter_connect")
int phone_home_detector_bpf_pid_func(struct sys_enter_connect_args *ctx)
{
    __u32 ip;
    struct sockaddr_in *sock_addr = (struct sockaddr_in *)ctx->addr_ptr;
    bpf_probe_read_user(&ip, sizeof(ip), &sock_addr->sin_addr.s_addr);

    __u32 pid = bpf_get_current_pid_tgid() >> 32;

    if (ip > 0)
    {
        bpf_map_update_elem(&MAP_NAME, &ip, &pid, BPF_NOEXIST);
    }

    return 0;
}

char _license[] SEC("license") = "Dual MIT/GPL";