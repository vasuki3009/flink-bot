package com.fakelinkbot.controller;

import com.fakelinkbot.model.AnalysisResult;
import com.fakelinkbot.service.NlpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class AnalyzeController {

    @Autowired
    private NlpService nlpService;

    @PostMapping("/analyze")
    public AnalysisResult analyzeUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        System.out.println("🌐 Web request to analyze: " + url);
        return nlpService.analyze(url);
    }
}