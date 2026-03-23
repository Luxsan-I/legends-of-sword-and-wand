package com.legendsofswordandwand.persistence;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.model.RankingEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private static DatabaseManager instance;

    private Map<String, Profile> profiles;
    private Map<String, CampaignProgress> campaignProgressMap;
    private Map<String, List<Party>> savedPartiesMap;
    private List<RankingEntry> rankingEntries;

    private DatabaseManager() {
        profiles = new HashMap<>();
        campaignProgressMap = new HashMap<>();
        savedPartiesMap = new HashMap<>();
        rankingEntries = new ArrayList<>();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Map<String, Profile> getProfiles() {
        return profiles;
    }

    public Map<String, CampaignProgress> getCampaignProgressMap() {
        return campaignProgressMap;
    }

    public Map<String, List<Party>> getSavedPartiesMap() {
        return savedPartiesMap;
    }

    public List<RankingEntry> getRankingEntries() {
        return rankingEntries;
    }
}