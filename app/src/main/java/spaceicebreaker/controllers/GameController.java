package spaceicebreaker.controllers;

import spaceicebreaker.models.GameClass;
import spaceicebreaker.models.User;
import spaceicebreaker.utils.Logger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class GameController {
    private Long attempts;
    private final Long maxAttempts;
    private Timer timer;
    private int timerSeconds;
    private Runnable updateCallbackFromTimer;

    private static final int TIMER_DELAY = 1000;
    private static final long START_EXPERIENCE_TO_NEXT_LEVEL_VALUE = 100;
    private static final double EXPERIENCE_TO_NEXT_LEVEL_MULTIPLIER = 1.5;

    public GameController() {
        this.attempts = 0L;
        this.maxAttempts = 3L;
        this.timer = createGameTimer();
        this.timerSeconds = 0;
    }

    private Timer createGameTimer() {
        Logger.getInstance().info("Create game timer");

        return new Timer(TIMER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timerSeconds++;

                if (updateCallbackFromTimer != null) {
                    updateCallbackFromTimer.run();
                }
            }
        });
    }

    public int getTimerSeconds() {
        return timerSeconds;
    }

    public void setUpdateCallbackFromTimer(Runnable updateCallbackFromTimer) {
        this.updateCallbackFromTimer = updateCallbackFromTimer;
    }

    public void timerStart() {
        this.timer.start();
    }

    public void timerStop() {
        this.timer.stop();
    }

    public GameField createGameField(GameClass gameDifficult) {
        return new GameField(gameDifficult);
    }

    public GameFieldSolver createGameFieldSolver(GameField gameField) {
        return new GameFieldSolver(gameField);
    }

    public void addAttempts() {
        Logger.getInstance().info("Add game attempt");

        if (attempts <= maxAttempts) {
            attempts++;
        }
    }

    public long getAttempts() {
        return attempts;
    }

    public long getMaxAttempts() {
        return maxAttempts;
    }

    public void giveRewards(GameClass difficult, User user) {
        long gameTime = getTimerSeconds();
        long additionalExperience =
                (START_EXPERIENCE_TO_NEXT_LEVEL_VALUE * difficult.getNumsCountToRemove()) / gameTime;

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
        return Double.valueOf(
                        START_EXPERIENCE_TO_NEXT_LEVEL_VALUE * Math.pow(EXPERIENCE_TO_NEXT_LEVEL_MULTIPLIER, level - 1))
                .longValue();
    }
}
