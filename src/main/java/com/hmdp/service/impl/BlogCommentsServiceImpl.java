package com.hmdp.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.BlogComments;
import com.hmdp.mapper.BlogCommentsMapper;
import com.hmdp.service.IBlogCommentsService;

@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
