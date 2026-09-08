package com.fakelinkbot.controller;

import com.fakelinkbot.model.AnalysisResult;
import com.fakelinkbot.service.NlpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppController {

    @Autowired
    private NlpService nlpService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping(value = "/webhook", produces = "application/xml")
    public String receiveMessage(
            @RequestParam("Body") String message,
            @RequestParam("From") String from
    ) {
        System.out.println("📱 From: " + from);
        System.out.println("📝 Message: " + message);

        try {
            if (message == null || message.trim().isEmpty()) {
                return "<Response><Message>Please send a link to analyze!</Message></Response>";
            }

            AnalysisResult result = nlpService.analyze(message);

            if (result == null) {
                return "<Response><Message>⚠️ Could not analyze. Send a valid URL.</Message></Response>";
            }

            // Push to WebSocket → popup on dashboard
            String url = nlpService.extractUrl(message);
            Map<String, Object> alert = Map.of(
                    "level",      result.getLevel(),
                    "score",      result.getScore(),
                    "url",        url != null ? url : message,
                    "from",       from,
                    "analyzedBy", result.getAnalyzedBy(),
                    "reasons",    result.getReasons() != null ? result.getReasons() : List.of()
            );
            messagingTemplate.convertAndSend("/topic/alerts", alert);
            System.out.println("🚨 Alert sent: " + result.getLevel());

            // Build WhatsApp reply
            String response;
            if ("SAFE".equals(result.getLevel())) {
                response = "✅ Link looks SAFE\n\n"
                        + "🔗 URL: " + url + "\n"
                        + "📊 Score: " + result.getScore() + "/100\n"
                        + "🤖 Engine: " + result.getAnalyzedBy() + "\n\n"
                        + "✅ Safe to open!";
            } else if ("DANGEROUS".equals(result.getLevel())) {
                response = "🚨 DANGEROUS LINK DETECTED!\n\n"
                        + "🔗 URL: " + url + "\n"
                        + "📊 Risk Score: " + result.getScore() + "/100\n\n"
                        + "🔍 Reasons:\n";
                if (result.getReasons() != null) {
                    for (String r : result.getReasons()) response += "  • " + r + "\n";
                }
                response += "\n🛑 DO NOT CLICK THIS LINK!\n"
                        + "🤖 Engine: " + result.getAnalyzedBy();
            } else {
                response = "⚠️ SUSPICIOUS LINK\n\n"
                        + "🔗 URL: " + url + "\n"
                        + "📊 Risk Score: " + result.getScore() + "/100\n\n"
                        + "🔍 Reasons:\n";
                if (result.getReasons() != null) {
                    for (String r : result.getReasons()) response += "  • " + r + "\n";
                }
                response += "\n⚠️ Proceed with caution!\n"
                        + "🤖 Engine: " + result.getAnalyzedBy();
            }

            return "<Response><Message>" + response + "</Message></Response>";

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error: " + e.getMessage());
            return "<Response><Message>⚠️ Error analyzing. Please send just the URL.</Message></Response>";
        }
    }
}