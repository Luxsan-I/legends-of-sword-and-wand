package com.legendsofswordandwand.pvp;

import com.legendsofswordandwand.battle.BattleSystem;
import com.legendsofswordandwand.model.BattleResult;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.persistence.RankingRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates a PvP match between two players.
 *
 * Acts as the Subject in the Observer pattern — it maintains a list of
 * BattleEventListeners and notifies them whenever something happens.
 *
 * Acts as the Context in the State pattern — it holds the current BattleState
 * and delegates all action requests to it.
 */
public class PvPController {

    private final Profile playerOneProfile;
    private final Profile playerTwoProfile;
    private final BattleSystem battleSystem;
    private final RankingRepository rankingRepository;

    // Observer pattern: list of registered listeners
    private final List<BattleEventListener> listeners = new ArrayList<>();

    // State pattern: current state of the battle
    private BattleState currentState;

    public PvPController(Profile playerOneProfile, Party partyOne,
                         Profile playerTwoProfile, Party partyTwo,
                         RankingRepository rankingRepository) {
        this.playerOneProfile = playerOneProfile;
        this.playerTwoProfile = playerTwoProfile;
        this.rankingRepository = rankingRepository;
        this.battleSystem = new BattleSystem(partyOne, partyTwo);

        // Start in player one's turn
        this.currentState = new PlayerTurnState(playerOneProfile.getUsername());
    }

    // -------------------------------------------------------------------------
    // Observer pattern: subscribe / unsubscribe / notify
    // -------------------------------------------------------------------------

    /** Registers a listener to receive battle events. */
    public void addBattleEventListener(BattleEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /** Removes a previously registered listener. */
    public void removeBattleEventListener(BattleEventListener listener) {
        listeners.remove(listener);
    }

    /** Fires an event to all registered listeners. */
    public void fireEvent(BattleEvent event) {
        for (BattleEventListener listener : listeners) {
            listener.onBattleEvent(event);
        }
    }

    // -------------------------------------------------------------------------
    // State pattern: action delegation
    // -------------------------------------------------------------------------

    public void attack(Hero actor, Hero target) {
        currentState.handleAttack(this, actor, target);
    }

    public void defend(Hero actor) {
        currentState.handleDefend(this, actor);
    }

    public void castAbility(Hero actor, Hero target) {
        currentState.handleCastAbility(this, actor, target);
    }

    public void waitTurn(Hero actor) {
        currentState.handleWait(this, actor);
    }

    /**
     * Called after each successful action to check whether the battle has
     * ended and update the state accordingly.
     */
    public void advanceTurn() {
        if (battleSystem.isBattleOver()) {
            currentState = new BattleOverState();
            finalizeBattle();
            fireEvent(new BattleEvent(BattleEvent.Type.BATTLE_OVER, null, null,
                    "The battle is over!"));
            return;
        }

        // Determine which player the next current hero belongs to
        Hero nextHero = battleSystem.getCurrentHero();
        String nextPlayerLabel = resolvePlayerLabel(nextHero);
        currentState = new PlayerTurnState(nextPlayerLabel);

        fireEvent(new BattleEvent(BattleEvent.Type.TURN_CHANGED, nextHero, null,
                "It is now " + nextHero.getName() + "'s turn (" + nextPlayerLabel + ")"));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Determines which player's party a hero belongs to.
     * Returns the matching player's username, or "Unknown" if not found.
     */
    private String resolvePlayerLabel(Hero hero) {
        if (hero == null) {
            return "Unknown";
        }
        for (Hero h : battleSystem.getPartyOne().getHeroes()) {
            if (h == hero) {
                return playerOneProfile.getUsername();
            }
        }
        return playerTwoProfile.getUsername();
    }

    /**
     * Records the PvP result in the ranking system once the battle ends.
     */
    private void finalizeBattle() {
        BattleResult result = battleSystem.getBattleResult();
        if (result == null) {
            return;
        }

        boolean partyOneWon = result.getWinningParty() == battleSystem.getPartyOne();
        Profile winner = partyOneWon ? playerOneProfile : playerTwoProfile;
        Profile loser  = partyOneWon ? playerTwoProfile : playerOneProfile;

        rankingRepository.recordPvPResult(winner, loser);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public BattleSystem getBattleSystem() {
        return battleSystem;
    }

    public BattleState getCurrentState() {
        return currentState;
    }

    public Profile getPlayerOneProfile() {
        return playerOneProfile;
    }

    public Profile getPlayerTwoProfile() {
        return playerTwoProfile;
    }

    public BattleResult getBattleResult() {
        return battleSystem.getBattleResult();
    }

    public boolean isBattleOver() {
        return battleSystem.isBattleOver();
    }
}
