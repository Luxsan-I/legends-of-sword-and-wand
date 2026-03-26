package com.legendsofswordandwand.ui.shared;

import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.ProfileRepository;
import com.legendsofswordandwand.persistence.RankingRepository;
import com.legendsofswordandwand.ui.pvp.PvPFlowCoordinator;

import javax.swing.*;

/**
 * Application entry point.
 * Handles login, then routes the user to the main menu.
 */
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
                loginView.showMessage(created ? "Profile created successfully." : "Could not create profile.");
            });

            loginView.addLoginListener(e -> {
                String username = loginView.getUsername();
                String password = loginView.getPassword();

                if (!profileRepository.authenticate(username, password)) {
                    loginView.showMessage("Invalid username or password.");
                    return;
                }

                Profile profile = profileRepository.findByUsername(username);
                loginView.dispose();
                showMainMenu(profile, profileRepository, rankingRepository);
            });

            loginView.setVisible(true);
        });
    }

    private static void showMainMenu(Profile profile,
                                     ProfileRepository profileRepository,
                                     RankingRepository rankingRepository) {
        MainMenuView mainMenu = new MainMenuView();

        mainMenu.addPvpListener(event -> {
            // For PvP we ask for the second player's credentials
            String p2Username = JOptionPane.showInputDialog(mainMenu, "Enter Player 2 username:");
            if (p2Username == null || p2Username.isBlank()) return;

            String p2Password = JOptionPane.showInputDialog(mainMenu, "Enter Player 2 password:");
            if (p2Password == null || p2Password.isBlank()) return;

            if (!profileRepository.authenticate(p2Username, p2Password)) {
                JOptionPane.showMessageDialog(mainMenu, "Player 2 credentials are invalid.");
                return;
            }

            Profile p2Profile = profileRepository.findByUsername(p2Username);
            mainMenu.setVisible(false);

            PvPFlowCoordinator coordinator = new PvPFlowCoordinator(
                    profile, p2Profile, rankingRepository,
                    () -> {
                        // Return to main menu after PvP finishes
                        mainMenu.setVisible(true);
                    }
            );
            coordinator.start();
        });

        mainMenu.addProfileListener(ev -> {
            ProfileView profileView = new ProfileView(profile);
            profileView.setVisible(true);
        });

        mainMenu.addRankingsListener(ev -> {
            RankingView rankingView = new RankingView(
                    rankingRepository.getLeagueStandings(), "PvP League Standings");
            rankingView.setVisible(true);
        });

        mainMenu.addExitListener(ev -> System.exit(0));
        mainMenu.setVisible(true);
    }
}