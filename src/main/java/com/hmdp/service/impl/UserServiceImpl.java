package com.hmdp.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

  @Resource
  private StringRedisTemplate stringRedisTemplate;

  @Override
  public Result sendCode(String phone, HttpSession session) {
    // 1 校验手机号
    if (RegexUtils.isPhoneInvalid(phone)) {
      // 1.2 如果不符合，返回错误信息
      return Result.fail("手机号格式错误！");
    }
    // 2 生成验证码
    String code = RandomUtil.randomNumbers(6);
    // 3 保存验证码到redis
    try {
      stringRedisTemplate.opsForValue()
          .set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    // 4 发送验证码
    log.info("向手机号：{} 发送验证码 request -> {}", phone, code);
    log.info("发送验证码 code -> {}", code);
    log.info("向手机号：{} 发送验证码 response -> {}", phone, code);
    // 5 返回OK
    return Result.ok();
  }

  @Override
  public Result login(LoginFormDTO loginFormDTO, HttpSession session) {
    // 1 校验手机号
    String phone = loginFormDTO.getPhone();
    if (RegexUtils.isPhoneInvalid(phone)) {
      // 1.2 如果不符合，返回错误信息
      return Result.fail("手机号格式错误！");
    }
    // 2 从redis获取验证码并校验
    String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
    String code = loginFormDTO.getCode();
    if (Objects.isNull(cacheCode) || !Objects.equals(cacheCode, code)) {
      // 2.1 不一致，报错
      return Result.fail("验证码错误");
    }
    // 3 根据手机号查询用户 select * from tb_user where phone = ?
    User user = query().eq("phone", phone).one();
    // 4 判断用户是否存在
    if (Objects.isNull(user)) {
      // 4.1 不存在，创建新用户并保存
      user = createUserWithPhone(phone);
    }
    // 5 保存用户信息到 redis 中
    // 5.1 随机生成token，作为登录令牌
    String token = UUID.randomUUID().toString(true);
    // 5.2 将User对象转为HashMap存储
    UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
    Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
        CopyOptions.create()
            .setIgnoreNullValue(true)
            .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
    // 5.3 存储
    String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
    stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
    // 5.4 设置token有效期
    stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
    // 6.返回token
    return Result.ok(token);
  }

  private User createUserWithPhone(String phone) {
    // 1 创建用户
    User user = new User();
    user.setPhone(phone);
    user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
    // 2 保存用户
    save(user);
    // 3 返回创建的用户
    return user;
  }

}
