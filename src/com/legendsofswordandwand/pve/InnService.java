package com.legendsofswordandwand.pve;

import com.legendsofswordandwand.model.Hero;
import com.legendsofswordandwand.model.HeroClass;
import com.legendsofswordandwand.model.HeroFactory;
import com.legendsofswordandwand.model.Item;
import com.legendsofswordandwand.model.ItemType;
import com.legendsofswordandwand.model.Party;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InnService {

    // Inn shop — costs and heal values per item type
    public static final int HEALTH_POTION_COST   = 200;
    public static final int HEALTH_POTION_VALUE  = 50;

    public static final int MANA_POTION_COST     = 150;
    public static final int MANA_POTION_VALUE    = 30;

    public static final int REVIVE_SCROLL_COST   = 2000;
    public static final int REVIVE_SCROLL_VALUE  = 50;   // HP restored on revive

    public static final int ATTACK_BOOST_COST    = 500;
    public static final int ATTACK_BOOST_VALUE   = 5;

    public static final int DEFENSE_BOOST_COST   = 500;
    public static final int DEFENSE_BOOST_VALUE  = 5;

    // Hero recruitment
    private static final int MAX_PARTY_SIZE        = 4;   // matches Party.MAX_HEROES
    private static final int MAX_INN_ROOM          = 10;  // heroes only available in first 10 rooms
    private static final int RECRUIT_COST_PER_LVL  = 200; // level 1 is free, else 200g * level

    private int gold;
    private final HeroFactory heroFactory;
    private final Random random;

    public InnService(int startingGold) {
        this.gold        = startingGold;
        this.heroFactory = new HeroFactory();
        this.random      = new Random();
    }

    // Package-private for testing with seeded Random
    InnService(int startingGold, Random random) {
        this.gold        = startingGold;
        this.heroFactory = new HeroFactory();
        this.random      = random;
    }

    // -------------------------------------------------------------------------
    // Restoration
    // -------------------------------------------------------------------------

    /**
     * On entering an inn, all heroes are fully healed, mana replenished, and
     * dead heroes revived. Called automatically by CampaignService on inn entry.
     */
    public void applyRestoration(Party party) {
        if (party == null) {
            return;
        }
        for (Hero hero : party.getHeroes()) {
            hero.heal(hero.getMaxHp());
            hero.restoreMana(hero.getMaxMana());
            if (!hero.isAlive()) {
                hero.revive(hero.getMaxHp());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Shop
    // -------------------------------------------------------------------------

    /**
     * Purchases one item of the given type and adds it to the party's inventory.
     *
     * @return true if purchase succeeded, false if insufficient gold or invalid args
     */
    public boolean buyItem(Party party, ItemType type) {
        if (party == null || type == null) {
            return false;
        }

        int cost  = getCost(type);
        int value = getValue(type);

        if (gold < cost) {
            return false;
        }

        gold -= cost;
        Item item = new Item(getItemName(type), type, value);
        party.getInventory().addItem(item);
        return true;
    }

    public int getCost(ItemType type) {
        if (type == null) return 0;
        switch (type) {
            case HEALTH_POTION:  return HEALTH_POTION_COST;
            case MANA_POTION:    return MANA_POTION_COST;
            case REVIVE_SCROLL:  return REVIVE_SCROLL_COST;
            case ATTACK_BOOST:   return ATTACK_BOOST_COST;
            case DEFENSE_BOOST:  return DEFENSE_BOOST_COST;
            default:             return 0;
        }
    }

    private int getValue(ItemType type) {
        if (type == null) return 0;
        switch (type) {
            case HEALTH_POTION:  return HEALTH_POTION_VALUE;
            case MANA_POTION:    return MANA_POTION_VALUE;
            case REVIVE_SCROLL:  return REVIVE_SCROLL_VALUE;
            case ATTACK_BOOST:   return ATTACK_BOOST_VALUE;
            case DEFENSE_BOOST:  return DEFENSE_BOOST_VALUE;
            default:             return 0;
        }
    }

    private String getItemName(ItemType type) {
        if (type == null) return "Unknown";
        switch (type) {
            case HEALTH_POTION:  return "Health Potion";
            case MANA_POTION:    return "Mana Potion";
            case REVIVE_SCROLL:  return "Revive Scroll";
            case ATTACK_BOOST:   return "Attack Boost";
            case DEFENSE_BOOST:  return "Defense Boost";
            default:             return "Unknown";
        }
    }

    // -------------------------------------------------------------------------
    // Hero Recruitment
    // -------------------------------------------------------------------------

    /**
     * Generates available heroes for recruitment at this inn.
     * Heroes are only available in the first 10 rooms.
     * If the party is full (4 heroes), returns an empty list.
     *
     * Heroes are random class, random level 1-4.
     * Level 1 costs 0g. Level 2+ costs 200g * level.
     */
    public List<Hero> getAvailableRecruits(Party party, int currentRoom) {
        List<Hero> recruits = new ArrayList<>();

        if (party == null || currentRoom > MAX_INN_ROOM) {
            return recruits;
        }

        if (party.getHeroes().size() >= MAX_PARTY_SIZE) {
            return recruits;
        }

        // Generate 1-3 random heroes to offer
        int count = 1 + random.nextInt(3);
        HeroClass[] classes = HeroClass.values();

        for (int i = 0; i < count; i++) {
            HeroClass heroClass = classes[random.nextInt(classes.length)];
            int level = 1 + random.nextInt(4); // 1-4
            Hero hero = heroFactory.createHero("Recruit " + (i + 1), heroClass);
            if (hero != null) {
                for (int lvl = 1; lvl < level; lvl++) {
                    hero.levelUp();
                }
                recruits.add(hero);
            }
        }

        return recruits;
    }

    /**
     * Recruits a hero into the party.
     *
     * @param party       the player's party
     * @param hero        the hero to recruit (from getAvailableRecruits)
     * @param currentRoom the current dungeon room number
     * @return true if recruitment succeeded
     */
    public boolean recruitHero(Party party, Hero hero, int currentRoom) {
        if (party == null || hero == null) {
            return false;
        }

        if (currentRoom > MAX_INN_ROOM) {
            return false;
        }

        if (party.getHeroes().size() >= MAX_PARTY_SIZE) {
            return false;
        }

        int cost = getRecruitCost(hero);
        if (gold < cost) {
            return false;
        }

        gold -= cost;
        return party.addHero(hero);
    }

    public int getRecruitCost(Hero hero) {
        if (hero == null) return 0;
        if (hero.getLevel() == 1) return 0;
        return RECRUIT_COST_PER_LVL * hero.getLevel();
    }

    // -------------------------------------------------------------------------
    // Gold
    // -------------------------------------------------------------------------

    public int getGold() {
        return gold;
    }

    public void addGold(int amount) {
        if (amount > 0) {
            gold += amount;
        }
    }

    public void deductGold(int amount) {
        if (amount > 0 && gold >= amount) {
            gold -= amount;
        }
    }
}
