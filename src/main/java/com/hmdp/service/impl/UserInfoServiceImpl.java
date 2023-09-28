package com.hmdp.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.UserInfo;
import com.hmdp.mapper.UserInfoMapper;
import com.hmdp.service.IUserInfoService;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
