package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.Hero;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TurnManager {
    private Queue<Hero> turnQueue;

    public TurnManager(List<Hero> heroes) {
        this.turnQueue = new LinkedList<>();
        for (Hero hero : heroes) {
            if (hero.isAlive()) {
                turnQueue.offer(hero);
            }
        }
    }

    public Hero getCurrentHero() {
        while (!turnQueue.isEmpty() && !turnQueue.peek().isAlive()) {
            turnQueue.poll();
        }
        return turnQueue.peek();
    }

    public void endTurn() {
        Hero current = turnQueue.poll();
        if (current != null && current.isAlive()) {
            current.setDefending(false);
            turnQueue.offer(current);
        }
    }

    public void moveCurrentHeroToEnd() {
        endTurn();
    }

    public Queue<Hero> getTurnQueue() {
        return turnQueue;
    }
}