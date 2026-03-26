package com.legendsofswordandwand.pvp;

import com.legendsofswordandwand.model.Hero;

/**
 * State representing a finished battle.
 * All action requests are ignored once the battle is over.
 *
 * Design Pattern: State (concrete state)
 */
public class BattleOverState implements BattleState {

    @Override
    public void handleAttack(PvPController context, Hero actor, Hero target) {
        // Battle is over — no actions allowed
    }

    @Override
    public void handleDefend(PvPController context, Hero actor) {
        // Battle is over — no actions allowed
    }

    @Override
    public void handleCastAbility(PvPController context, Hero actor, Hero target) {
        // Battle is over — no actions allowed
    }

    @Override
    public void handleWait(PvPController context, Hero actor) {
        // Battle is over — no actions allowed
    }

    @Override
    public String getStateLabel() {
        return "Battle Over";
    }
}
