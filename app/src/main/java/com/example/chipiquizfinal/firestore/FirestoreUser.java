package com.example.chipiquizfinal.firestore;

public class FirestoreUser {
    private String email;
    private String username;
    private String profileImageUrl;
    private int points;
    private int lives;
    private int streak;
    private String selectedLanguage;

    // Празен конструктор е нужен на Firestore SDK
    public FirestoreUser() {}

    public FirestoreUser(String email, String username,
                         String profileImageUrl,
                         int points, int lives, int streak,
                         String selectedLanguage) {
        this.email = email;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.points = points;
        this.lives = lives;
        this.streak = streak;
        this.selectedLanguage = selectedLanguage;
    }

    // getters и setters за всички полета
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }

    public int getStreak() { return streak; }
    public void setStreak(int streak) { this.streak = streak; }

    public String getSelectedLanguage() { return selectedLanguage; }
    public void setSelectedLanguage(String selectedLanguage) { this.selectedLanguage = selectedLanguage; }
}
