package com.coresaken.mcserverlist.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DontSleepController {
    @PostMapping("/dont-sleep-buddy")
    public void receiveTestMessage(@RequestBody String message) {
        System.out.println("Received message: " + message);
    }
}
