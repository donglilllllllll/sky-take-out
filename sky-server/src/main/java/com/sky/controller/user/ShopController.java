package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.ShopStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.controller.admin.user
 * @Project：sky-take-out
 * @name：ShopController
 * @Date：2024/7/2 23:45
 * @Filename：ShopController
 */
@RestController("UserShopController")
@RequestMapping("/user/shop")
@Slf4j
public class ShopController {
    @Autowired
    private ShopStatusService shopStatusService;

    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = shopStatusService.getStatus();
        log.info("查询店铺状态===={}", status == 1 ? "营业" : "打烊");
        return Result.success(status);
    }
}
