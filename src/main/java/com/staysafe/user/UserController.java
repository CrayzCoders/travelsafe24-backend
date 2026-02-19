package com.staysafe.user;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @GetMapping
    public String getName() {
        return "hi";
    }
}
