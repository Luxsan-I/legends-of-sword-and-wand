package com.legendsofswordandwand.pve;

import java.util.Random;

public class RoomGenerator {

    public enum RoomType {
        BATTLE,
        INN
    }

    private static final double BASE_BATTLE_CHANCE = 0.60;
    private static final double CHANCE_INCREMENT    = 0.03;
    private static final double MAX_BATTLE_CHANCE   = 0.90;
    private static final int    LEVEL_BRACKET       = 10;

    private final Random random;

    public RoomGenerator() {
        this.random = new Random();
    }

    // Package-private constructor for testing with a seeded Random
    RoomGenerator(Random random) {
        this.random = random;
    }

    /**
     * Determines the type of the next room based on the party's cumulative level.
     * Battle chance starts at 60% and increases by 3% per 10 cumulative levels,
     * capped at 90%.
     *
     * @param cumulativeLevel sum of all hero levels in the party
     * @return RoomType.BATTLE or RoomType.INN
     */
    public RoomType generateRoom(int cumulativeLevel) {
        if (cumulativeLevel < 0) {
            cumulativeLevel = 0;
        }

        int brackets = cumulativeLevel / LEVEL_BRACKET;
        double battleChance = Math.min(
                BASE_BATTLE_CHANCE + brackets * CHANCE_INCREMENT,
                MAX_BATTLE_CHANCE
        );

        return random.nextDouble() < battleChance ? RoomType.BATTLE : RoomType.INN;
    }

    /**
     * Returns the battle probability for a given cumulative level.
     * Useful for display or testing without triggering RNG.
     */
    public double getBattleChance(int cumulativeLevel) {
        if (cumulativeLevel < 0) {
            cumulativeLevel = 0;
        }
        int brackets = cumulativeLevel / LEVEL_BRACKET;
        return Math.min(
                BASE_BATTLE_CHANCE + brackets * CHANCE_INCREMENT,
                MAX_BATTLE_CHANCE
        );
    }
}
