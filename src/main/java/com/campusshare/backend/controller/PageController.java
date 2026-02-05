package com.campusshare.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Akan mencari file login.html
    }
    @GetMapping("/register")
    public String registerPage() {
        return "register"; // Akan mencari file register.html
    }
    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard"; // Akan mencari file dashboard.html
    }
    @GetMapping("/")
    public String homePage() {
        return "redirect:/login";
    }
}