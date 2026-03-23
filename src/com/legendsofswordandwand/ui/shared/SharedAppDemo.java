package com.legendsofswordandwand.ui.shared;

import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.ProfileRepository;
import com.legendsofswordandwand.persistence.RankingRepository;

import javax.swing.*;

public class SharedAppDemo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProfileRepository profileRepository = new ProfileRepository();
            RankingRepository rankingRepository = new RankingRepository();

            LoginView loginView = new LoginView();

            loginView.addRegisterListener(e -> {
                String username = loginView.getUsername();
                String password = loginView.getPassword();

                boolean created = profileRepository.createProfile(username, password);
                if (created) {
                    loginView.showMessage("Profile created successfully.");
                } else {
                    loginView.showMessage("Could not create profile.");
                }
            });

            loginView.addLoginListener(e -> {
                String username = loginView.getUsername();
                String password = loginView.getPassword();

                boolean authenticated = profileRepository.authenticate(username, password);
                if (!authenticated) {
                    loginView.showMessage("Invalid username or password.");
                    return;
                }

                Profile profile = profileRepository.findByUsername(username);
                loginView.dispose();

                MainMenuView mainMenuView = new MainMenuView();
                mainMenuView.addProfileListener(event -> {
                    ProfileView profileView = new ProfileView(profile);
                    profileView.setVisible(true);
                });

                mainMenuView.addRankingsListener(event -> {
                    RankingView rankingView = new RankingView(
                            rankingRepository.getHallOfFame(),
                            "Hall of Fame"
                    );
                    rankingView.setVisible(true);
                });

                mainMenuView.addExitListener(event -> System.exit(0));
                mainMenuView.setVisible(true);
            });

            loginView.setVisible(true);
        });
    }
}