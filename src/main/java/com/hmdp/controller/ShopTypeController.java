package com.hmdp.controller;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.hmdp.dto.Result;
import com.hmdp.service.IShopTypeService;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService shopTypeService;

    @GetMapping("/list")
    public Result queryTypeList() {
        Result shopTypeList = shopTypeService.queryShopTypeList();
        return Result.ok(shopTypeList);
    }
}
