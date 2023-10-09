package com.hmdp.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import cn.hutool.http.HttpStatus;

/**
 * @Author: liwei515
 * @CreateTime: 2023-09-28
 * @Description: 登录拦截器
 * @Version: 1.0
 */
public class LoginInterceptor implements HandlerInterceptor {

  /**
   * 前置拦截器，用于判断用户是否登录
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // 1 判断是否需要拦截（ThreadLocal中是否有用户）
    if (UserHolder.getUser() == null) {
      // 没有，需要拦截，设置状态码
      response.setStatus(HttpStatus.HTTP_UNAUTHORIZED);
      // 拦截
      return false;
    }
    // 有用户，则放行
    return true;
  }
}
