package com.example.quizadmin;

public class ResultModel {
    private String email,marks;

    public ResultModel(String email, String marks) {
        this.email = email;
        this.marks = marks;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }
}
