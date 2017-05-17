package com.zalora.aloha.memcached;

import lombok.*;

import java.io.Serializable;
import javax.persistence.*;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cache")
public class MemcachedItem implements Serializable {

    private static final long serialVersionUID = 7503234879985469265L;

    @Id
    @Column(name = "id_cache")
    private String key;

    @Column(name = "data", columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] data;

    @Column(name = "flags", columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    private long flags;

    @Column(name = "expire", columnDefinition = "BIGINT DEFAULT 0", nullable = false)
    private long expire;

    public MemcachedItem(String key) {
        this.key = key;
    }

}
