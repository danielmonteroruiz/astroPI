package com.astropi.astropi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sirve la aplicacion React desde Spring Boot cuando se despliega como una sola app.
 */
@Controller
public class FrontendController {

    @GetMapping({
            "/",
            "/login",
            "/dashboard",
            "/admin",
            "/reset-password"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
