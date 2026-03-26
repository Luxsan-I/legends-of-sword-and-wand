package com.legendsofswordandwand.ui.pvp;

import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.RankingRepository;
import com.legendsofswordandwand.pvp.PvPController;

import javax.swing.*;

/**
 * Coordinates the full PvP flow:
 *   1. Player 1 builds their party (PartyBuilderView)
 *   2. Player 2 builds their party (PartyBuilderView)
 *   3. Battle screen opens (PvPBattleView)
 *   4. Result screen opens (PvPResultView)
 *   5. Returns to main menu via onFinished callback
 */
public class PvPFlowCoordinator {

    private final Profile playerOneProfile;
    private final Profile playerTwoProfile;
    private final RankingRepository rankingRepository;
    private final Runnable onFinished;

    public PvPFlowCoordinator(Profile playerOneProfile,
                               Profile playerTwoProfile,
                               RankingRepository rankingRepository,
                               Runnable onFinished) {
        this.playerOneProfile = playerOneProfile;
        this.playerTwoProfile = playerTwoProfile;
        this.rankingRepository = rankingRepository;
        this.onFinished = onFinished;
    }

    /** Starts the PvP flow — call this from the main menu. */
    public void start() {
        SwingUtilities.invokeLater(this::showPlayerOneBuilder);
    }

    private void showPlayerOneBuilder() {
        PartyBuilderView p1Builder = new PartyBuilderView(playerOneProfile.getUsername());
        p1Builder.addConfirmListener(e -> {
            Party partyOne = p1Builder.getParty();
            p1Builder.dispose();
            showPlayerTwoBuilder(partyOne);
        });
        p1Builder.setVisible(true);
    }

    private void showPlayerTwoBuilder(Party partyOne) {
        PartyBuilderView p2Builder = new PartyBuilderView(playerTwoProfile.getUsername());
        p2Builder.addConfirmListener(e -> {
            Party partyTwo = p2Builder.getParty();
            p2Builder.dispose();
            startBattle(partyOne, partyTwo);
        });
        p2Builder.setVisible(true);
    }

    private void startBattle(Party partyOne, Party partyTwo) {
        PvPController controller = new PvPController(
                playerOneProfile, partyOne,
                playerTwoProfile, partyTwo,
                rankingRepository
        );

        PvPBattleView battleView = new PvPBattleView(controller);
        battleView.setOnBattleOver(() -> {
            // Small delay so the last log line is visible before the result pops up
            Timer timer = new Timer(800, ev -> {
                battleView.dispose();
                showResult(controller);
            });
            timer.setRepeats(false);
            timer.start();
        });
        battleView.setVisible(true);
    }

    private void showResult(PvPController controller) {
        PvPResultView resultView = new PvPResultView(
                controller.getBattleResult(),
                playerOneProfile,
                playerTwoProfile
        );
        resultView.addReturnToMenuListener(e -> {
            resultView.dispose();
            if (onFinished != null) {
                onFinished.run();
            }
        });
        resultView.setVisible(true);
    }
}
