package com.github.vgaj.phd.server.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Pair<K,V>
{
    public static <K,V> Pair of (K key, V value)
    {
        Pair p = new Pair();
        p.key = key;
        p.value = value;
        return p;
    }
    private K key;
    private V value;
}
