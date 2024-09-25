package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation(value = "文件上传")
    public Result<String> uploda(MultipartFile file) {
        log.info("文件上传：{}", file);
        try {
            //获取源文件名称
            String originalFilename = file.getOriginalFilename();
            //获取文件后缀
            int index = originalFilename.lastIndexOf(".");
            // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
            String objectName = UUID.randomUUID().toString() + originalFilename.substring(index);
            String upload = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(upload);
        } catch (IOException e) {
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }
    }

}
