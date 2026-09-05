package spaceicebreaker.gui.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;
import spaceicebreaker.controllers.GameController;
import spaceicebreaker.controllers.GameOperationsController;
import spaceicebreaker.models.GameClass;
import spaceicebreaker.models.Statistic;
import spaceicebreaker.models.User;

public class GamePanel extends JPanel implements ActionListener {
    private GameWindow window;
    private User user;
    private GameClass characterClass;
    private GameController gameController;
    private GameOperationsController gameOperationsController;

    private boolean gameOver = false;
    private long playerScore = 0;

    private int playerX, playerY;
    private boolean left, right, up, down, shooting;
    private int shootCooldown = 0;
    private int spawnTick = 0;

    private Timer windowTimer;

    private final java.util.List<Asteroid> asteroids = new ArrayList<>();
    private final java.util.List<Enemy> enemies = new ArrayList<>();
    private final java.util.List<Bullet> bullets = new ArrayList<>();
    private final java.util.List<Bullet> enemyBullets = new ArrayList<>();
    private final java.util.List<Particle> particles = new ArrayList<>();

    private final Random rnd = new Random();

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int TIMER_DELAY_FOR_60_FPS = 16;

    private static final int ASTEROID_SCORE_COAST = 2;
    private static final int ENEMY_SCORE_COAST = 10;

    public GamePanel(
            GameWindow window,
            User user,
            GameClass characterClass,
            GameController gameController,
            GameOperationsController gameOperationsController) {
        this.window = window;
        this.user = user;
        this.characterClass = characterClass;
        this.gameController = gameController;
        this.gameOperationsController = gameOperationsController;

        initUI();
        setupControls();
    }

    private void initUI() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        restart();

        windowTimer = new Timer(TIMER_DELAY_FOR_60_FPS, this);
        windowTimer.start();

        setDoubleBuffered(true);
    }

    private void setupControls() {
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        left = true;
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        right = true;
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        up = true;
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        down = true;
                        break;
                    case KeyEvent.VK_SPACE:
                        shooting = true;
                    case KeyEvent.VK_R: {
                        if (gameOver) endGame();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A:
                        left = false;
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D:
                        right = false;
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W:
                        up = false;
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S:
                        down = false;
                        break;
                    case KeyEvent.VK_SPACE:
                        shooting = false;
                        break;
                }
            }
        });
    }

    private void endGame() {
        this.gameController.giveRewards(playerScore, user);

        Statistic userStatistic = user.getStatistic();

        Date lastPlayDate = userStatistic.getLastPlayDate();
        Date currentDate = Date.from(Instant.now());

        userStatistic.setLastPlayDate(currentDate);

        Calendar lastPlayCalendar = Calendar.getInstance();
        Calendar currentCalendar = Calendar.getInstance();
        lastPlayCalendar.setTime(lastPlayDate);
        currentCalendar.setTime(currentDate);

        boolean datesEquals = lastPlayCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                && lastPlayCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                && lastPlayCalendar.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH);

        if (!datesEquals) {
            Long daysInGame = userStatistic.getDaysInGame();
            daysInGame++;
            userStatistic.setDaysInGame(daysInGame);
        }

        if (characterClass == GameClass.SCOUT && playerScore > userStatistic.getScoutBestScore()) {
            userStatistic.setScoutBestScore(playerScore);
        }

        if (characterClass == GameClass.TANK && playerScore > userStatistic.getTankBestScore()) {
            userStatistic.setTankBestScore(playerScore);
        }

        if (characterClass == GameClass.DAMAGE_DEALER && playerScore > userStatistic.getDamageDealerBestScore()) {
            userStatistic.setDamageDealerBestScore(playerScore);
        }

        this.gameOperationsController.updateUser(user);

        restart();
    }

    private void restart() {
        gameOver = false;
        asteroids.clear();
        enemies.clear();
        bullets.clear();
        enemyBullets.clear();
        particles.clear();

        playerX = WIDTH / 2;
        playerY = HEIGHT - 80;

        playerScore = 0;

        gameController.setLivesCount(user.getLevel());
        gameController.setMaxLivesCount(user.getLevel());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawStars(g2);

        if (!gameOver) drawPlayer(g2);

        for (Asteroid a : asteroids) a.draw(g2);

        for (Enemy e : enemies) e.draw(g2);

        g2.setColor(new Color(80, 200, 255));
        for (Bullet b : bullets) b.draw(g2);

        g2.setColor(new Color(255, 100, 80));
        for (Bullet b : enemyBullets) b.draw(g2);

        for (Particle p : particles) p.draw(g2);

        drawHUD(g2);

        if (gameOver) drawGameOver(g2);
    }

    private void drawStars(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        for (int i = 0; i < 60; i++) {
            int sx = rnd.nextInt(WIDTH);
            int sy = rnd.nextInt(HEIGHT);
            g.fillRect(sx, sy, 2, 2);
        }
    }

    private void drawPlayer(Graphics2D g) {
        switch (characterClass) {
            case GameClass.SCOUT:
                drawScoutPlayer(g);
                break;
            case GameClass.TANK:
                drawTankPlayer(g);
                break;
            case GameClass.DAMAGE_DEALER:
                drawDamageDealerPlayer(g);
                break;
        }
    }

    private void drawScoutPlayer(Graphics2D g) {
        int[] x = {playerX, playerX - 14, playerX - 7, playerX + 7, playerX + 14};
        int[] y = {playerY - 20, playerY + 12, playerY + 6, playerY + 6, playerY + 12};
        g.setColor(new Color(100, 200, 255));
        g.fillPolygon(x, y, 5);
        g.setColor(Color.CYAN);
        g.drawPolygon(x, y, 5);

        g.setColor(Color.ORANGE);
        g.fillOval(playerX - 4, playerY + 6, 8, 10);
    }

    private void drawTankPlayer(Graphics2D g) {
        int[] tankX = {playerX - 18, playerX, playerX + 18, playerX + 12, playerX - 12};
        int[] tankY = {playerY - 10, playerY - 24, playerY - 10, playerY + 14, playerY + 14};

        g.setColor(new Color(80, 80, 100));
        g.fillPolygon(tankX, tankY, 5);
        g.setColor(Color.GRAY);
        g.drawPolygon(tankX, tankY, 5);

        g.setColor(Color.ORANGE);
        g.fillRect(playerX - 4, playerY - 6, 8, 12);

        g.setColor(new Color(100, 100, 130));
        g.fillOval(playerX - 20, playerY + 4, 12, 8);
        g.fillOval(playerX + 8, playerY + 4, 12, 8);
    }

    private void drawDamageDealerPlayer(Graphics2D g) {
        int[] dmgX = {playerX - 8, playerX, playerX + 8, playerX + 4, playerX - 4};
        int[] dmgY = {playerY - 16, playerY - 26, playerY - 16, playerY + 10, playerY + 10};

        g.setColor(new Color(200, 50, 50));
        g.fillPolygon(dmgX, dmgY, 5);
        g.setColor(Color.RED);
        g.drawPolygon(dmgX, dmgY, 5);

        g.setColor(Color.YELLOW);
        g.fillOval(playerX - 14, playerY - 4, 6, 10);
        g.fillOval(playerX + 8, playerY - 4, 6, 10);

        g.setColor(Color.MAGENTA);
        g.fillOval(playerX - 3, playerY + 2, 6, 6);
    }

    private void drawHUD(Graphics2D g) {
        g.setFont(new Font("Consolas", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("HP: " + gameController.getLivesCount(), 16, 30);
        g.drawString("Score: " + playerScore, 16, 80);
        g.drawString("Player level: " + user.getLevel(), 16, 100);
        g.drawString("Current XP: " + user.getExperience(), 16, 120);
        g.drawString("XP to next level: " + user.getExperienceToNextLevel(), 16, 140);

        drawHealthBar(g);
    }

    private void drawHealthBar(Graphics2D g) {
        int barW = 200;
        int barH = 14;
        int barX = 16;
        int barY = 38;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barW, barH);
        float ratio = Math.max(0f, (float) gameController.getLivesCount() / gameController.getMaxLivesCount());
        ratio = Math.min(ratio, 1f);
        g.setColor(ratio > 0.5f ? Color.GREEN : ratio > 0.25f ? Color.ORANGE : Color.RED);
        g.fillRect(barX, barY, (int) (barW * ratio), barH);
        g.setColor(Color.GRAY);
        g.drawRect(barX, barY, barW, barH);
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font("Consolas", Font.BOLD, 48));
        g.setColor(Color.RED);
        g.drawString("GAME OVER", WIDTH / 2 - 130, HEIGHT / 2 - 10);
        g.setFont(new Font("Consolas", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("Press R to restart", WIDTH / 2 - 110, HEIGHT / 2 + 30);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            repaint();
            return;
        }

        playerMove();

        playerShoot();

        objectsSpawn();

        asteroidsUpdate();

        enemiesUpdate();

        userBulletsUpdate();

        enemiesBulletsUpdate();

        effectsUpdate();

        if (gameController.getLivesCount() <= 0) gameOver = true;

        repaint();
    }

    private void playerMove() {
        int speed = 0;

        switch (characterClass) {
            case GameClass.SCOUT:
                speed = 8;
                break;
            case GameClass.TANK:
                speed = 6;
                break;
            case GameClass.DAMAGE_DEALER:
                speed = 7;
                break;
        }

        if (left) playerX -= speed;
        if (right) playerX += speed;
        if (up) playerY -= speed;
        if (down) playerY += speed;
        playerX = Math.max(15, Math.min(WIDTH - 15, playerX));
        playerY = Math.max(25, Math.min(HEIGHT - 15, playerY));
    }

    private void playerShoot() {
        if (shooting && shootCooldown <= 0) {
            switch (characterClass) {
                case GameClass.SCOUT:
                    bullets.add(new Bullet(playerX, playerY - 50, 0, -8, true));
                    bullets.add(new Bullet(playerX, playerY - 30, 0, -8, true));
                    bullets.add(new Bullet(playerX, playerY - 10, 0, -8, true));
                    shootCooldown = 8;
                    break;
                case GameClass.TANK:
                    bullets.add(new Bullet(playerX - 20, playerY - 20, 0, -8, true));
                    bullets.add(new Bullet(playerX, playerY - 20, 0, -8, true));
                    bullets.add(new Bullet(playerX + 20, playerY - 20, 0, -8, true));
                    shootCooldown = 24;
                    break;
                case GameClass.DAMAGE_DEALER:
                    bullets.add(new Bullet(playerX, playerY - 20, -1, -8, true));
                    bullets.add(new Bullet(playerX, playerY - 20, 0, -8, true));
                    bullets.add(new Bullet(playerX, playerY - 20, 1, -8, true));
                    shootCooldown = 16;
                    break;
            }
        }
        if (shootCooldown > 0) shootCooldown--;
    }

    private void objectsSpawn() {
        spawnTick++;
        if (spawnTick % 40 == 0) {
            asteroids.add(new Asteroid(rnd.nextInt(WIDTH), -30));
        }
        if (spawnTick % 90 == 0) {
            enemies.add(new Enemy(rnd.nextInt(WIDTH - 40) + 20, -30));
        }
    }

    private void asteroidsUpdate() {
        Iterator<Asteroid> aIt = asteroids.iterator();
        while (aIt.hasNext()) {
            Asteroid a = aIt.next();
            a.update();
            if (a.y > HEIGHT + 40) {
                try {
                    aIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }

                continue;
            }
            if (a.hits(playerX, playerY)) {
                gameController.subLives();
                spawnExplosion(a.x, a.y, Color.GRAY);

                try {
                    aIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }
            }
        }
    }

    private void enemiesUpdate() {
        Iterator<Enemy> eIt = enemies.iterator();
        while (eIt.hasNext()) {
            Enemy en = eIt.next();
            en.update();
            if (en.y > HEIGHT + 40) {
                try {
                    eIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }

                continue;
            }
            if (en.hits(playerX, playerY)) {
                gameController.subLives();
                spawnExplosion(en.x, en.y, Color.RED);

                try {
                    eIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }

                continue;
            }

            if (rnd.nextInt(60) == 0) {
                enemyBullets.add(
                        new Bullet(en.x, en.y + 12, (playerX - en.x) * 0.05, (playerY - en.y) * 0.05 + 3, false));
            }
        }
    }

    private void userBulletsUpdate() {
        Iterator<Bullet> bIt = bullets.iterator();
        while (bIt.hasNext()) {
            Bullet b = bIt.next();
            b.update();
            if (b.y < -10 || b.y > HEIGHT + 10 || b.x < -10 || b.x > WIDTH + 10) {
                try {
                    bIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }

                continue;
            }

            userBulletsOnAsteroidUpdate(b, bIt);

            userBulletsOnEnemiesUpdate(b, bIt);
        }
    }

    private void userBulletsOnAsteroidUpdate(Bullet b, Iterator<Bullet> bIt) {
        for (Iterator<Asteroid> it = asteroids.iterator(); it.hasNext(); ) {
            Asteroid a = it.next();
            if (b.hits(a.x, a.y, a.r)) {
                spawnExplosion(a.x, a.y, Color.GRAY);

                try {
                    it.remove();
                    bIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }

                playerScore += ASTEROID_SCORE_COAST;

                break;
            }
        }
    }

    private void userBulletsOnEnemiesUpdate(Bullet b, Iterator<Bullet> bIt) {
        for (Iterator<Enemy> it = enemies.iterator(); it.hasNext(); ) {
            Enemy en = it.next();
            if (b.hits(en.x, en.y, 16)) {
                spawnExplosion(en.x, en.y, Color.RED);

                try {
                    it.remove();
                    bIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }

                playerScore += ENEMY_SCORE_COAST;

                break;
            }
        }
    }

    private void enemiesBulletsUpdate() {
        Iterator<Bullet> ebIt = enemyBullets.iterator();
        while (ebIt.hasNext()) {
            Bullet b = ebIt.next();
            b.update();
            if (b.y < -10 || b.y > HEIGHT + 10 || b.x < -10 || b.x > WIDTH + 10) {
                try {
                    ebIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }
                continue;
            }
            if (b.hits(playerX, playerY, 14)) {
                gameController.subLives();

                try {
                    ebIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }
            }
        }
    }

    private void effectsUpdate() {
        Iterator<Particle> pIt = particles.iterator();
        while (pIt.hasNext()) {
            Particle p = pIt.next();
            p.update();
            if (p.life <= 0)
                try {
                    pIt.remove();
                } catch (IllegalStateException e) {
                    ;
                }
        }
    }

    private void spawnExplosion(double x, double y, Color c) {
        for (int i = 0; i < 12; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2;
            double sp = 1 + rnd.nextDouble() * 3;
            particles.add(new Particle(x, y, Math.cos(ang) * sp, Math.sin(ang) * sp, c));
        }
    }
}
