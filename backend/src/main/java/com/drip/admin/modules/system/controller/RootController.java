package com.drip.admin.modules.system.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RootController {
    @GetMapping("/")
    public ResponseEntity<Void> root() {
        return ResponseEntity.status(302)
            .header(HttpHeaders.LOCATION, URI.create("/swagger-ui/index.html").toString())
            .build();
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
