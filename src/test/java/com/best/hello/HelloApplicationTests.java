package com.best.hello;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@SpringBootTest
class HelloApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    @GetMapping("/uploadFile")
    public String test(@RequestParam MultipartFile uploadFile) throws IOException {
        // 原文件名称
        System.out.println("uploadFile.getOriginalFilename() = " + uploadFile.getOriginalFilename());
        // 文件的接收参数 @RequestParam MultipartFile uploadFile中的 uploadFile
        System.out.println("uploadFile.getName() = " + uploadFile.getName());
        // 文件的类型
        System.out.println("uploadFile.getContentType() = " + uploadFile.getContentType());
        System.out.println("uploadFile.getResource() = " + uploadFile.getResource());
        System.out.println("uploadFile.getBytes() = " + uploadFile.getBytes());
        // 文件大小
        System.out.println("uploadFile.getSize() = " + uploadFile.getSize());
        return "ok";
    }

}
