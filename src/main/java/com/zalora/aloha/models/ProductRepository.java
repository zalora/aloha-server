package com.zalora.aloha.models;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import javax.transaction.Transactional;

/**
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
@Component
@Transactional(rollbackOn = Exception.class)
public interface ProductRepository extends PagingAndSortingRepository<CacheItem, String> {}
