package com.hmdp.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author: liwei515
 * @CreateTime: 2023-10-10
 * @Description: ShopServiceImplTest
 * @Version: 1.0
 */
@SpringBootTest
class ShopServiceImplTest {

  @Autowired
  private ShopServiceImpl shopService;

  /**
   * 预热数据，正常开发中，为热点key设置逻辑过期，然后在某段时间手动删除缓存中逻辑过期的key
   */
  @Test
  void loadShopToCache() {
    shopService.saveShopToCache( 1L, 10L);
  }

}