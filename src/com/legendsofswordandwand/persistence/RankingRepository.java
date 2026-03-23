package com.legendsofswordandwand.persistence;

import com.legendsofswordandwand.model.Profile;
import com.legendsofswordandwand.model.RankingEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RankingRepository {
    private DatabaseManager databaseManager;

    public RankingRepository() {
        this.databaseManager = DatabaseManager.getInstance();
    }

    public void recordScore(Profile profile, int score) {
        if (profile == null || score < 0) {
            return;
        }

        profile.addScore(score);
        updateRankingEntry(profile);
    }

    public void recordPvPResult(Profile winner, Profile loser) {
        if (winner == null || loser == null) {
            return;
        }

        winner.recordWin();
        loser.recordLoss();

        updateRankingEntry(winner);
        updateRankingEntry(loser);
    }

    public List<RankingEntry> getHallOfFame() {
        List<RankingEntry> entries = new ArrayList<>(databaseManager.getRankingEntries());
        entries.sort(Comparator.comparingInt(RankingEntry::getScore).reversed());
        return entries;
    }

    public List<RankingEntry> getLeagueStandings() {
        List<RankingEntry> entries = new ArrayList<>(databaseManager.getRankingEntries());
        entries.sort(Comparator.comparingInt(RankingEntry::getWins).reversed());
        return entries;
    }

    private void updateRankingEntry(Profile profile) {
        List<RankingEntry> entries = databaseManager.getRankingEntries();

        for (int i = 0; i < entries.size(); i++) {
            RankingEntry entry = entries.get(i);

            if (entry.getUsername().equals(profile.getUsername())) {
                entries.set(i, new RankingEntry(
                        profile.getUsername(),
                        profile.getTotalScore(),
                        profile.getPvpWins(),
                        profile.getPvpLosses()
                ));
                return;
            }
        }

        entries.add(new RankingEntry(
                profile.getUsername(),
                profile.getTotalScore(),
                profile.getPvpWins(),
                profile.getPvpLosses()
        ));
    }
}