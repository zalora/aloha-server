package com.zalora.aloha.models;

import javax.persistence.*;
import lombok.*;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cache")
public class CacheItem {

    @Id
    @Getter @Setter
    @Column(name = "id_cache")
    private String id;

    @Getter @Setter
    @Column(name = "data", columnDefinition = "MEDIUMBLOB", nullable = false)
    private byte[] data;

    @Getter @Setter
    @Column(name = "flags", columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    private long flags;

    @Override
    public String toString() {
        return new String(data);
    }

}
