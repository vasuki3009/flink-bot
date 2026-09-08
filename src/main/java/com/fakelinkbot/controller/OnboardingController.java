package com.fakelinkbot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/onboard")
public class OnboardingController {

    @Value("${twilio.sandbox.keyword:join pile-water}")
    private String sandboxKeyword;

    @Value("${twilio.whatsapp.number:whatsapp:+14155238886}")
    private String twilioNumber;

    @GetMapping("/twilio-info")
    public Map<String, String> getTwilioInfo(@RequestParam String userEmail) {
        String displayNumber = twilioNumber.replace("whatsapp:", "");
        return Map.of(
                "sandboxNumber", displayNumber,
                "joinMessage",   sandboxKeyword
        );
    }
}