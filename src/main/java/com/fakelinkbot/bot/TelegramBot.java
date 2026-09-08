package com.fakelinkbot.bot;

import com.fakelinkbot.model.AnalysisResult;
import com.fakelinkbot.service.NlpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private NlpService nlpService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public String getBotToken() { return botToken; }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text   = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();
        String from   = update.getMessage().getFrom().getFirstName();

        System.out.println("📨 Telegram from: " + from);
        System.out.println("📝 Message: " + text);

        // Extract URL from message
        String url = extractUrl(text);
        if (url == null) {
            // No link found — ignore or reply
            sendReply(chatId, "ℹ️ No link detected in your message.\n\nSend me any link and I'll check if it's safe!");
            return;
        }

        try {
            AnalysisResult result = nlpService.analyze(text);

            // Push to WebSocket → fires popup on monitor.html
            Map<String, Object> alert = Map.of(
                    "level",      result.getLevel(),
                    "score",      result.getScore(),
                    "url",        url,
                    "from",       "Telegram: " + from,
                    "analyzedBy", result.getAnalyzedBy(),
                    "reasons",    result.getReasons()
            );
            messagingTemplate.convertAndSend("/topic/alerts", alert);
            System.out.println("🚨 Telegram alert pushed: " + result.getLevel());

            // Build reply message
            String reply;
            if ("SAFE".equals(result.getLevel())) {
                reply = "✅ Link looks SAFE\n\n" +
                        "🔗 URL: " + url + "\n" +
                        "📊 Score: " + result.getScore() + "/100\n" +
                        "🔍 Engine: " + result.getAnalyzedBy() + "\n\n" +
                        "✅ Safe to open!";
            } else if ("DANGEROUS".equals(result.getLevel())) {
                reply = "🚨 DANGEROUS LINK DETECTED!\n\n" +
                        "🔗 URL: " + url + "\n" +
                        "📊 Risk Score: " + result.getScore() + "/100\n\n" +
                        "🔍 Reasons:\n" + formatReasons(result.getReasons()) +
                        "\n🛑 DO NOT CLICK THIS LINK!\n\n" +
                        "🤖 Engine: " + result.getAnalyzedBy();
            } else {
                reply = "⚠️ SUSPICIOUS LINK\n\n" +
                        "🔗 URL: " + url + "\n" +
                        "📊 Risk Score: " + result.getScore() + "/100\n\n" +
                        "🔍 Reasons:\n" + formatReasons(result.getReasons()) +
                        "\n⚠️ Proceed with caution!\n\n" +
                        "🤖 Engine: " + result.getAnalyzedBy();
            }

            sendReply(chatId, reply);

        } catch (Exception e) {
            e.printStackTrace();
            sendReply(chatId, "⚠️ Error analyzing link. Please try again.");
        }
    }

    private void sendReply(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String extractUrl(String text) {
        Pattern pattern = Pattern.compile(
                "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+"
        );
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private String formatReasons(List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) return "  None\n";
        StringBuilder sb = new StringBuilder();
        for (String r : reasons) sb.append("  • ").append(r).append("\n");
        return sb.toString();
    }
}