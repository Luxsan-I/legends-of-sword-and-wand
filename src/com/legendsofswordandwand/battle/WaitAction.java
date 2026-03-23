package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;

public class WaitAction implements BattleAction {

    @Override
    public boolean execute(BattleSystem battleSystem, Hero actor, Hero target) {
        return battleSystem.performWait(actor);
    }

    @Override
    public String getActionName() {
        return "Wait";
    }
}