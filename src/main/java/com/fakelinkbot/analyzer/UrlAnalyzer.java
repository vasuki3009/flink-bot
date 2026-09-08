package com.fakelinkbot.analyzer;

import java.util.*;

public class UrlAnalyzer {

    private static final List<String> SUSPICIOUS_TLDS = Arrays.asList(
            ".tk", ".ml", ".ga", ".cf", ".gq", ".xyz", ".top",
            ".click", ".link", ".win", ".loan", ".online", ".site"
    );

    private static final List<String> URL_SHORTENERS = Arrays.asList(
            "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly",
            "short.link", "tiny.cc", "is.gd", "buff.ly", "cutt.ly",
            "rb.gy", "shorturl.at", "clck.ru", "rebrand.ly"
    );

    private static final List<String> PHISHING_KEYWORDS = Arrays.asList(
            "verify", "login", "signin", "account", "secure", "update",
            "confirm", "suspended", "unlock", "claim", "prize", "free",
            "winner", "gift", "password", "bank", "payment", "urgent",
            "alert", "limited", "offer", "reward", "lucky", "won"
    );

    private static final List<String> TRUSTED_DOMAINS = Arrays.asList(
            "google.com", "youtube.com", "facebook.com", "instagram.com",
            "twitter.com", "linkedin.com", "github.com", "microsoft.com",
            "apple.com", "amazon.com", "wikipedia.org", "stackoverflow.com",
            "whatsapp.com", "telegram.org", "netflix.com", "spotify.com",
            "nios.ac.in", "sbi.co.in", "hdfcbank.com", "icicibank.com",
            "irctc.co.in", "paytm.com", "flipkart.com", "myntra.com"
    );

    private static final List<String> KNOWN_FAKE_DOMAINS = Arrays.asList(
            "nios-ac.in", "old-nios-ac.in", "nios.gen.in",
            "nios.ac.in-examresult.in", "bholananda.org",
            "sbi-net.in", "sbibank.in", "sbi-online.in",
            "hdfcbank-net.in", "hdfc-netbanking.in",
            "icici-bank.in", "icicinet.in",
            "paytm-mall.in", "paytm-offers.in",
            "amazon-offers.in", "amzon.in",
            "flipkart-sale.in", "flipkart-offers.in",
            "irctc-booking.in", "irctc-railways.in",
            "gov-yojana.in", "pmkisan-gov.in"
    );

    private static final List<String> BRAND_HYPHEN_PATTERNS = Arrays.asList(
            "nios-", "sbi-", "hdfc-", "icici-", "rbi-",
            "irctc-", "paytm-", "amazon-", "flipkart-",
            "google-", "facebook-", "whatsapp-", "instagram-",
            "paypal-", "netflix-", "apple-", "microsoft-",
            // Lookalike / number-substitution brand patterns
            "amaz0n-", "paypa1-", "g00gle-", "fac3book-",
            "amzon-", "amazom-", "arnazon-", "micros0ft-",
            "netfl1x-", "appl3-", "lnstagram-", "linkedln-"
    );

    // Common number-substitution lookalikes for brands
    private static final List<String> NUMBER_SUBSTITUTION_PATTERNS = Arrays.asList(
            "amaz0n", "paypa1", "g00gle", "fac3book",
            "micros0ft", "netfl1x", "appl3", "inst4gram",
            "tw1tter", "lnked1n", "wh4tsapp", "fl1pkart",
            "payt0m", "y0utube", "sp0tify"
    );

    public int analyze(String url, StringBuilder reasons) {
        int score = 0;
        String lowerUrl = url.toLowerCase();

        // 1. Trusted domains — immediately SAFE
        for (String trusted : TRUSTED_DOMAINS) {
            if (lowerUrl.contains(trusted)) return 0;
        }

        // 2. Known fake domains — immediately DANGEROUS
        for (String fake : KNOWN_FAKE_DOMAINS) {
            if (lowerUrl.contains(fake)) {
                reasons.append("Known fake/phishing domain detected\n");
                reasons.append("Officially reported by organization\n");
                return 90;
            }
        }

        // 3. Number substitution (check BEFORE brand-hyphen so score stacks correctly)
        boolean numberSubFound = false;
        for (String pattern : NUMBER_SUBSTITUTION_PATTERNS) {
            if (lowerUrl.contains(pattern)) {
                if (!numberSubFound) {
                    score += 60;
                    reasons.append("Number substitution detected (e.g. 0 instead of o)\n");
                    reasons.append("Classic phishing impersonation technique\n");
                    numberSubFound = true;
                }
            }
        }

        // 4. Brand hyphen typosquatting
        for (String pattern : BRAND_HYPHEN_PATTERNS) {
            if (lowerUrl.contains(pattern)) {
                score += 50;
                reasons.append("Brand impersonation detected\n");
                reasons.append("Possible typosquatting attack\n");
                break;
            }
        }

        // 5. URL shorteners
        for (String shortener : URL_SHORTENERS) {
            if (lowerUrl.contains(shortener)) {
                score += 40;
                reasons.append("URL shortener detected: ").append(shortener).append("\n");
                reasons.append("Cannot verify final destination\n");
                break;
            }
        }

        // 6. Suspicious TLD
        for (String tld : SUSPICIOUS_TLDS) {
            if (lowerUrl.endsWith(tld) || lowerUrl.contains(tld + "/")) {
                score += 35;
                reasons.append("Suspicious TLD: ").append(tld).append("\n");
                break;
            }
        }

        // 7. Phishing keywords
        int kwCount = 0;
        for (String kw : PHISHING_KEYWORDS) {
            if (lowerUrl.contains(kw) && kwCount < 2) {
                score += 15;
                kwCount++;
                reasons.append("Phishing keyword: ").append(kw).append("\n");
            }
        }

        // 8. IP address used instead of domain
        if (lowerUrl.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*")) {
            score += 30;
            reasons.append("IP address used instead of domain\n");
        }

        return Math.min(score, 100);
    }
}