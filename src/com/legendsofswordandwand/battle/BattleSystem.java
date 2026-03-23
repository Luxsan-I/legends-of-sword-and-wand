package com.legendsofswordandwand.battle;

import com.legendsofswordandwand.model.BattleResult;
import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.Party;

import java.util.ArrayList;
import java.util.List;

public class BattleSystem {
    private Party partyOne;
    private Party partyTwo;
    private TurnManager turnManager;
    private int turnsPlayed;

    public BattleSystem(Party partyOne, Party partyTwo) {
        this.partyOne = partyOne;
        this.partyTwo = partyTwo;
        this.turnsPlayed = 0;

        List<Hero> allHeroes = new ArrayList<>();
        allHeroes.addAll(partyOne.getAliveHeroes());
        allHeroes.addAll(partyTwo.getAliveHeroes());

        for (Hero hero : allHeroes) {
            hero.resetForBattle();
        }

        this.turnManager = new TurnManager(allHeroes);
    }

    public Hero getCurrentHero() {
        return turnManager.getCurrentHero();
    }

    public boolean isBattleOver() {
        return partyOne.isDefeated() || partyTwo.isDefeated();
    }

    public boolean performAttack(Hero attacker, Hero target) {
        Hero currentHero = getCurrentHero();

        if (attacker == null || target == null || currentHero == null) {
            return false;
        }

        if (attacker != currentHero || !attacker.isAlive() || !target.isAlive()) {
            return false;
        }

        int damage = attacker.getAttack() - target.getEffectiveDefense();
        if (damage < 1) {
            damage = 1;
        }

        target.takeDamage(damage);
        turnsPlayed++;
        turnManager.endTurn();
        return true;
    }

    public boolean performDefend(Hero hero) {
        Hero currentHero = getCurrentHero();

        if (hero == null || currentHero == null) {
            return false;
        }

        if (hero != currentHero || !hero.isAlive()) {
            return false;
        }

        hero.setDefending(true);
        hero.heal(10);
        hero.restoreMana(5);
        turnsPlayed++;
        turnManager.endTurn();
        return true;
    }

    public boolean performWait(Hero hero) {
        Hero currentHero = getCurrentHero();

        if (hero == null || currentHero == null) {
            return false;
        }

        if (hero != currentHero || !hero.isAlive()) {
            return false;
        }

        turnsPlayed++;
        turnManager.moveCurrentHeroToEnd();
        return true;
    }

    public boolean performCastAbility(Hero caster, Hero target) {
        Hero currentHero = getCurrentHero();

        if (caster == null || target == null || currentHero == null) {
            return false;
        }

        if (caster != currentHero || !caster.isAlive() || !target.isAlive()) {
            return false;
        }

        if (!caster.useMana(20)) {
            return false;
        }

        int damage = caster.getMagic() + 10 - target.getEffectiveDefense();
        if (damage < 1) {
            damage = 1;
        }

        target.takeDamage(damage);
        turnsPlayed++;
        turnManager.endTurn();
        return true;
    }

    public boolean executeAction(BattleAction action, Hero actor, Hero target) {
        if (action == null) {
            return false;
        }
        return action.execute(this, actor, target);
    }

    public BattleResult getBattleResult() {
        if (!isBattleOver()) {
            return null;
        }

        if (partyOne.isDefeated()) {
            return new BattleResult(partyTwo, partyOne, turnsPlayed);
        }

        return new BattleResult(partyOne, partyTwo, turnsPlayed);
    }

    public Party getPartyOne() {
        return partyOne;
    }

    public Party getPartyTwo() {
        return partyTwo;
    }

    public int getTurnsPlayed() {
        return turnsPlayed;
    }

    public List<String> getAvailableActions(Hero hero) {
        List<String> actions = new ArrayList<>();

        if (hero != null && hero.isAlive()) {
            actions.add("Attack");
            actions.add("Defend");
            actions.add("Wait");
            if (hero.getCurrentMana() >= 20) {
                actions.add("Cast Ability");
            }
        }

        return actions;
    }
}