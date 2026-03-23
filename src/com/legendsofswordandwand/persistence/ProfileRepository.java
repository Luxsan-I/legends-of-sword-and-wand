package com.legendsofswordandwand.persistence;

import com.legendsofswordandwand.model.Profile;

import java.util.Map;

public class ProfileRepository {
    private DatabaseManager databaseManager;

    public ProfileRepository() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public boolean createProfile(String username, String password) {
        Map<String, Profile> profiles = databaseManager.getProfiles();

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }

        if (profiles.containsKey(username)) {
            return false;
        }

        profiles.put(username, new Profile(username, password));
        return true;
    }

    public Profile findByUsername(String username) {
        return databaseManager.getProfiles().get(username);
    }

    public boolean authenticate(String username, String password) {
        Profile profile = findByUsername(username);

        if (profile == null) {
            return false;
        }

        return profile.getPassword().equals(password);
    }

    public void saveProfile(Profile profile) {
        if (profile == null) {
            return;
        }

        databaseManager.getProfiles().put(profile.getUsername(), profile);
    }
}