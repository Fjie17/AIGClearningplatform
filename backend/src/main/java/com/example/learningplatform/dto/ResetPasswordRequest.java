package com.example.learningplatform.dto;

public class ResetPasswordRequest {

    private String email;
    private String emailCode;
    private String password;
    private String confirmPassword;

    // ======= getter & setter =======
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailCode() {
        return emailCode;
    }
    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
