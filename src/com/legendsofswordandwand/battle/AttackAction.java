package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;

public class AttackAction implements BattleAction {

    @Override
    public boolean execute(BattleSystem battleSystem, Hero actor, Hero target) {
        return battleSystem.performAttack(actor, target);
    }

    @Override
    public String getActionName() {
        return "Attack";
    }
}