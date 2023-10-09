package com.hmdp.config;

import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;

/**
 * @Author: liwei515
 * @CreateTime: 2023-09-28
 * @Description: mvc拦截器
 * @Version: 1.0
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {


  /**
   * new出来的对象是无法直接注入IOC容器的（RefreshTokenInterceptor是直接new出来的） 所以这里需要在配置类中注入，然后通过构造器传入到当前类中
   */
  @Resource
  private StringRedisTemplate stringRedisTemplate;

  /**
   * 拦截器注册
   * @param registry 注册器
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 1 登录拦截器
    registry.addInterceptor(new LoginInterceptor())
        // 设置放行请求
        .excludePathPatterns(
            "/shop/**",
            "/voucher/**",
            "/shop-type/**",
            "/upload/**",
            "/blog/hot",
            "/user/code",
            "/user/login"
        )
        .order(1);
    // 优先级默认都是0，值越大优先级越低
    // 2 token刷新的拦截器
    registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
        .addPathPatterns("/**")
        .order(0);
  }


}
