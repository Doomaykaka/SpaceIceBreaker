package deusmatrix.gui;

import deusmatrix.controllers.GameOperationsController;
import deusmatrix.controllers.SudokuGameController;
import deusmatrix.dao.StatisticsDAO;
import deusmatrix.dao.UsersDAO;
import deusmatrix.models.*;
import deusmatrix.utils.HibernateConfiguration;
import deusmatrix.utils.Logger;
import deusmatrix.utils.SupportFunctions;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import javax.swing.border.Border;

public class GameWindow extends JFrame {
    private User user;
    private GameClass difficult;
    private GameController sudokuGameController;
    private GameOperationsController gameOperationsController;

    private GameField gameField;
    private GameField solvedGameField;

    private JLabel timerLabel;
    private JLabel attemptsLabel;
    private JLabel difficultyLabel;
    private JPanel sudokuPanel;
    private JButton[] numberButtons;
    private JButton hintButton;

    private JButton[][] cells = new JButton[GameField.FIELD_SIZE][GameField.FIELD_SIZE];

    private int selectedRow = -1;
    private int selectedColumn = -1;

    private int usedHintsCount = 0;

    private boolean gameOver;

    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MILLISECONDS_IN_SECOND = 1000;

    public GameWindow(User user, GameClass difficult) {
        this.user = user;
        this.difficult = difficult;
        this.gameOver = false;

        initController();
        initUI();
    }

    private void initController() {
        UsersDAO usersDAO = new UsersDAO(HibernateConfiguration.getEntityManagerFactory());
        StatisticsDAO statisticsDAO = new StatisticsDAO(HibernateConfiguration.getEntityManagerFactory());

        gameOperationsController = new GameOperationsController(usersDAO, statisticsDAO);

        sudokuGameController = new GameController();
        Runnable updateCallbackFromTimer = createUpdateTimerLabelCallback(sudokuGameController);
        sudokuGameController.setUpdateCallbackFromTimer(updateCallbackFromTimer);

        sudokuGameController.timerStart();
    }

    private Runnable createUpdateTimerLabelCallback(GameController sudokuGameController) {
        return new Runnable() {
            @Override
            public void run() {
                long timerSeconds = sudokuGameController.getTimerSeconds();

                long timerSecondsMod = timerSeconds % SECONDS_IN_MINUTE;
                long timerMinutes = timerSeconds / SECONDS_IN_MINUTE;

                timerLabel.setText(
                        "Time: " + String.format("%02d", timerMinutes) + ":" + String.format("%02d", timerSecondsMod));
            }
        };
    }

    private void initUI() {
        Logger.getInstance().info("initializing Sudoku UI");

        setTitle("Deus Matrix — Sudoku");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        addTopPanel();
        addCentralPanel();
        addBottomPanel();

        pack();
    }

    private void addTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(timerLabel, BorderLayout.WEST);

        difficultyLabel = new JLabel("Difficulty: ");
        difficultyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topPanel.add(difficultyLabel, BorderLayout.CENTER);

        attemptsLabel = new JLabel("Attempts: 0 / " + this.sudokuGameController.getMaxAttempts());
        attemptsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(attemptsLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    private void addCentralPanel() {
        sudokuPanel = new JPanel();
        sudokuPanel.setLayout(new GridBagLayout());

        addCentralPanelColumnsLabels();
        addCentralPanelRowsLabels();
        addCentralPanelColumnsRightBorder();
        addGameField();
    }

    private void addCentralPanelColumnsLabels() {
        for (int column = 0; column < GameField.FIELD_SIZE; column++) {
            JLabel columnLabel = new JLabel(String.valueOf(column + 1));
            columnLabel.setHorizontalAlignment(SwingConstants.CENTER);
            columnLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            addGridComponent(sudokuPanel, columnLabel, column + 1, 0);
        }
    }

    private void addCentralPanelRowsLabels() {
        for (int row = 0; row < GameField.FIELD_SIZE; row++) {
            JLabel rowLabel = new JLabel(String.valueOf(row + 1));
            rowLabel.setHorizontalAlignment(SwingConstants.CENTER);
            rowLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            addGridComponent(sudokuPanel, rowLabel, 0, row + 1);
        }
    }

    private void addCentralPanelColumnsRightBorder() {
        for (int row = 0; row < GameField.FIELD_SIZE; row++) {
            JLabel borderLabel = new JLabel(" ");
            borderLabel.setHorizontalAlignment(SwingConstants.CENTER);
            borderLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            addGridComponent(sudokuPanel, borderLabel, GameField.FIELD_SIZE + 1, row + 1);
        }
    }

    private void addGameField() {
        for (int row = 0; row < GameField.FIELD_SIZE; row++) {
            for (int column = 0; column < GameField.FIELD_SIZE; column++) {
                JButton cell = new JButton(" ");
                cell.setFont(new Font("Arial", Font.BOLD, 20));
                cell.setFocusPainted(true);
                cell.setContentAreaFilled(true);
                cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

                drawBlockBorders(cell, row, column);

                final int r = row;
                final int c = column;

                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        selectCell(r, c);
                    }
                });

                cell.addKeyListener(getGameKeyListener());

                cells[row][column] = cell;
                addGridComponent(sudokuPanel, cell, column + 1, row + 1);
            }
        }

        add(sudokuPanel, BorderLayout.CENTER);
    }

    private void addBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        numberButtons = new JButton[GameField.FIELD_SIZE];
        for (int i = 0; i < GameField.FIELD_SIZE; i++) {
            final int value = i + 1;
            numberButtons[i] = new JButton(String.valueOf(value));
            numberButtons[i].addActionListener(e -> onNumberButtonClick(value));
            bottomPanel.add(numberButtons[i]);
        }

        hintButton = new JButton("Hint");
        hintButton.addActionListener(e -> showHint());
        bottomPanel.add(hintButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addGridComponent(JPanel panel, Component component, int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = x;
        gbc.gridy = y;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);

        panel.add(component, gbc);
    }

    private void drawBlockBorders(JButton cell, int row, int column) {
        Color thinColor = Color.LIGHT_GRAY;
        Color thickColor = Color.BLACK;
        int thinWidth = 1;
        int thickWidth = 6;

        Border innerBorder = BorderFactory.createLineBorder(thinColor, thinWidth);

        int topInset = thinWidth;
        int leftInset = thinWidth;
        int bottomInset = thinWidth;
        int rightInset = thinWidth;

        boolean isBlockRightEdge = (column + 1) % GameField.BLOCKS_IN_LINE_COUNT == 0;
        boolean isBlockBottomEdge = (row + 1) % GameField.BLOCKS_IN_LINE_COUNT == 0;
        boolean isBlockLeftEdge = (column + 1) % GameField.BLOCKS_IN_LINE_COUNT == 1;
        boolean isBlockTopEdge = (row + 1) % GameField.BLOCKS_IN_LINE_COUNT == 1;

        if (isBlockRightEdge) {
            rightInset = thickWidth;
        }

        if (isBlockBottomEdge) {
            bottomInset = thickWidth;
        }

        if (isBlockLeftEdge) {
            leftInset = thickWidth;
        }

        if (isBlockTopEdge) {
            topInset = thickWidth;
        }

        if (column == 0) {
            leftInset = thickWidth;
        }

        if (row == 0) {
            topInset = thickWidth;
        }

        Border outerBorder = BorderFactory.createMatteBorder(topInset, leftInset, bottomInset, rightInset, thickColor);

        cell.setBorder(BorderFactory.createCompoundBorder(outerBorder, innerBorder));
    }

    private void selectCell(int row, int column) {
        selectedRow = row;
        selectedColumn = column;

        updateCellHighlighting();
    }

    private void updateCellHighlighting() {
        for (int row = 0; row < GameField.FIELD_SIZE; row++) {
            for (int column = 0; column < GameField.FIELD_SIZE; column++) {
                if (row == selectedRow || column == selectedColumn) {
                    cells[row][column].setSelected(true);
                } else {
                    cells[row][column].setSelected(false);
                }
            }
        }
    }

    public void setCellValue(int row, int column, int value) {
        if (row >= 0 && row < GameField.FIELD_SIZE && column >= 0 && column < GameField.FIELD_SIZE) {
            cells[row][column].setText(value > 0 ? String.valueOf(value) : " ");
        }
    }

    private void onNumberButtonClick(int value) {
        if (this.gameOver) {
            return;
        }

        if (selectedRow != -1 && selectedColumn != -1) {
            int rightValue = this.solvedGameField.getCellValue(this.selectedRow, this.selectedColumn);

            if (this.gameField.getCellValue(selectedRow, selectedColumn) != 0) {
                SupportFunctions.showMessage("Field not empty!");

                return;
            }

            if (value != rightValue) {
                incrementAttempts();

                SupportFunctions.showMessage("Bad value!");
            } else {
                setCellValue(selectedRow, selectedColumn, value);
                this.gameField.setCellValue(selectedRow, selectedColumn, value);
            }

            if (this.gameField.fieldIsSolved()) {
                finalizeGame(true);
            }
        } else {
            SupportFunctions.showMessage("Select field cell!");
        }
    }

    private void incrementAttempts() {
        this.sudokuGameController.addAttempts();
        attemptsLabel.setText("Attempts: " + this.sudokuGameController.getAttempts() + " / "
                + this.sudokuGameController.getMaxAttempts());

        if (this.sudokuGameController.getAttempts() >= this.sudokuGameController.getMaxAttempts()) {
            finalizeGame(false);
        }
    }

    private void showHint() {
        if (this.gameOver) {
            return;
        }

        if (this.usedHintsCount >= 1) {
            SupportFunctions.showMessage("The hints are over!");
            return;
        }

        int rightValue = this.solvedGameField.getCellValue(this.selectedRow, this.selectedColumn);

        this.gameField.setCellValue(this.selectedRow, this.selectedColumn, rightValue);
        setCellValue(this.selectedRow, this.selectedColumn, rightValue);

        usedHintsCount++;
    }

    private void setDifficulty(String difficulty) {
        difficultyLabel.setText("Difficulty: " + difficulty);
    }

    public void create() {
        Logger.getInstance().info("launch");

        setDifficulty(this.difficult.toString());

        this.sudokuGameController.timerStart();

        SwingUtilities.invokeLater(() -> setVisible(true));

        this.gameField = fillGameField();
        this.solvedGameField = solveField(this.gameField);
    }

    private GameField fillGameField() {
        GameField gameField = sudokuGameController.createGameField(difficult);

        for (int row = 0; row < GameField.FIELD_SIZE; row++) {
            for (int column = 0; column < GameField.FIELD_SIZE; column++) {
                int cellValue = gameField.getCellValue(row, column);

                if (cellValue != GameField.FIELD_EMPTY_VALUE) {
                    setCellValue(row, column, cellValue);
                }
            }
        }

        return gameField;
    }

    private GameField solveField(GameField gameField) {
        GameField solvedField = null;

        try {
            solvedField = gameField.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        GameFieldSolver solver = sudokuGameController.createGameFieldSolver(solvedField);
        solver.solve();

        return solvedField;
    }

    private void finalizeGame(boolean haveWin) {
        Logger.getInstance().info("Finalize game");

        this.sudokuGameController.timerStop();

        this.gameOver = true;

        if (haveWin) {
            SupportFunctions.showMessage("You've won!");

            this.sudokuGameController.giveRewards(this.difficult, this.user);
        } else {
            SupportFunctions.showMessage("Game over!");
        }

        updateStatistic(haveWin);
        showUserState();
    }

    private void updateStatistic(boolean haveWin) {
        Statistic statistic = user.getStatistic();

        LocalDate currentDate = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate lastPlayDate = statistic
                .getLastPlayDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        long daysInGame = statistic.getDaysInGame();

        if (!lastPlayDate.equals(currentDate)) {
            daysInGame++;
            lastPlayDate = currentDate;

            statistic.setDaysInGame(daysInGame);
            statistic.setLastPlayDate(
                    Date.from(lastPlayDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        if (this.difficult.equals(GameClass.EASY)) {
            if (haveWin) {
                long easyWins = statistic.getEasyWins();
                easyWins++;
                statistic.setEasyWins(easyWins);
            } else {
                long easyLose = statistic.getEasyLose();
                easyLose++;
                statistic.setEasyLose(easyLose);
            }
        } else if (this.difficult.equals(GameClass.MIDDLE)) {
            if (haveWin) {
                long middleWins = statistic.getMiddleWins();
                middleWins++;
                statistic.setMiddleWins(middleWins);
            } else {
                long middleLose = statistic.getMiddleLose();
                middleLose++;
                statistic.setMiddleLose(middleLose);
            }
        } else if (this.difficult.equals(GameClass.HARD)) {
            if (haveWin) {
                long hardWins = statistic.getHardWins();
                hardWins++;
                statistic.setHardWins(hardWins);
            } else {
                long hardLose = statistic.getHardLose();
                hardLose++;
                statistic.setHardLose(hardLose);
            }
        }

        if (haveWin) {
            long seconds = this.sudokuGameController.getTimerSeconds();

            if (this.difficult.equals(GameClass.EASY)) {
                long bestEasySeconds = statistic.getEasyBestTime();

                if (seconds < bestEasySeconds || bestEasySeconds == 0) {
                    statistic.setEasyBestTime(seconds);
                }
            } else if (this.difficult.equals(GameClass.MIDDLE)) {
                long bestMiddleSeconds = statistic.getMiddleBestTime();

                if (seconds < bestMiddleSeconds || bestMiddleSeconds == 0) {
                    statistic.setMiddleBestTime(seconds);
                }
            } else if (this.difficult.equals(GameClass.HARD)) {
                long bestHardSeconds = statistic.getHardBestTime();

                if (seconds < bestHardSeconds || bestHardSeconds == 0) {
                    statistic.setHardBestTime(seconds);
                }
            }
        }

        gameOperationsController.updateUser(user);
    }

    private void showUserState() {
        StringBuilder builder = new StringBuilder();

        builder.append("User ");
        builder.append(user.getName());
        builder.append(":\n");

        builder.append("level - ");
        builder.append(user.getLevel());
        builder.append("\n");
        builder.append("experience - ");
        builder.append(user.getExperience());
        builder.append("\n");
        builder.append("experience to next level - ");
        builder.append(user.getExperienceToNextLevel());

        SupportFunctions.showMessage(builder.toString());
    }

    private KeyAdapter getGameKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                processAnswer(e);

                processHint(e);

                processSelectionUp(e);
                processSelectionDown(e);
                processSelectionRight(e);
                processSelectionLeft(e);

                selectFirstCell();

                processGameEnd(e);
            }
        };
    }

    private void processGameEnd(KeyEvent e) {
        if (this.gameOver) {
            boolean needNewRound = answerNeedNewRound();

            if (needNewRound) {
                Logger.getInstance().info("Continue game");

                AdditionalGameWindows.selectGameSetting(user);

                this.dispose();
            }
        }
    }

    private boolean answerNeedNewRound() {
        boolean needNewRound = false;

        Object[] options = {"Yes", "No"};

        AtomicInteger choice = new AtomicInteger();

        choice.set(JOptionPane.showOptionDialog(
                null,
                "Continue?",
                "Game continue",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]));

        needNewRound = choice.get() == 0;

        return needNewRound;
    }

    private void processHint(KeyEvent e) {
        if (selectedRow != -1 && selectedColumn != -1) {
            if (e.getKeyCode() == KeyEvent.VK_H) {
                showHint();
            }
        }
    }

    private void processAnswer(KeyEvent e) {
        if (selectedRow != -1 && selectedColumn != -1) {
            if (e.getKeyCode() == KeyEvent.VK_1) {
                onNumberButtonClick(1);
            } else if (e.getKeyCode() == KeyEvent.VK_2) {
                onNumberButtonClick(2);
            } else if (e.getKeyCode() == KeyEvent.VK_3) {
                onNumberButtonClick(3);
            } else if (e.getKeyCode() == KeyEvent.VK_4) {
                onNumberButtonClick(4);
            } else if (e.getKeyCode() == KeyEvent.VK_5) {
                onNumberButtonClick(5);
            } else if (e.getKeyCode() == KeyEvent.VK_6) {
                onNumberButtonClick(6);
            } else if (e.getKeyCode() == KeyEvent.VK_7) {
                onNumberButtonClick(7);
            } else if (e.getKeyCode() == KeyEvent.VK_8) {
                onNumberButtonClick(8);
            } else if (e.getKeyCode() == KeyEvent.VK_9) {
                onNumberButtonClick(9);
            }
        }
    }

    private void processSelectionUp(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            if (selectedRow != -1 && selectedRow != 0) {
                selectedRow--;

                updateCellHighlighting();
            }
        }
    }

    private void processSelectionDown(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            if (selectedRow != -1 && selectedRow != 8) {
                selectedRow++;

                updateCellHighlighting();
            }
        }
    }

    private void processSelectionRight(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            if (selectedColumn != -1 && selectedColumn != 8) {
                selectedColumn++;

                updateCellHighlighting();
            }
        }
    }

    private void processSelectionLeft(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            if (selectedColumn != -1 && selectedColumn != 0) {
                selectedColumn--;

                updateCellHighlighting();
            }
        }
    }

    private void selectFirstCell() {
        if (selectedRow == -1 || selectedColumn == -1) {
            selectedRow = 1;
            selectedColumn = 1;
        }
    }
}
