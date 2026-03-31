package com.legendsofswordandwand.ui.shared;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.CampaignProgress.CampaignStatus;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.CampaignRepository;
import com.legendsofswordandwand.persistence.ProfileRepository;
import com.legendsofswordandwand.persistence.RankingRepository;
import com.legendsofswordandwand.pve.CampaignService;
import com.legendsofswordandwand.pve.InnService;
import com.legendsofswordandwand.ui.pve.CampaignResultView;
import com.legendsofswordandwand.ui.pve.CampaignView;
import com.legendsofswordandwand.ui.pve.InnView;
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
                loginView.showMessage(created
                        ? "Profile created successfully."
                        : "Could not create profile.");
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
        CampaignRepository campaignRepository = new CampaignRepository();
        InnService innService = new InnService(500); // starting gold for new campaigns
        CampaignService campaignService = new CampaignService(campaignRepository, innService);

        MainMenuView mainMenu = new MainMenuView();

        // Grey out "Continue" if no incomplete campaign exists
        mainMenu.setContinuePveEnabled(campaignService.hasIncompleteCampaign(profile));

        // -----------------------------------------------------------------
        // New PvE Campaign
        // -----------------------------------------------------------------
        mainMenu.addNewPveListener(e -> {
            // Ask the player to pick a hero class
            HeroClass[] classes = HeroClass.values();
            HeroClass chosen = (HeroClass) JOptionPane.showInputDialog(
                    mainMenu,
                    "Choose your starting hero class:",
                    "New Campaign",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    classes,
                    classes[0]);

            if (chosen == null) return; // player cancelled

            CampaignProgress progress = campaignService.startNew(profile, chosen);
            if (progress == null) {
                JOptionPane.showMessageDialog(mainMenu, "Could not start campaign.");
                return;
            }

            mainMenu.setVisible(false);
            openCampaignView(progress, campaignService, innService,
                    campaignRepository, profile, mainMenu);
        });

        // -----------------------------------------------------------------
        // Continue PvE Campaign
        // -----------------------------------------------------------------
        mainMenu.addContinuePveListener(e -> {
            CampaignProgress progress = campaignService.loadCampaign(profile);
            if (progress == null) {
                JOptionPane.showMessageDialog(mainMenu, "No saved campaign found.");
                mainMenu.setContinuePveEnabled(false);
                return;
            }

            // Campaign completed — go straight to result screen
            if (progress.isCampaignCompleted()) {
                mainMenu.setVisible(false);
                CampaignResultView resultView = new CampaignResultView(
                        campaignService, progress, profile, campaignRepository,
                        () -> {
                            mainMenu.setContinuePveEnabled(
                                    campaignService.hasIncompleteCampaign(profile));
                            mainMenu.setVisible(true);
                        });
                resultView.setVisible(true);
                return;
            }

            mainMenu.setVisible(false);

            // Route based on where the player left off
            if (progress.getStatus() == CampaignStatus.IN_INN) {
                InnView innView = new InnView(
                        innService, progress, profile, campaignRepository);
                innView.setOnNextRoom(() -> {
                    innView.dispose();
                    openCampaignView(progress, campaignService, innService,
                            campaignRepository, profile, mainMenu);
                });
                innView.setVisible(true);
            } else {
                openCampaignView(progress, campaignService, innService,
                        campaignRepository, profile, mainMenu);
            }
        });

        // -----------------------------------------------------------------
        // PvP
        // -----------------------------------------------------------------
        mainMenu.addPvpListener(event -> {
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
                    () -> mainMenu.setVisible(true));
            coordinator.start();
        });

        // -----------------------------------------------------------------
        // Profile / Rankings / Exit
        // -----------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    // Helper — opens CampaignView and wires its return callback
    // -------------------------------------------------------------------------

    private static void openCampaignView(CampaignProgress progress,
                                         CampaignService campaignService,
                                         InnService innService,
                                         CampaignRepository campaignRepository,
                                         Profile profile,
                                         MainMenuView mainMenu) {
        CampaignView campaignView = new CampaignView(
                campaignService, innService, progress, profile, campaignRepository);

        campaignView.setOnReturnToMenu(() -> {
            campaignView.dispose();
            mainMenu.setContinuePveEnabled(
                    campaignService.hasIncompleteCampaign(profile));
            mainMenu.setVisible(true);
        });

        campaignView.setVisible(true);
    }
}