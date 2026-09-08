package com.fakelinkbot.analyzer;

public class RiskScorer {

    public String getLevel(int score) {
        if (score >= 70) return "DANGEROUS";
        if (score >= 35) return "SUSPICIOUS";
        return "SAFE";
    }
}