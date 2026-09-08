package com.fakelinkbot.controller;

import com.fakelinkbot.model.AnalysisResult;
import com.fakelinkbot.service.NlpService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
public class TelegramController {

    @Autowired
    private NlpService nlpService;

    private final String BOT_TOKEN ="8489022830:AAH2CogkOS2E77cSjVhsb5QIKcnW-leXFrQ";

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> update) {

        try {
            System.out.println("📩 Telegram Update: " + update);

            // 🔹 Extract message text
            Map message = (Map) update.get("message");
            if (message == null) return ResponseEntity.ok("No message");

            String text = (String) message.get("text");
            Map chat = (Map) message.get("chat");
            Integer chatId = (Integer) chat.get("id");

            // 🔹 Extract URL
            String url = extractUrl(text);

            // 🔹 Call Python ML API
            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "http://127.0.0.1:5000/predict";

            Map<String, String> request = Map.of("url", url);

            ResponseEntity<Map> mlResponse =
                    restTemplate.postForEntity(apiUrl, request, Map.class);

            Map result = mlResponse.getBody();
            if (result == null) throw new RuntimeException("ML API error");

            String prediction = (String) result.get("prediction");
            Double confidence = Double.valueOf(result.get("confidence").toString());

            // 🔹 NLP Analysis
            AnalysisResult nlpResult = nlpService.analyze(text);
            List<String> reasons = nlpResult.getReasons();

            // 🔹 Build reply
            String emoji = prediction.equalsIgnoreCase("PHISHING") ? "🚨" : "✅";

            StringBuilder reply = new StringBuilder();
            reply.append("🔍 Link Analysis\n\n");
            reply.append("URL: ").append(url).append("\n");
            reply.append(emoji).append(" Prediction: ").append(prediction).append("\n");
            reply.append("📊 Confidence: ").append((int)(confidence * 100)).append("%\n\n");

            if (!reasons.isEmpty()) {
                reply.append("🧠 Reasons:\n");
                for (String r : reasons) {
                    reply.append("- ").append(r).append("\n");
                }
            }

            // 🔹 Send message back to Telegram
            sendMessage(chatId, reply.toString());

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Error");
        }
    }

    // 🔹 Send message to Telegram
    private void sendMessage(Integer chatId, String text) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text", text
        );

        restTemplate.postForEntity(url, body, String.class);
    }

    // 🔍 Extract URL
    private String extractUrl(String message) {
        String[] words = message.split("\\s+");
        for (String word : words) {
            if (word.startsWith("http://") || word.startsWith("https://")) {
                return word;
            }
        }
        return message;
    }
}