package com.legendsofswordandwand.pvp;

import com.legendsofswordandwand.model.Hero;

/**
 * Represents the current state of a PvP battle.
 * Each state determines what actions are valid and who is acting.
 *
 * Design Pattern: State
 */
public interface BattleState {

    /**
     * Called when an attack action is requested.
     *
     * @param context the PvP controller managing this state
     * @param actor   the hero performing the attack
     * @param target  the hero being attacked
     */
    void handleAttack(PvPController context, Hero actor, Hero target);

    /**
     * Called when a defend action is requested.
     *
     * @param context the PvP controller managing this state
     * @param actor   the hero defending
     */
    void handleDefend(PvPController context, Hero actor);

    /**
     * Called when a cast ability action is requested.
     *
     * @param context the PvP controller managing this state
     * @param actor   the hero casting
     * @param target  the hero being targeted
     */
    void handleCastAbility(PvPController context, Hero actor, Hero target);

    /**
     * Called when a wait action is requested.
     *
     * @param context the PvP controller managing this state
     * @param actor   the hero waiting
     */
    void handleWait(PvPController context, Hero actor);

    /**
     * Returns a human-readable label for this state (for UI display).
     */
    String getStateLabel();
}
