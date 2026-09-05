package spaceicebreaker.gui.game;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.*;
import spaceicebreaker.controllers.GameController;
import spaceicebreaker.controllers.GameOperationsController;
import spaceicebreaker.dao.StatisticsDAO;
import spaceicebreaker.dao.UsersDAO;
import spaceicebreaker.models.*;
import spaceicebreaker.utils.HibernateConfiguration;
import spaceicebreaker.utils.Logger;

public class GameWindow extends JFrame {
    private User user;
    private GameClass characterClass;
    private GameController gameController;
    private GameOperationsController gameOperationsController;

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public GameWindow(User user, GameClass characterClass) {
        this.user = user;
        this.characterClass = characterClass;

        initController();
        initUI();
    }

    private void initController() {
        UsersDAO usersDAO = new UsersDAO(HibernateConfiguration.getEntityManagerFactory());
        StatisticsDAO statisticsDAO = new StatisticsDAO(HibernateConfiguration.getEntityManagerFactory());

        gameOperationsController = new GameOperationsController(usersDAO, statisticsDAO);

        gameController = new GameController(user);

        gameController.timerStart();
    }

    private void initUI() {
        Logger.getInstance().info("initializing Game UI");

        setTitle("Space Shooter");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setFocusable(true);

        setLayout(new BorderLayout());

        setBackground(Color.BLACK);

        add(new GamePanel(this, user, characterClass, gameController, gameOperationsController));

        pack();
    }

    public void create() {
        Logger.getInstance().info("launch");

        this.gameController.timerStart();

        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
