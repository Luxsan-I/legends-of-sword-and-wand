package com.legendsofswordandwand.pvp;

/**
 * Observer interface for receiving battle events.
 * Implement this to be notified whenever something happens during a PvP battle
 * (e.g. a hero attacks, a hero dies, the battle ends).
 *
 * Design Pattern: Observer
 */
public interface BattleEventListener {

    /**
     * Called whenever a battle event occurs.
     *
     * @param event the event that just happened
     */
    void onBattleEvent(BattleEvent event);
}
