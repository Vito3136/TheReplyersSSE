package com.example.vulnapp.controller;

import com.example.vulnapp.model.User;
import com.example.vulnapp.repository.Database;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class AuthController {

    @GetMapping("/signup")
    public String signupForm(HttpSession session, Model model) {
        String csrfToken = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", csrfToken);
        model.addAttribute("csrfToken", csrfToken);
        return "signup";
    }

    @PostMapping("/signup")
    public String doSignup(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("csrfToken") String csrfToken,
                           HttpSession session,
                           Model model) {
        try {
            String sessionToken = (String) session.getAttribute("csrfToken");
            if (sessionToken == null || !sessionToken.equals(csrfToken)) {
                throw new SecurityException("Invalid CSRF token");
            }

            Database.createUser(username, password);
            model.addAttribute("msg", "User created! You can now login.");
            return "redirect:/login";
        } catch (Exception ex) {
            model.addAttribute("error", "Error: " + ex.getMessage());
            return "signup";
        }
    }

    @GetMapping({"/login", "/"})
    public String loginForm(HttpSession session, Model model) {
        String csrfToken = UUID.randomUUID().toString();
        session.setAttribute("csrfToken", csrfToken);
        model.addAttribute("csrfToken", csrfToken);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("csrfToken") String csrfToken,
                          Model model,
                          HttpServletRequest request) {
        try {
            String sessionToken = (String) request.getSession().getAttribute("csrfToken");
            if (sessionToken == null || !sessionToken.equals(csrfToken)) {
                throw new SecurityException("Invalid CSRF token");
            }

            User user = Database.validateUser(username, password);
            if (user != null) {
                HttpSession oldSession = request.getSession(false);
                if (oldSession != null) {
                    oldSession.invalidate();
                }

                HttpSession newSession = request.getSession(true);
                newSession.setAttribute("user", user);
                return "redirect:/home";
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "login";
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error during login for user "
                    + username + ": " + ex.getMessage() + ex);
            model.addAttribute("error", "An unexpected error occurred. Please try again later.");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
