package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ShopStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.controller.admin
 * @Project：sky-take-out
 * @name：ShopController
 * @Date：2024/7/2 23:23
 * @Filename：ShopController
 */
@Slf4j
@RestController("AdminShopController")
@RequestMapping("/admin/shop")
public class ShopController {

    @Autowired
    private ShopStatusService shopStatusService;

    /**
     * 修改店铺状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    public Result updateStatus(@PathVariable Integer status) {
        log.info("修改店铺状态:{}", status == 1 ? "营业" : "打烊");//1营业 2打烊
        shopStatusService.setStatus(status);
        return Result.success();
    }
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = shopStatusService.getStatus();
        log.info("查询店铺状态===={}", status == 1 ? "营业" : "打烊");
        return Result.success(status);
    }




}
