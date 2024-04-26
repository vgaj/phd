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
#include <linux/pkt_cls.h>
#include <bpf/bpf_helpers.h>
#include <linux/if_ether.h>
#include <linux/in.h>
#include <linux/ip.h>

#define MAP_NAME phd_ip_to_bytes
struct 
{
        __uint(type, BPF_MAP_TYPE_HASH);
        __type(key, __u32);
        __type(value, __u32);
        __uint(max_entries, 1000000);
} MAP_NAME SEC(".maps");

SEC("phone_home_detector_bpf_count")
int phone_home_detector_bpf_count_func(struct __sk_buff *skb)
{
    void *data_end = (void *)(long)skb->data_end;
    void *data = (void *)(long)skb->data;

    if (data + sizeof(struct ethhdr) + sizeof(struct iphdr) < data_end)
    {
        struct ethhdr *eth = data;
    
        // Is it an IP packet?
        if (eth->h_proto == __constant_htons(ETH_P_IP)) 
        {
            struct iphdr *ip = data + sizeof(struct ethhdr);
            
            __u32 daddr = ip->daddr;
            
            // TODO exclude UDP or TCP header
            __u32 length = data_end - data - sizeof(struct ethhdr);// - sizeof(struct iphdr);
            
            __u32 *value = bpf_map_lookup_elem(&MAP_NAME, &daddr);
            if (value)
            {
                *value += length;
            }
            else
            {
            	bpf_map_update_elem(&MAP_NAME, &daddr, &length, BPF_NOEXIST);
            }
        }
    }
    return TC_ACT_UNSPEC;
}

