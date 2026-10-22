package app.testutils;

import app.entities.DailyPuzzle;
import app.entities.DailyPuzzleAttempt;
import app.entities.User;

import java.util.Map;

public record Seeded(Map<String, User> users, Map<String, DailyPuzzle> puzzles, Map<String, DailyPuzzleAttempt> attempts) {
}
