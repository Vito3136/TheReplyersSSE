package com.example.vulnapp.controller;

import org.springframework.ui.Model;
import com.example.vulnapp.model.User;
import com.example.vulnapp.repository.Database;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String settings(HttpSession s, Model m) {
        User u = requireLogin(s);
        m.addAttribute("current", u.getUsername());
        return "settings";
    }

    @PostMapping("/changeUsername")
    public String change(@RequestParam String newName, HttpSession s) {
        User u = requireLogin(s);
        try { Database.changeUsername(u.getId(), newName); }
        catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Change username error", e);
        }
        u.setUsername(newName);
        return "redirect:/settings?done";
    }

    private User requireLogin(HttpSession s) {
        User u = (User) s.getAttribute("user");
        if (u == null) throw new ResponseStatusException(HttpStatus.FOUND, "redirect:/login");
        return u;
    }
}

