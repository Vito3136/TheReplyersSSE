package com.example.vulnapp.controller;

import com.example.vulnapp.model.Upload;
import com.example.vulnapp.model.User;
import com.example.vulnapp.repository.Database;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

@Controller
public class MainController {

    /*@GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("username", user.getUsername());
        return "index";
    }*/

    /*@PostMapping("/message")
    public String saveMessage(@RequestParam("message") String message, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        try {
            Database.addUpload(null, message, user.getId()); // stored XSS possible
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/uploads";
    }*/

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

    /*@GetMapping({"/home", "/quick"})
    public String quick(HttpSession session, Model m) {
        checkLogin(session);
        m.addAttribute("page", "quick");
        return "quick";
    }*/

    /*@GetMapping({"/home", "/quick"})
    public String quick(HttpSession s, Model m) {
        User u = requireLogin(s);
        m.addAttribute("page", "quick");
        try {
            m.addAttribute("messages", Database.getAllMessages());
        } catch (SQLException e) { m.addAttribute("error", e.getMessage()); }
        return "quick";
    }*/

    @GetMapping({"/home", "/quick"})
    public String quick(@RequestParam(required = false) String echo,   // <- nuovo
                        HttpSession session, Model model) {

        User u = requireLogin(session);
        model.addAttribute("page", "quick");

        // lista di tutti i messaggi  (stored XSS già esistente)
        try { model.addAttribute("messages", Database.getAllMessages()); }
        catch (SQLException e) { model.addAttribute("error", e.getMessage()); }

        // ① passa l’input appena digitato (reflected)
        model.addAttribute("echo", echo);
        return "quick";
    }

    /*@PostMapping("/message")
    public String saveMessage(@RequestParam String message, HttpSession s) {
        User u = requireLogin(s);
        try { Database.addMessage(message, u.getId()); }
        catch (SQLException ignored) {}
        return "redirect:/quick";
    }*/

    @PostMapping("/message")
    public String saveMessage(@RequestParam String message,
                              HttpSession session) {

        User u = requireLogin(session);

        // Stored XSS (come prima)
        try { Database.addMessage(message, u.getId()); } catch (SQLException ignored) {}

        // Reflected XSS → rimanda a /quick con il testo non filtrato
        return "redirect:/quick?echo=" +
                UriUtils.encode(message, StandardCharsets.UTF_8);
    }

    /* helper */
    private User requireLogin(HttpSession s) {
        User u = (User) s.getAttribute("user");
        if (u == null) throw new ResponseStatusException(HttpStatus.FOUND, "redirect:/login");
        return u;
    }

    @GetMapping("/upload")
    public String uploadPage(HttpSession session, Model m) {
        checkLogin(session);
        m.addAttribute("page", "upload");
        return "upload";
    }

    /** semplice util per centralizzare il redirect se non loggato */
    private void checkLogin(HttpSession s) {
        if (s.getAttribute("user") == null)
            throw new ResponseStatusException(HttpStatus.FOUND, "redirect:/login");
    }
}
