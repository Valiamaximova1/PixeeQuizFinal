package com.example.chipiquizfinal.entity;


import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "email")
    private String email;
    @ColumnInfo(name = "password")
    private String password;
    @ColumnInfo(name = "username")
    private String username;
    @ColumnInfo(name = "firstName")
    private String firstName;
    @ColumnInfo(name = "lastName")
    private String lastName;
    @ColumnInfo(name = "language")
    private String language;
    @ColumnInfo(name = "level")
    private int level;
    @ColumnInfo(name = "points")
    private int points;
    @ColumnInfo(name = "lives")
    private int lives;
    @ColumnInfo(name = "consecutive_days")
    private int consecutiveDays;
    @ColumnInfo(name = "freeze_day")
    private boolean freezeDay;
    @ColumnInfo(name = "daily_practice")
    private int dailyPractice;
    @ColumnInfo(name = "role")
    private String role = "user";
    @ColumnInfo(name = "selected_language_code")
    private String selectedLanguageCode = "bg";
    @Nullable
    @ColumnInfo(name = "profile_image_uri")
    private String profileImagePath;

    @ColumnInfo(name = "last_life_timestamp")
    private long lastLifeTimestamp;

    public User() {}

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getDailyPractice() {
        return dailyPractice;
    }
    public void setDailyPractice(int dailyPractice) {
        this.dailyPractice = dailyPractice;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getConsecutiveDays() {
        return consecutiveDays;
    }

    public void setConsecutiveDays(int consecutiveDays) {
        this.consecutiveDays = consecutiveDays;
    }

    public boolean isFreezeDay() {
        return freezeDay;
    }

    public void setFreezeDay(boolean freezeDay) {
        this.freezeDay = freezeDay;
    }

    public String getSelectedLanguageCode() {
        return selectedLanguageCode;
    }

    public void setSelectedLanguageCode(String selectedLanguageCode) {
        this.selectedLanguageCode = selectedLanguageCode;
    }
    @Nullable
    public String getProfileImagePath() {
        return profileImagePath;
    }
    @Nullable
    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    public long getLastLifeTimestamp() {
        return lastLifeTimestamp;
    }

    public void setLastLifeTimestamp(long lastLifeTimestamp) {
        this.lastLifeTimestamp = lastLifeTimestamp;
    }
}
