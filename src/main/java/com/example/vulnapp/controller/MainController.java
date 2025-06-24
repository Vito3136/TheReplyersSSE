package com.example.vulnapp.controller;

import com.example.vulnapp.model.Upload;
import com.example.vulnapp.model.User;
import com.example.vulnapp.repository.Database;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class MainController {

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("username", user.getUsername());
        return "index";
    }

    @PostMapping("/message")
    public String saveMessage(@RequestParam("message") String message, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            Database.addUpload(null, message, user.getId()); // stored XSS possible
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/uploads";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Database.addUpload(file.getOriginalFilename(), content, user.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/uploads";
    }

    @GetMapping("/uploads")
    public String viewUploads(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            List<Upload> uploads = Database.getAllUploads();
            model.addAttribute("uploads", uploads);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "uploads";
    }
}
