package com.hmdp.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.hmdp.utils.RedisConstants;
import cn.hutool.json.JSONUtil;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  /**
   * String缓存策略实现 设置过期时间
   */
  @Override
  public Result queryShopTypeList() {
    // 1 从Redis中查询店铺类型
    String shopTypeJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_TYPE_KEY);
    // 2 判断缓存是否命中
    List<ShopType> shopTypeList = null;
    if (StringUtils.hasText(shopTypeJson)) {
      // 2.1 缓存命中，直接返回缓存数据
      shopTypeList = JSONUtil.toList(shopTypeJson, ShopType.class);
      return Result.ok(shopTypeList);
    }
    // 2.1 缓存未命中，查询数据库
    shopTypeList = query().orderByAsc("sort").list();
    // 3 判断数据库中是否存在该数据
    if (CollectionUtils.isEmpty(shopTypeList)) {
      // 3.1 数据库中不存在该数据，返回失败信息
      return Result.fail("店铺分类不存在");
    }
    // 3.2 店铺数据存在，写入Redis，并返回查询的数据
    stringRedisTemplate.opsForValue()
        .set(RedisConstants.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopTypeList), RedisConstants.CACHE_SHOP_TYPE_TTL,
            TimeUnit.MINUTES);
    return Result.ok(shopTypeList);
  }

  /**
   * List缓存策略实现 不设置过期时间
   */
  public Result queryShopTypeListV2() {
    // 1 从Redis中查询店铺类型
    List<String> shopTypeJsonList = stringRedisTemplate.opsForList().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);
    // 2 判断缓存是否命中
    if (!CollectionUtils.isEmpty(shopTypeJsonList)) {
      // 2.1 缓存命中，直接返回缓存数据
      List<ShopType> shopTypeList = new ArrayList<>();
      for (String shopTypeJson : shopTypeJsonList) {
        shopTypeList.add(JSONUtil.toBean(shopTypeJson, ShopType.class));
      }
      // stream流方式
//      List<ShopType> shopTypeList = shopTypeJsonList.stream().map((shopTypeJson) -> JSONUtil.toBean(shopTypeJson, ShopType.class)).collect(Collectors.toList());
      return Result.ok(shopTypeList);
    }
    // 2.1 缓存未命中，查询数据库
    List<ShopType> shopTypes = query().orderByAsc("sort").list();
    // 3 判断数据库中是否存在该数据
    if (CollectionUtils.isEmpty(shopTypes)) {
      // 3.1 数据库中不存在该数据，返回失败信息
      return Result.fail("店铺分类不存在");
    }
    // 3.2 店铺数据存在，写入Redis，并返回查询的数据
    for (ShopType shopType : shopTypes) {
      stringRedisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopType));
    }
    // stream流方式
//    List<String> shopTypeJsons = shopTypes.stream().map(JSONUtil::toJsonStr).collect(Collectors.toList());
//    stringRedisTemplate.opsForList().rightPushAll(RedisConstants.CACHE_SHOP_TYPE_KEY, shopTypeJsons);
    return Result.ok(shopTypes);
  }

  /**
   * Zset缓存策略实现 不设置过期时间
   */
  public Result queryShopTypeListV3() {
    // 1 从Redis中查询店铺类型
    Set<String> shopTypeJsonSet = stringRedisTemplate.opsForZSet().range(RedisConstants.CACHE_SHOP_TYPE_KEY, 0, -1);
    // 2 判断缓存是否命中
    if (!CollectionUtils.isEmpty(shopTypeJsonSet)) {
      // 2.1 缓存命中，直接返回缓存数据
      List<ShopType> shopTypeList = new ArrayList<>();
      for (String shopTypeJson : shopTypeJsonSet) {
        shopTypeList.add(JSONUtil.toBean(shopTypeJson, ShopType.class));
      }
      // stream流方式
//      List<ShopType> shopTypeList = shopTypeJsonSet.stream().map((shopTypeJson) -> JSONUtil.toBean(shopTypeJson, ShopType.class)).collect(Collectors.toList());
      return Result.ok(shopTypeList);
    }
    // 2.1 缓存未命中，查询数据库
    List<ShopType> shopTypes = query().orderByAsc("sort").list();
    // 3 判断数据库中是否存在该数据
    if (CollectionUtils.isEmpty(shopTypes)) {
      // 3.1 数据库中不存在该数据，返回失败信息
      return Result.fail("店铺分类不存在");
    }
    // 3.2 店铺数据存在，写入Redis，并返回查询的数据
    for (ShopType shopType : shopTypes) {
      stringRedisTemplate.opsForZSet().add(RedisConstants.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopType), shopType.getSort());
    }
    return Result.ok(shopTypes);
  }
}
