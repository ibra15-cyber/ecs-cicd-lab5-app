package com.ibra.simple_full_stack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index.html";
    }

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload.html";
    }

    @GetMapping("/gallery")
    public String galleryPage() {
        return "gallery.html";
    }
}