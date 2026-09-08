package com.fakelinkbot.model;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResult {

    private int score;
    private String level;
    private String analyzedBy;
    private String emoji;
    private List<String> reasons = new ArrayList<>();

    // Getters & Setters
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getAnalyzedBy() { return analyzedBy; }
    public void setAnalyzedBy(String analyzedBy) { this.analyzedBy = analyzedBy; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
}