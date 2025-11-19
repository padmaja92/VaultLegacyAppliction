package com.vaultlegacy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class VaultLegacyController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/services")
    public String services() {
        return "services";
    }

    @PutMapping("/client-login")
    public String clientLogin(@RequestBody String credentials) {
        // Add authentication logic here
        return "redirect:/dashboard";
    }
}