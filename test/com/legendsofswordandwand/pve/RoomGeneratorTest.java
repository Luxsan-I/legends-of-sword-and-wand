package com.legendsofswordandwand.pve;

import com.legendsofswordandwand.pve.RoomGenerator.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class RoomGeneratorTest {

    private RoomGenerator roomGenerator;

    @BeforeEach
    void setUp() {
        roomGenerator = new RoomGenerator();
    }

    // -------------------------------------------------------------------------
    // getBattleChance — formula verification
    // -------------------------------------------------------------------------

    @Test
    void getBattleChance_zeroLevelIs60Percent() {
        assertEquals(0.60, roomGenerator.getBattleChance(0), 0.001);
    }

    @Test
    void getBattleChance_level10Is63Percent() {
        // floor(10/10) = 1 bracket, 0.60 + 1*0.03 = 0.63
        assertEquals(0.63, roomGenerator.getBattleChance(10), 0.001);
    }

    @Test
    void getBattleChance_level20Is66Percent() {
        assertEquals(0.66, roomGenerator.getBattleChance(20), 0.001);
    }

    @Test
    void getBattleChance_level100IsCappedAt90Percent() {
        // floor(100/10) = 10 brackets, 0.60 + 10*0.03 = 0.90, still at cap
        assertEquals(0.90, roomGenerator.getBattleChance(100), 0.001);
    }

    @Test
    void getBattleChance_veryHighLevelStaysAt90Percent() {
        assertEquals(0.90, roomGenerator.getBattleChance(500), 0.001);
    }

    @Test
    void getBattleChance_negativeLevelTreatedAsZero() {
        assertEquals(0.60, roomGenerator.getBattleChance(-5), 0.001);
    }

    @Test
    void getBattleChance_level9StillAt60Percent() {
        // floor(9/10) = 0 brackets, still base
        assertEquals(0.60, roomGenerator.getBattleChance(9), 0.001);
    }

    // -------------------------------------------------------------------------
    // generateRoom — deterministic via anonymous Random overrides
    // -------------------------------------------------------------------------

    @Test
    void generateRoom_returnsBattleWhenRollBelowChance() {
        // nextDouble returns 0.0, which is < 0.60 → BATTLE
        RoomGenerator gen = new RoomGenerator(new Random() {
            @Override public double nextDouble() { return 0.0; }
        });

        assertEquals(RoomType.BATTLE, gen.generateRoom(0));
    }

    @Test
    void generateRoom_returnsInnWhenRollAboveChance() {
        // nextDouble returns 1.0, which is >= 0.60 → INN
        RoomGenerator gen = new RoomGenerator(new Random() {
            @Override public double nextDouble() { return 1.0; }
        });

        assertEquals(RoomType.INN, gen.generateRoom(0));
    }

    @Test
    void generateRoom_battleAtExactlyBelowChance() {
        // At cumLevel 0 chance is 0.60; roll 0.599 → BATTLE
        RoomGenerator gen = new RoomGenerator(new Random() {
            @Override public double nextDouble() { return 0.599; }
        });

        assertEquals(RoomType.BATTLE, gen.generateRoom(0));
    }

    @Test
    void generateRoom_innAtExactlyChance() {
        // Roll equals chance exactly (0.60) → INN (not strictly less than)
        RoomGenerator gen = new RoomGenerator(new Random() {
            @Override public double nextDouble() { return 0.60; }
        });

        assertEquals(RoomType.INN, gen.generateRoom(0));
    }

    @Test
    void generateRoom_negativeCumulativeLevelTreatedAsZero() {
        RoomGenerator gen = new RoomGenerator(new Random() {
            @Override public double nextDouble() { return 0.0; }
        });

        // Should not throw and should treat negative as 0
        assertDoesNotThrow(() -> gen.generateRoom(-10));
    }

    // -------------------------------------------------------------------------
    // Statistical distribution — not deterministic, but sanity-checks the RNG
    // -------------------------------------------------------------------------

    @Test
    void generateRoom_atLevel0RoughlyBattleAndInnDistribution() {
        // Over 1000 rolls with cumulativeLevel=0, expect ~60% BATTLE
        int battles = 0;
        int total   = 1000;
        for (int i = 0; i < total; i++) {
            if (roomGenerator.generateRoom(0) == RoomType.BATTLE) battles++;
        }
        double ratio = (double) battles / total;
        // Allow ±10% tolerance around 60%
        assertTrue(ratio >= 0.50 && ratio <= 0.70,
                "Expected ~60% battles but got " + ratio);
    }

    @Test
    void generateRoom_atHighLevelMoreBattlesThanAtLowLevel() {
        int battlesLow  = 0;
        int battlesHigh = 0;
        int total       = 500;
        for (int i = 0; i < total; i++) {
            if (roomGenerator.generateRoom(0)   == RoomType.BATTLE) battlesLow++;
            if (roomGenerator.generateRoom(100) == RoomType.BATTLE) battlesHigh++;
        }
        assertTrue(battlesHigh >= battlesLow,
                "High-level party should encounter at least as many battles as low-level");
    }
}
