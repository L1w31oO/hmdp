package com.hmdp.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisData;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TTL;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @Autowired
  private CacheClient cacheClient;

  public static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

  /**
   * v1.0
   * 根据id查询商铺数据（查询时，重建缓存）
   */
//  @Override
//  public Result queryById(Long id) {
//    // 1 从Redis中查询店铺数据
//    // 1.1 组装key
//    String key = RedisConstants.CACHE_SHOP_KEY + id;
//    // 1.2 查询
//    String shopJson = stringRedisTemplate.opsForValue().get(key);
//    Shop shop = null;
//    // 2 判断缓存是否命中
//    if (StringUtils.hasText(shopJson)) {
//      // 2.1 缓存命中，直接返回店铺数据
//      shop = JSONUtil.toBean(shopJson, Shop.class);
//      return Result.ok(shop);
//    }
//    // 2.2 缓存未命中，从数据库中查询店铺数据
//    shop = getById(id);
//    // 3 判断数据库是否存在店铺数据
//    if (Objects.isNull(shop)) {
//      // 3.1 数据库中不存在，返回失败信息
//      return Result.fail("店铺不存在");
//    }
//    // 3.2 数据库中存在，写入Redis，并返回店铺数据
//    stringRedisTemplate.opsForValue()
//        .set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//    return Result.ok(shop);
//  }

  /**
   * v2.0 考虑缓存穿透，基于缓存空对象解决缓存穿透
   * 根据id查询商铺数据（查询时，重建缓存）
   */
//  @Override
//  public Result queryById(Long id) {
//    // 1 从Redis中查询店铺数据
//    // 1.1 组装key
//    String key = RedisConstants.CACHE_SHOP_KEY + id;
//    // 1.2 查询
//    String shopJson = stringRedisTemplate.opsForValue().get(key);
//    Shop shop = null;
//    // 2 判断缓存是否命中
//    if (StringUtils.hasText(shopJson)) {
//      // 2.1 缓存命中，直接返回店铺数据
//      shop = JSONUtil.toBean(shopJson, Shop.class);
//      return Result.ok(shop);
//    }
//    // 2.2 缓存未命中
//    // 2.2.1 判断缓存中查询的数据是否是空字符串(hasText把null和空字符串给排除了)
//    if (Objects.nonNull(shopJson)) {
//      // 2.2.2 当前数据是空字符串（说明该数据是之前缓存的空对象），直接返回失败信息
//      return Result.fail("店铺不存在");
//    }
//    // 2.2.3 当前数据是null，从数据库中查询店铺数据
//    shop = getById(id);
//    // 3 判断数据库是否存在店铺数据
//    if (Objects.isNull(shop)) {
//      // 3.1 数据库中不存在
//      // 3.1.1 缓存空对象（解决缓存穿透）
//      stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//      // 3.1.2 返回失败信息
//      return Result.fail("店铺不存在");
//    }
//    // 3.2 数据库中存在，写入Redis，并返回店铺数据
//    stringRedisTemplate.opsForValue()
//        .set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//    return Result.ok(shop);
//  }

  /**
   * v3.0 考虑缓存击穿，基于互斥锁解决缓存击穿
   * 根据id查询商铺数据（查询时，重建缓存）
   */
//  @Override
//  public Result queryById(Long id) {
//    String key = RedisConstants.CACHE_SHOP_KEY + id;
//    // 1 从Redis中查询店铺数据，并判断缓存是否命中
//    Result result = getShopFromCache(key);
//    if (Objects.nonNull(result)) {
//      // 缓存命中，直接返回
//      return result;
//    }
//    // 2 缓存未命中，需要重建缓存，判断能否能够获取互斥锁
//    String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
//    try {
//      boolean lock = tryLock(lockKey);
//      if (!lock) {
//        // 2.1 获取锁失败，已有线程在重建缓存，则休眠递归重试
//        Thread.sleep(50);
//        return queryById(id);
//      }
//      // 2.2 获取锁成功，判断缓存是否重建，防止堆积的线程全部请求数据库（所以说双检是很有必要的）
//      result = getShopFromCache(key);
//      if (Objects.nonNull(result)) {
//        // 缓存命中，直接返回
//        return result;
//      }
//      // 3 从数据库中查询店铺数据，并判断数据库是否存在店铺数据
//      Shop shop = getById(id);
//      if (Objects.isNull(shop)) {
//        // 3.1 数据库中不存在
//        // 3.1.1 缓存空对象（解决缓存穿透）
//        stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
//        // 3.1.2 返回失败信息
//        return Result.fail("店铺不存在");
//      }
//      // 3.2 数据库中存在，写入Redis，并返回店铺数据
//      stringRedisTemplate.opsForValue()
//          .set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//      return Result.ok(shop);
//    } catch (Exception e) {
//      throw new RuntimeException("服务异常");
//    } finally {
//      // 5、释放锁（释放锁一定要记得放在finally中，防止死锁）
//      unlock(lockKey);
//    }
//  }

  /**
   * v4.0 考虑缓存击穿，基于逻辑过期解决缓存击穿
   * 根据id查询商铺数据（查询时，重建缓存）
   */
//  @Override
//  public Result queryById(Long id) {
//    String key = RedisConstants.CACHE_SHOP_KEY + id;
//    // 1 从Redis中查询店铺数据，并判断缓存是否命中
//    String shopJson = stringRedisTemplate.opsForValue().get(key);
//    if (!StringUtils.hasText(shopJson)) {
//      // 1.1 缓存未命中，返回空
//      return null;
//    }
//    // 1.2 缓存命中，将JSON字符串反序列化未对象，并判断缓存数据是否逻辑过期
//    RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
//    // 这里需要先转成JSONObject再转成反序列化，否则可能无法正确映射Shop的字段
//    JSONObject data = (JSONObject) redisData.getData();
//    Shop shop = JSONUtil.toBean(data, Shop.class);
//    LocalDateTime expireTime = redisData.getExpireTime();
//    if (expireTime.isAfter(LocalDateTime.now())) {
//      // 当前缓存数据未过期，直接返回
//      return Result.ok(shop);
//    }
//    // 2 缓存数据已过期，获取互斥锁，并且重建缓存
//    String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
//    boolean lock = tryLock(lockKey);
//    if (lock) {
//      // 获取锁成功，开启一个子线程去重建缓存
//      CACHE_REBUILD_EXECUTOR.submit(() -> {
//        try {
//          this.saveShopToCache(id, RedisConstants.CACHE_SHOP_LOGICAL_TTL);
//        } finally {
//          // 释放锁
//          unlock(lockKey);
//        }
//      });
//    }
//    // 4、返回数据
//    return Result.ok(shop);
//  }

  /**
   * v5.0 根据id查询商铺数据（查询时，重建缓存）
   */
  @Override
  public Result queryById(Long id) {
    // 解决缓存穿透
    Shop shop = cacheClient
        .queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

    // 互斥锁解决缓存击穿
    // Shop shop = cacheClient
    //         .queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

    // 逻辑过期解决缓存击穿
    // Shop shop = cacheClient
    //         .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);

    if (shop == null) {
      return Result.fail("店铺不存在！");
    }
    // 返回
    return Result.ok(shop);
  }

  /**
   * 从缓存中获取店铺数据
   */
  private Result getShopFromCache(String key) {
    String shopJson = stringRedisTemplate.opsForValue().get(key);
    // 判断缓存是否命中
    if (StringUtils.hasText(shopJson)) {
      // 缓存数据有值，说明缓存命中了，直接返回店铺数据
      Shop shop = JSONUtil.toBean(shopJson, Shop.class);
      return Result.ok(shop);
    }
    // 判断缓存中查询的数据是否是空字符串(isNotBlank把 null 和 空字符串 给排除了)
    if (Objects.nonNull(shopJson)) {
      // 当前数据是空字符串，说明缓存也命中了（该数据是之前缓存的空对象），直接返回失败信息
      return Result.fail("店铺不存在");
    }
    // 缓存未命中（缓存数据既没有值，又不是空字符串）
    return null;
  }

  /**
   * 获取锁
   */
  private boolean tryLock(String lockKey) {
    Boolean lock = stringRedisTemplate.opsForValue()
        .setIfAbsent(lockKey, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
    return BooleanUtil.isTrue(lock);
  }

  /**
   * 释放锁
   */
  private void unlock(String lockKey) {
    stringRedisTemplate.delete(lockKey);
  }

  public void saveShopToCache(Long id, Long expireSeconds) {
    // 从数据库中查询店铺数据
    Shop shop = getById(id);
    // 封装逻辑过期数据
    RedisData redisData = new RedisData();
    redisData.setData(shop);
    redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
    // 将逻辑过期数据存入Redis中
    stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
  }

  /**
   * 更新商铺数据（更新时，更新数据库，删除缓存）
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public Result updateShop(Shop shop) {
    // 参数校验 略
    // 1 更新数据库
    updateById(shop);
    // 2 删除缓存
    stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());
    return Result.ok();
  }

}
