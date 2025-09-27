package org.example.catalogservice.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @Value("${catalog.message:Default message}")
    private String message;

    @GetMapping("/catalog/message")
    public String getMessage() {
        return message;
    }
}