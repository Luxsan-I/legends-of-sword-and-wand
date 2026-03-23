package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;

public class CastAbilityAction implements BattleAction {

    @Override
    public boolean execute(BattleSystem battleSystem, Hero actor, Hero target) {
        return battleSystem.performCastAbility(actor, target);
    }

    @Override
    public String getActionName() {
        return "Cast Ability";
    }
}