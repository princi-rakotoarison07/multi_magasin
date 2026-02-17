package com.magasin.multi_magasin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {

    @GetMapping("/")
    public String root() {
        return "redirect:/multi_magasin/";
    }

    @GetMapping("/multi_magasin")
    public String multiMagasinNoSlash() {
        return "redirect:/multi_magasin/";
    }
}
