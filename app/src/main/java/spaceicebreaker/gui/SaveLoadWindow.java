package deusmatrix.gui;

import deusmatrix.controllers.GameOperationsController;
import deusmatrix.dao.*;
import deusmatrix.models.Statistic;
import deusmatrix.models.User;
import deusmatrix.utils.HibernateConfiguration;
import deusmatrix.utils.SupportFunctions;
import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class SaveLoadWindow extends JFrame {
    private static GameOperationsController gameOperationsController;
    private final boolean runInStatisticMode;
    private final JTable table;
    private final DefaultTableModel tableModel;

    private static volatile User currentUser;

    private static final int WIDTH = 600;
    private static final int HEIGHT = 450;
    private static final String WINDOW_TITLE = "Load game";

    public SaveLoadWindow(boolean runInStatisticMode) {
        this.runInStatisticMode = runInStatisticMode;

        boolean isResizable = false;

        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setResizable(isResizable);

        setLayout(new BorderLayout());

        String[] columnNames = {"User name", "Load", "Delete"};

        if (runInStatisticMode) {
            columnNames = new String[] {"User name", "View statistic"};
        }

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);

        table.setFillsViewportHeight(true);
        table.setRowHeight(45);
        table.setAutoCreateRowSorter(true);

        if (runInStatisticMode) {
            table.getColumnModel().getColumn(1).setCellRenderer(getTableCellRenderer("View statistic"));
        } else {
            table.getColumnModel().getColumn(1).setCellRenderer(getTableCellRenderer("Load"));
            table.getColumnModel().getColumn(2).setCellRenderer(getTableCellRenderer("Delete"));
        }

        table.getSelectionModel().addListSelectionListener(getTableListSelectionListener());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomRightPanel = new JPanel();
        bottomRightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottomRightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnLoadFromFile = new JButton("Load save from file");
        btnLoadFromFile.addActionListener(e -> loadUserFromFile());
        bottomRightPanel.add(btnLoadFromFile);

        if (!runInStatisticMode) {
            add(bottomRightPanel, BorderLayout.SOUTH);
        }

        initController();
        loadUsers();
    }

    private TableCellRenderer getTableCellRenderer(String text) {
        return new TableCellRenderer() {
            private JButton button;

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (button == null) {
                    button = new JButton();
                    button.setPreferredSize(new Dimension(100, 35));
                }

                button.setText(text);

                return button;
            }
        };
    }

    private ListSelectionListener getTableListSelectionListener() {
        return e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = table.getSelectedRow();
                if (viewRow < 0) return;

                int viewColumn = table.getSelectedColumn();
                if (viewColumn < 0) return;

                int modelRow = table.convertRowIndexToModel(viewRow);
                int modelColumn = table.convertColumnIndexToModel(viewColumn);

                User user = (User) tableModel.getValueAt(modelRow, 1);

                if (runInStatisticMode) {
                    viewStatsButtonClick(modelColumn, user);
                } else {
                    loadRemoveButtonsClick(modelColumn, user);
                }
            }
        };
    }

    private void loadRemoveButtonsClick(int modelColumn, User user) {
        if (modelColumn == 1) {
            setCurrentUser(user);

            SupportFunctions.showMessage("User loaded");

            AdditionalGameWindows.selectGameSetting(user);

            this.dispose();
        } else if (modelColumn == 2) {
            removeUser(user);
        }
    }

    private void viewStatsButtonClick(int modelColumn, User user) {
        if (modelColumn == 1) {
            Statistic statistic = user.getStatistic();

            SupportFunctions.showMessage("User stats loaded");

            StringBuilder builder = new StringBuilder();

            builder.append("User ");
            builder.append(user.getName());
            builder.append(":\n");

            builder.append("\n");

            builder.append("create date - ");
            builder.append(user.getCreationDate());
            builder.append("\n");
            builder.append("level - ");
            builder.append(user.getLevel());
            builder.append("\n");
            builder.append("experience - ");
            builder.append(user.getExperience());
            builder.append("\n");
            builder.append("experience to next level - ");
            builder.append(user.getExperienceToNextLevel());
            builder.append("\n");

            builder.append("\n");

            builder.append("last play date - ");
            builder.append(statistic.getLastPlayDate());
            builder.append("\n");
            builder.append("days in game - ");
            builder.append(statistic.getDaysInGame());
            builder.append("\n");
            builder.append("easy difficult wins - ");
            builder.append(statistic.getEasyWins());
            builder.append("\n");
            builder.append("middle difficult wins - ");
            builder.append(statistic.getMiddleWins());
            builder.append("\n");
            builder.append("hard difficult wins - ");
            builder.append(statistic.getHardWins());
            builder.append("\n");
            builder.append("easy difficult best time (sec) - ");
            builder.append(statistic.getEasyBestTime());
            builder.append("\n");
            builder.append("middle difficult best time (sec) - ");
            builder.append(statistic.getMiddleBestTime());
            builder.append("\n");
            builder.append("hard difficult best time (sec) - ");
            builder.append(statistic.getHardBestTime());
            builder.append("\n");
            builder.append("easy difficult lose count - ");
            builder.append(statistic.getEasyLose());
            builder.append("\n");
            builder.append("middle difficult lose count - ");
            builder.append(statistic.getMiddleLose());
            builder.append("\n");
            builder.append("hard difficult lose count - ");
            builder.append(statistic.getHardLose());

            SupportFunctions.showMessage(builder.toString());
        }
    }

    private void removeUser(User user) {
        Object[] options = {"Remove", "Cancel"};

        SwingUtilities.invokeLater(() -> {
            long answer = JOptionPane.showOptionDialog(
                    null,
                    "Remove user?",
                    "User remove",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (answer == 0) {
                gameOperationsController.removeUser(user.getId());

                SupportFunctions.showMessage("User removed");
            }

            refreshTable();
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        loadUsers();
    }

    private void initController() {
        UsersDAO usersDAO = new UsersDAO(HibernateConfiguration.getEntityManagerFactory());
        StatisticsDAO statisticsDAO = new StatisticsDAO(HibernateConfiguration.getEntityManagerFactory());

        gameOperationsController = new GameOperationsController(usersDAO, statisticsDAO);
    }

    private void loadUsers() {
        java.util.List<User> users = gameOperationsController.getAllUsers();
        for (User slot : users) {
            addLoadSlot(slot);
        }
    }

    private void addLoadSlot(User user) {
        StringBuilder builder = new StringBuilder();

        builder.append(user.getName());
        builder.append(" [");
        builder.append("level - ");
        builder.append(user.getLevel());
        builder.append(" lvl");
        builder.append("]");

        if (runInStatisticMode) {
            tableModel.addRow(new Object[] {builder.toString(), user});
        } else {
            tableModel.addRow(new Object[] {builder.toString(), user, user});
        }
    }

    private void loadUserFromFile() {
        File saveFile = SupportFunctions.chooseFile();
        if (saveFile == null) return;

        User loadedUser = gameOperationsController.importUser(saveFile);
        if (loadedUser != null) {
            gameOperationsController.saveUser(loadedUser);
            refreshTable();
            SupportFunctions.showMessage("User imported from file");
        } else {
            SupportFunctions.showMessage("Failed to import user");
        }
    }

    public void showWindow() {
        setVisible(true);
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void saveUser() {
        if (getCurrentUser() == null || gameOperationsController == null) {
            SupportFunctions.showMessage("Load game before save");
            return;
        }

        File saveFile = SupportFunctions.saveFile();

        if (saveFile == null) {
            SupportFunctions.showMessage("Bad save file");
            return;
        }

        gameOperationsController.exportUser(getCurrentUser(), saveFile);

        SupportFunctions.showMessage("User exported to file");
    }
}
