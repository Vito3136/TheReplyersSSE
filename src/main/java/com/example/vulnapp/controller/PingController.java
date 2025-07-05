package com.example.vulnapp.controller;

import com.example.vulnapp.repository.Database;
import org.springframework.ui.Model;
import com.example.vulnapp.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
public class PingController {

    @GetMapping("/users")
    public String users(HttpSession s, Model m) throws SQLException {
        String csrfToken = UUID.randomUUID().toString();
        s.setAttribute("csrfToken", csrfToken);
        m.addAttribute("csrfToken", csrfToken);
        User me = requireLogin(s);
        m.addAttribute("page", "users");
        m.addAttribute("users", Database.getOtherUsers(me.getId()));
        return "users";
    }

    @PostMapping("/ping")
    public String send(@RequestParam long toId,
                       @RequestParam("csrfToken") String csrfToken,
                       HttpSession s) {
        String sessionToken = (String) s.getAttribute("csrfToken");
        if (sessionToken == null || !sessionToken.equals(csrfToken)) {
            throw new SecurityException("Invalid CSRF token");
        }
        User me = requireLogin(s);
        try {
            Database.addPing(me.getId(), toId);
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Send ping error", e);
        }
        return "redirect:/users";
    }

    @GetMapping("/pings")
    public String received(HttpSession s, Model m) throws SQLException {
        User me = requireLogin(s);
        m.addAttribute("page", "pings");
        m.addAttribute("pings", Database.getSenders(me.getId()));
        return "pings";
    }

    private User requireLogin(HttpSession s) {
        User u = (User) s.getAttribute("user");
        if (u == null) throw new ResponseStatusException(HttpStatus.FOUND, "redirect:/login");
        return u;
    }
}

