package deusmatrix.gui;

import deusmatrix.controllers.GameOperationsController;
import deusmatrix.dao.*;
import deusmatrix.models.GameDifficult;
import deusmatrix.models.User;
import deusmatrix.utils.HibernateConfiguration;
import deusmatrix.utils.Logger;
import javax.swing.*;

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
            Object[] options = {"Easy", "Middle", "Hard"};

            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Select the game difficult",
                    "Game difficult selection",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            GameClass difficult = null;

            if (choice == 0) {
                Logger.getInstance().info("Easy game setting chose");

                difficult = GameClass.EASY;
            } else if (choice == 1) {
                Logger.getInstance().info("Middle game setting chose");

                difficult = GameClass.MIDDLE;
            } else if (choice == 2) {
                Logger.getInstance().info("Hard game setting chose");

                difficult = GameClass.HARD;
            } else {
                return;
            }

            GameWindow gameWindow = new GameWindow(user, difficult);
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
