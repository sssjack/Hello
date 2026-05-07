package com.interview.coach.domain;

public enum ExamType {
    NATIONAL("国考"),
    PROVINCIAL("省考"),
    INSTITUTION("事业编"),
    CUSTOM("自定义题");

    private final String label;

    ExamType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
