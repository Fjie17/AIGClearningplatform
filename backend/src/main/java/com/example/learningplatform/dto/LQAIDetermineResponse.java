package com.example.learningplatform.dto;

public class LQAIDetermineResponse {

    private String LQAI;
    private String LQAI_code;
    private String profile;

    public LQAIDetermineResponse() {
    }

    public LQAIDetermineResponse(String LQAI, String LQAI_code, String profile) {
        this.LQAI = LQAI;
        this.LQAI_code = LQAI_code;
        this.profile = profile;
    }

    public String getLQAI() {
        return LQAI;
    }

    public void setLQAI(String LQAI) {
        this.LQAI = LQAI;
    }

    public String getLQAI_code() {
        return LQAI_code;
    }

    public void setLQAI_code(String LQAI_code) {
        this.LQAI_code = LQAI_code;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}