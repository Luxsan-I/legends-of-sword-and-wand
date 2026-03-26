package com.legendsofswordandwand.pvp;

import com.legendsofswordandwand.battle.AttackAction;
import com.legendsofswordandwand.battle.CastAbilityAction;
import com.legendsofswordandwand.battle.DefendAction;
import com.legendsofswordandwand.battle.WaitAction;
import com.legendsofswordandwand.model.Hero;

/**
 * State representing an active player's turn.
 * Validates that the acting hero is the correct current hero before
 * delegating to BattleSystem, then fires Observer events.
 *
 * Design Pattern: State (concrete state)
 */
public class PlayerTurnState implements BattleState {

    private final String playerLabel;

    public PlayerTurnState(String playerLabel) {
        this.playerLabel = playerLabel;
    }

    @Override
    public void handleAttack(PvPController context, Hero actor, Hero target) {
        if (!isCurrentHero(context, actor)) {
            return;
        }

        int targetHpBefore = target.getCurrentHp();
        boolean success = context.getBattleSystem().executeAction(new AttackAction(), actor, target);

        if (success) {
            int damage = targetHpBefore - target.getCurrentHp();
            context.fireEvent(new BattleEvent(
                    BattleEvent.Type.HERO_ATTACKED, actor, target,
                    actor.getName() + " attacks " + target.getName() + " for " + damage + " damage!"
            ));

            if (!target.isAlive()) {
                context.fireEvent(new BattleEvent(
                        BattleEvent.Type.HERO_DIED, null, target,
                        target.getName() + " has been defeated!"
                ));
            }
            context.advanceTurn();
        }
    }

    @Override
    public void handleDefend(PvPController context, Hero actor) {
        if (!isCurrentHero(context, actor)) {
            return;
        }

        boolean success = context.getBattleSystem().executeAction(new DefendAction(), actor, null);

        if (success) {
            context.fireEvent(new BattleEvent(
                    BattleEvent.Type.HERO_DEFENDED, actor, null,
                    actor.getName() + " takes a defensive stance!"
            ));
            context.advanceTurn();
        }
    }

    @Override
    public void handleCastAbility(PvPController context, Hero actor, Hero target) {
        if (!isCurrentHero(context, actor)) {
            return;
        }

        int targetHpBefore = target.getCurrentHp();
        boolean success = context.getBattleSystem().executeAction(new CastAbilityAction(), actor, target);

        if (success) {
            int damage = targetHpBefore - target.getCurrentHp();
            context.fireEvent(new BattleEvent(
                    BattleEvent.Type.HERO_CAST_ABILITY, actor, target,
                    actor.getName() + " casts an ability on " + target.getName() + " for " + damage + " damage!"
            ));

            if (!target.isAlive()) {
                context.fireEvent(new BattleEvent(
                        BattleEvent.Type.HERO_DIED, null, target,
                        target.getName() + " has been defeated!"
                ));
            }
            context.advanceTurn();
        }
    }

    @Override
    public void handleWait(PvPController context, Hero actor) {
        if (!isCurrentHero(context, actor)) {
            return;
        }

        boolean success = context.getBattleSystem().executeAction(new WaitAction(), actor, null);

        if (success) {
            context.fireEvent(new BattleEvent(
                    BattleEvent.Type.HERO_WAITED, actor, null,
                    actor.getName() + " waits..."
            ));
            context.advanceTurn();
        }
    }

    @Override
    public String getStateLabel() {
        return playerLabel + "'s Turn";
    }

    private boolean isCurrentHero(PvPController context, Hero actor) {
        return actor != null && actor == context.getBattleSystem().getCurrentHero();
    }
}
