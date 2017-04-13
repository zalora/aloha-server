package com.zalora.aloha.memcached;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Data
@NoArgsConstructor
public class MemcachedItem implements Serializable {

    private static final long serialVersionUID = 7503234879985469265L;

    private byte[] data;
    private long expire;
    private long flags;
    private String key;

    public MemcachedItem(String key) {
        this.key = key;
    }

    public MemcachedItem(byte[] data, long expire, long flags, String key) {
        this.data = data;
        this.expire = expire;
        this.flags = flags;
        this.key = key;
    }

}
