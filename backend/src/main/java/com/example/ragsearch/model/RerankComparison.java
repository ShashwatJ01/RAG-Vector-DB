package com.example.ragsearch.model;

import java.util.List;

public class RerankComparison {
    private String baselineAnswer;
    private List<SourceCitation> baselineSources;
    private int sourceOverlap;

    public RerankComparison() {
    }

    public RerankComparison(String baselineAnswer, List<SourceCitation> baselineSources, int sourceOverlap) {
        this.baselineAnswer = baselineAnswer;
        this.baselineSources = baselineSources;
        this.sourceOverlap = sourceOverlap;
    }

    public String getBaselineAnswer() {
        return baselineAnswer;
    }

    public void setBaselineAnswer(String baselineAnswer) {
        this.baselineAnswer = baselineAnswer;
    }

    public List<SourceCitation> getBaselineSources() {
        return baselineSources;
    }

    public void setBaselineSources(List<SourceCitation> baselineSources) {
        this.baselineSources = baselineSources;
    }

    public int getSourceOverlap() {
        return sourceOverlap;
    }

    public void setSourceOverlap(int sourceOverlap) {
        this.sourceOverlap = sourceOverlap;
    }
}
