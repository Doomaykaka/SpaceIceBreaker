package deusmatrix.gui;

import deusmatrix.utils.Logger;
import deusmatrix.utils.SupportFunctions;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class MenuWindow extends JFrame {
    private ScreensaverWindow screensaverWindow = null;

    private static final int WIDTH = 200;
    private static final int HEIGHT = 375;
    private static final String WINDOW_TITLE = "Menu";

    public MenuWindow() {
        boolean isResizable = false;

        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(isResizable);

        setIconImage(SupportFunctions.getAppIcon());

        List<JButton> controls = fillWindowElements();
        addButtonsActionListeners(controls);
    }

    public void showScreensaver() {
        screensaverWindow = new ScreensaverWindow();
        screensaverWindow.create();
    }

    public void showWindow() {
        screensaverWindow.close();
        setVisible(true);
    }

    private List<JButton> fillWindowElements() {
        List<JButton> controls = new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        JButton btnNewGame = new JButton("New game");
        JButton btnLoadGame = new JButton("Load game");
        JButton btnSaveGame = new JButton("Save game");
        JButton btnStatistic = new JButton("Statistic");
        JButton btnSettings = new JButton("Settings");
        JButton btnExit = new JButton("Exit");

        int gap = 15;
        SupportFunctions.addButtonWithGap(panel, btnNewGame, gap);
        SupportFunctions.addButtonWithGap(panel, btnLoadGame, gap);
        SupportFunctions.addButtonWithGap(panel, btnSaveGame, gap);
        SupportFunctions.addButtonWithGap(panel, btnStatistic, gap);
        SupportFunctions.addButtonWithGap(panel, btnSettings, gap);
        SupportFunctions.addButtonWithGap(panel, btnExit, gap);

        controls.add(btnNewGame);
        controls.add(btnLoadGame);
        controls.add(btnSaveGame);
        controls.add(btnStatistic);
        controls.add(btnSettings);
        controls.add(btnExit);

        add(panel);

        return controls;
    }

    private void addButtonsActionListeners(List<JButton> buttons) {
        ActionListener listener = e -> {
            String command = e.getActionCommand();

            boolean runInStatisticMode;

            switch (command) {
                case "New game":
                    AdditionalGameWindows.startNewGame();
                    break;
                case "Load game":
                    runInStatisticMode = false;

                    SaveLoadWindow saveLoadWindow = new SaveLoadWindow(runInStatisticMode);
                    SwingUtilities.invokeLater(() -> saveLoadWindow.showWindow());
                    break;
                case "Save game":
                    SaveLoadWindow.saveUser();
                    break;
                case "Statistic":
                    runInStatisticMode = true;

                    SaveLoadWindow statisticWindow = new SaveLoadWindow(runInStatisticMode);
                    SwingUtilities.invokeLater(() -> statisticWindow.showWindow());
                    break;
                case "Settings":
                    OptionsWindow optionsWindow = new OptionsWindow();
                    SwingUtilities.invokeLater(() -> optionsWindow.showWindow());
                    break;
                case "Exit":
                    int confirm = JOptionPane.showConfirmDialog(
                            this, "Do you really want to get out?", "Confirmation", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        System.exit(0);
                    }
                    break;
            }
        };

        for (JButton button : buttons) {
            button.addActionListener(listener);
        }
    }

    public class ScreensaverWindow {
        private JFrame screensaverWindow;

        private static final int SCREENSAVER_WIDTH = 665;
        private static final int SCREENSAVER_HEIGHT = 380;

        private static final String SCREENSAVER_WINDOW_TITLE = "Wait please";
        private static final String SCREENSAVER_IMAGE = "screen.png";

        public ScreensaverWindow() {
            Logger.getInstance().info("creating");

            init();
            insertComponents();
        }

        private void init() {
            Logger.getInstance().info("initialization");

            boolean isResizable = false;

            Dimension preferredSize = new Dimension(SCREENSAVER_WIDTH, SCREENSAVER_HEIGHT);

            screensaverWindow = new JFrame(SCREENSAVER_WINDOW_TITLE);
            screensaverWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            screensaverWindow.setLayout((LayoutManager) null);
            screensaverWindow.setBounds(SupportFunctions.getWindowBounds(SCREENSAVER_WIDTH, SCREENSAVER_HEIGHT));
            screensaverWindow.setPreferredSize(preferredSize);
            screensaverWindow.setResizable(isResizable);

            screensaverWindow.setIconImage(SupportFunctions.getAppIcon());
        }

        private void insertComponents() {
            Logger.getInstance().info("components creation");

            Container container = screensaverWindow.getContentPane();

            ImageIcon screensaverImage = SupportFunctions.getResourceImage(SCREENSAVER_IMAGE);
            JLabel imageLabel = new JLabel();
            imageLabel.setIcon(screensaverImage);
            imageLabel.setBounds(0, 0, SCREENSAVER_WIDTH, SCREENSAVER_HEIGHT);

            container.add(imageLabel);

            screensaverWindow.pack();
        }

        public void create() {
            Logger.getInstance().info("launch");

            SwingUtilities.invokeLater(() -> screensaverWindow.setVisible(true));
        }

        public void close() {
            Logger.getInstance().info("close");

            SwingUtilities.invokeLater(() -> screensaverWindow.dispose());
        }
    }
}
