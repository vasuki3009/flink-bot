package com.fakelinkbot.config;

import com.fakelinkbot.bot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(telegramBot);
            System.out.println("✅ Telegram Bot registered successfully!");
            return api;
        } catch (TelegramApiException e) {
            System.err.println("⚠️ Telegram Bot registration failed: " + e.getMessage());
            System.err.println("⚠️ Check your bot token in application.properties");
            // Return null so app still starts even if Telegram fails
            return null;
        }
    }
}