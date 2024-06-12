package com.best.hello.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/UPLOAD")
public class Upload {


    //Save the uploaded file to this folder
    private static String UPLOADED_FOLDER = "/tmp/";

    @GetMapping("/index")
    public String index() {
        return "upload";
    }

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


    @PostMapping("/upload")
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path dir = Paths.get(UPLOADED_FOLDER);
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            // Create parent dir if not exists
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.write(path, bytes);
            redirectAttributes.addFlashAttribute("message",
                    "You successfully uploaded '" + file.getOriginalFilename() + "'");

        } catch (Exception ex) {
            ex.printStackTrace();
            return ex.toString();
        }
        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }
}
