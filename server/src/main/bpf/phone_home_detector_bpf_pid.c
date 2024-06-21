/*
TODO: deal with GPL
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

char _license[] SEC("license") = "GPL";