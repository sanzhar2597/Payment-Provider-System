package kz.ufo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/")
public class HomeController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping
    public String welcome() {
        return "Welcome to " + appName.toUpperCase();
    }

}