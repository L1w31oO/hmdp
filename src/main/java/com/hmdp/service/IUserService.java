package com.hmdp.service;

import javax.servlet.http.HttpSession;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;

public interface IUserService extends IService<User> {

  /**
   * 发送手机验证码
   */
  Result sendCode(String phone, HttpSession session);

  Result login(LoginFormDTO loginFormDTO, HttpSession session);

}
