package com.hmdp.utils;

import com.hmdp.dto.UserDTO;

/**
 * ThreadLocal：为每个线程提供一份单独存储空间，只有在线程内才能获取对应的值。以下是使用ThreadLocal的基本工具类代码
 */
public class UserHolder {

  private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

  public static void saveUser(UserDTO user) {
    tl.set(user);
  }

  public static UserDTO getUser() {
    return tl.get();
  }

  public static void removeUser() {
    tl.remove();
  }
}
