package spaceicebreaker.gui;

import javax.swing.*;
import spaceicebreaker.controllers.GameOperationsController;
import spaceicebreaker.dao.*;
import spaceicebreaker.gui.game.GameWindow;
import spaceicebreaker.models.GameClass;
import spaceicebreaker.models.User;
import spaceicebreaker.utils.HibernateConfiguration;
import spaceicebreaker.utils.Logger;

public class AdditionalGameWindows {
    private static volatile GameOperationsController gameOperationsController;

    public static void startNewGame() {
        SwingUtilities.invokeLater(() -> {
            String name = JOptionPane.showInputDialog(
                    null, "Enter the user's name:", "User Creation", JOptionPane.QUESTION_MESSAGE);

            if (name != null && !name.trim().isEmpty()) {
                Logger.getInstance().info("Start new game");

                GameOperationsController controller = getGameOperationsController();

                selectGameSetting(controller.createNewUser(name));
            }
        });
    }

    public static void selectGameSetting(User user) {
        SwingUtilities.invokeLater(() -> {
            Object[] options = {"Scout", "Tank", "Damage dealer"};

            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Choose a character class",
                    "Character class selection",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            GameClass characterClass = null;

            if (choice == 0) {
                Logger.getInstance().info("Scout class chose");

                characterClass = GameClass.SCOUT;
            } else if (choice == 1) {
                Logger.getInstance().info("Tank class chose");

                characterClass = GameClass.TANK;
            } else if (choice == 2) {
                Logger.getInstance().info("Damage dealer class chose");

                characterClass = GameClass.DAMAGE_DEALER;
            } else {
                return;
            }

            GameWindow gameWindow = new GameWindow(user, characterClass);
            gameWindow.create();
        });
    }

    private static synchronized GameOperationsController getGameOperationsController() {
        if (gameOperationsController != null) {
            return gameOperationsController;
        }

        UsersDAO usersDAO = new UsersDAO(HibernateConfiguration.getEntityManagerFactory());
        StatisticsDAO statisticsDAO = new StatisticsDAO(HibernateConfiguration.getEntityManagerFactory());

        gameOperationsController = new GameOperationsController(usersDAO, statisticsDAO);

        return gameOperationsController;
    }
}
