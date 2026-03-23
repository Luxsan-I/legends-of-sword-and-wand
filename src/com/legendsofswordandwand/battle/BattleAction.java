package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;

public interface BattleAction {
    boolean execute(BattleSystem battleSystem, Hero actor, Hero target);
    String getActionName();
}