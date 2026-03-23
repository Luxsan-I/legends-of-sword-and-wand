package com.legendsofswordandwand.model;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Item> items;

    public Inventory() {
        this.items = new ArrayList<>();
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        if (item != null) {
            items.add(item);
        }
    }

    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    public boolean useItem(Item item, Hero hero) {
        if (item == null || hero == null) {
            return false;
        }

        if (!items.contains(item)) {
            return false;
        }

        boolean used = item.useOn(hero);
        if (used) {
            items.remove(item);
        }
        return used;
    }

    public int size() {
        return items.size();
    }
}