package com.example.vulnapp.controller;

import com.example.vulnapp.model.User;
import com.example.vulnapp.repository.Database;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String doSignup(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           Model model) {
        try {
            Database.createUser(username, password);
            model.addAttribute("msg", "User created! You can now login.");
            return "login";
        } catch (Exception ex) {
            model.addAttribute("error", "Error: " + ex.getMessage());
            return "signup";
        }
    }

    @GetMapping({"/login", "/"})
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          Model model,
                          HttpSession session) {
        try {
            User user = Database.validateUser(username, password);
            if (user != null) {
                session.setAttribute("user", user);
                return "redirect:/home";
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
        } catch (Exception ex) {
            model.addAttribute("error", "Error: " + ex.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
