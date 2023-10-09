package com.hmdp.utils;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import com.hmdp.dto.UserDTO;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;
import cn.hutool.core.bean.BeanUtil;

/**
 * @Author: liwei515
 * @CreateTime: 2023-10-08
 * @Description: 如果要使用redis作为缓存对象信息，首先拦截器要获取到stringRedisTemplate对象，才能调用缓存api
 * @Version: 1.0
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {

  private StringRedisTemplate stringRedisTemplate;

  /**
   * 构造方法注入
   */
  public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    // 1 获取请求头中的token，并判断token是否存在
    String token = request.getHeader("authorization");
    if (!StringUtils.hasText(token)) {
      // token不存在，说明当前用户未登录，不需要刷新直接放行
      return true;
    }
    // 2 基于TOKEN获取redis中的用户
    String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
    Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);
    if (userMap.isEmpty()) {
      // 用户不存在，说明当前用户未登录，不需要刷新直接放行
      return true;
    }
    // 3 用户存在，将查询到的hash数据转为UserDTO
    UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
    // 4 保存用户信息到 ThreadLocal
    UserHolder.saveUser(userDTO);
    // 5 刷新token有效期
    stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
    // 6 已刷新，放行
    return true;
  }
}
