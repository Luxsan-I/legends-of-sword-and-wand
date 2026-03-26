package com.legendsofswordandwand.pvp;

import com.legendsofswordandwand.model.Hero;

/**
 * Represents a battle event fired during a PvP match.
 * Used by the Observer pattern to notify the UI of state changes.
 */
public class BattleEvent {

    public enum Type {
        HERO_ATTACKED,
        HERO_DEFENDED,
        HERO_CAST_ABILITY,
        HERO_WAITED,
        HERO_DIED,
        TURN_CHANGED,
        BATTLE_OVER
    }

    private final Type type;
    private final Hero actor;
    private final Hero target;
    private final String message;

    public BattleEvent(Type type, Hero actor, Hero target, String message) {
        this.type = type;
        this.actor = actor;
        this.target = target;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public Hero getActor() {
        return actor;
    }

    public Hero getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
    }
}
