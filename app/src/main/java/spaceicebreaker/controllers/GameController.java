package spaceicebreaker.controllers;

import spaceicebreaker.models.User;
import spaceicebreaker.utils.Logger;

public class GameController {
    private Long livesCount;
    private Long maxLivesCount;

    private static final long START_EXPERIENCE_TO_NEXT_LEVEL_VALUE = 100;
    private static final double EXPERIENCE_TO_NEXT_LEVEL_MULTIPLIER = 100;

    public GameController(User user) {
        this.livesCount = user.getLevel();
        this.maxLivesCount = user.getLevel();
    }

    public void subLives() {
        Logger.getInstance().info("Sub lives");

        if (livesCount > 0) {
            livesCount--;
        }
    }

    public void setLivesCount(Long currentLivesCount) {
        livesCount = currentLivesCount;
    }

    public void setMaxLivesCount(Long newMaxLivesCount) {
        maxLivesCount = newMaxLivesCount;
    }

    public Long getLivesCount() {
        return livesCount;
    }

    public Long getMaxLivesCount() {
        return maxLivesCount;
    }

    public void giveRewards(Long score, User user) {
        long additionalExperience = score;

        long currentLevel = user.getLevel();
        long currentExperience = user.getExperience() + additionalExperience;

        if (additionalExperience > 0) {
            long experienceToNextLevel = user.getExperienceToNextLevel();

            while (currentExperience > experienceToNextLevel) {
                experienceToNextLevel = calculateExperienceToNextLevel(currentLevel + 1);
                currentLevel++;
            }

            user.setLevel(currentLevel);
            user.setExperience(currentExperience);
            user.setExperienceToNextLevel(experienceToNextLevel);
        }
    }

    private long calculateExperienceToNextLevel(long level) {
        return Double.valueOf(START_EXPERIENCE_TO_NEXT_LEVEL_VALUE
                        + Math.multiplyExact((long) EXPERIENCE_TO_NEXT_LEVEL_MULTIPLIER, level - 1))
                .longValue();
    }
}
