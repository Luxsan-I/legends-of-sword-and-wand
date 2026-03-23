package com.legendsofswordandwand.persistence;

import com.legendsofswordandwand.model.Party;
import com.legendsofswordandwand.model.Profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PartyRepository {
    private DatabaseManager databaseManager;

    public PartyRepository() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public List<Party> loadSavedParties(Profile profile) {
        if (profile == null) {
            return new ArrayList<>();
        }

        Map<String, List<Party>> savedPartiesMap = databaseManager.getSavedPartiesMap();
        return savedPartiesMap.getOrDefault(profile.getUsername(), new ArrayList<>());
    }

    public boolean saveParty(Profile profile, Party party) {
        if (profile == null || party == null) {
            return false;
        }

        Map<String, List<Party>> savedPartiesMap = databaseManager.getSavedPartiesMap();
        List<Party> parties = savedPartiesMap.getOrDefault(profile.getUsername(), new ArrayList<>());

        if (parties.size() >= 5) {
            return false;
        }

        parties.add(party);
        savedPartiesMap.put(profile.getUsername(), parties);
        return true;
    }

    public boolean replaceParty(Profile profile, int index, Party party) {
        if (profile == null || party == null) {
            return false;
        }

        Map<String, List<Party>> savedPartiesMap = databaseManager.getSavedPartiesMap();
        List<Party> parties = savedPartiesMap.get(profile.getUsername());

        if (parties == null || index < 0 || index >= parties.size()) {
            return false;
        }

        parties.set(index, party);
        return true;
    }
}