package com.example.rpg.util;

public final class EloCalculator {
    private EloCalculator() {}

    public static int calculateNewRating(int rating, int opponentRating, double score, int kFactor) {
        double expected = 1.0 / (1.0 + Math.pow(10.0, (opponentRating - rating) / 400.0));
        return (int) Math.round(rating + kFactor * (score - expected));
    }
}
