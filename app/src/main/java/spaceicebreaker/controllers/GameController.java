package spaceicebreaker.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import spaceicebreaker.models.User;
import spaceicebreaker.utils.Logger;

public class GameController {
    private Long livesCount;
    private final Long maxLivesCount;
    private Timer timer;
    private int timerSeconds;

    private static final int TIMER_DELAY = 1000;
    private static final long START_EXPERIENCE_TO_NEXT_LEVEL_VALUE = 100;
    private static final double EXPERIENCE_TO_NEXT_LEVEL_MULTIPLIER = 10;

    public GameController(User user) {
        this.livesCount = user.getLevel();
        this.maxLivesCount = user.getLevel();
        this.timer = createGameTimer();
        this.timerSeconds = 0;
    }

    private Timer createGameTimer() {
        Logger.getInstance().info("Create game timer");

        return new Timer(TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timerSeconds++;
            }
        });
    }

    public int getTimerSeconds() {
        return timerSeconds;
    }

    public void timerStart() {
        this.timer.start();
    }

    public void timerStop() {
        this.timer.stop();
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
                        * Math.multiplyExact((long) EXPERIENCE_TO_NEXT_LEVEL_MULTIPLIER, level - 1))
                .longValue();
    }
}
