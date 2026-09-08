package com.fakelinkbot.service;

import com.fakelinkbot.analyzer.RiskScorer;
import com.fakelinkbot.analyzer.UrlAnalyzer;
import com.fakelinkbot.model.AnalysisResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NlpService {

    private final UrlAnalyzer urlAnalyzer = new UrlAnalyzer();
    private final RiskScorer  riskScorer  = new RiskScorer();

    // ── Main analyze method — uses BOTH engines ──
    public AnalysisResult analyze(String message) {
        try {
            String url = extractUrl(message);

            if (url == null) {
                return buildResult("SAFE", 0, "Java Analyzer", new ArrayList<>());
            }

            // Rule engine result
            StringBuilder reasons = new StringBuilder();
            int ruleScore = urlAnalyzer.analyze(url, reasons);

            // ML engine result
            AnalysisResult mlResult = callMlService(url);

            // Take the HIGHER risk score (safer for user)
            int finalScore    = ruleScore;
            String analyzedBy = "Java Rule Engine";

            if (mlResult != null) {
                if (mlResult.getScore() > ruleScore) {
                    finalScore = mlResult.getScore();
                }
                // Show combined engine whenever ML runs successfully
                analyzedBy = "Java + Python ML";
            }
            List<String> reasonList = new ArrayList<>();
            for (String line : reasons.toString().split("\n")) {
                if (!line.trim().isEmpty()) reasonList.add(line.trim());
            }

            return buildResult(
                    riskScorer.getLevel(finalScore),
                    finalScore,
                    analyzedBy,
                    reasonList
            );

        } catch (Exception e) {
            e.printStackTrace();
            return buildResult("SAFE", 0, "Java Analyzer", new ArrayList<>());
        }
    }

    private AnalysisResult callMlService(String url) {
        try {
            System.out.println("🤖 Calling ML for: " + url);

            // Use GET request — much simpler, no JSON body issues
            String encodedUrl = java.net.URLEncoder.encode(url,
                    java.nio.charset.StandardCharsets.UTF_8);
            String mlUrl = "http://127.0.0.1:8001/predict?url=" + encodedUrl;

            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(mlUrl))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            java.net.http.HttpResponse<String> response =
                    client.send(request,
                            java.net.http.HttpResponse.BodyHandlers.ofString());

            String body = response.body();
            System.out.println("🤖 ML Response: " + body);

            // Parse using Jackson
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(body);

            int score    = node.get("score").asInt();
            String level = node.get("level").asText();

            System.out.println("✅ ML Score: " + score + " Level: " + level);

            AnalysisResult r = new AnalysisResult();
            r.setLevel(level);
            r.setScore(score);
            r.setAnalyzedBy("Python ML Model");
            r.setReasons(List.of("ML prediction: 12 URL features"));
            return r;

        } catch (Exception e) {
            System.out.println("❌ ML Error: " + e.getMessage());
            return null;
        }
    }
    // ── Extract URL from message text ──
    public String extractUrl(String text) {
        if (text == null) return null;

        // First try full URL with http/https
        Pattern p = Pattern.compile("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");
        Matcher m = p.matcher(text);
        if (m.find()) return m.group();

        // Try plain URL — match LONGEST domain possible
        Pattern plain = Pattern.compile(
                "(?:^|\\s)((?:[\\w\\-]+\\.)+[a-z]{2,}(?:/[\\S]*)?)"
        );
        Matcher pm = plain.matcher(text);
        String longest = null;
        while (pm.find()) {
            String found = pm.group(1).trim();
            if (longest == null || found.length() > longest.length()) {
                longest = found;
            }
        }
        if (longest != null) return "http://" + longest;

        return null;
    }
    // ── Helper ──
    private AnalysisResult buildResult(String level, int score,
                                       String analyzedBy, List<String> reasons) {
        AnalysisResult r = new AnalysisResult();
        r.setLevel(level);
        r.setScore(score);
        r.setAnalyzedBy(analyzedBy);
        r.setReasons(reasons);
        return r;
    }
}