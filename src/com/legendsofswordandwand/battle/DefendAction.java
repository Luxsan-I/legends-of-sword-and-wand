package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;

public class DefendAction implements BattleAction {

    @Override
    public boolean execute(BattleSystem battleSystem, Hero actor, Hero target) {
        return battleSystem.performDefend(actor);
    }

    @Override
    public String getActionName() {
        return "Defend";
    }
}