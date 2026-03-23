package com.legendsofswordandwand.persistence;

import com.legendsofswordandwand.model.CampaignProgress;
import com.legendsofswordandwand.model.Profile;

public class CampaignRepository {
    private DatabaseManager databaseManager;

    public CampaignRepository() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public void saveCampaign(Profile profile, CampaignProgress progress) {
        if (profile == null || progress == null) {
            return;
        }

        databaseManager.getCampaignProgressMap().put(profile.getUsername(), progress);
    }

    public CampaignProgress loadCampaign(Profile profile) {
        if (profile == null) {
            return null;
        }

        return databaseManager.getCampaignProgressMap().get(profile.getUsername());
    }

    public boolean hasIncompleteCampaign(Profile profile) {
        if (profile == null) {
            return false;
        }

        CampaignProgress progress = databaseManager.getCampaignProgressMap().get(profile.getUsername());
        return progress != null && !progress.isCampaignCompleted();
    }

    public void deleteCampaign(Profile profile) {
        if (profile == null) {
            return;
        }

        databaseManager.getCampaignProgressMap().remove(profile.getUsername());
    }
}