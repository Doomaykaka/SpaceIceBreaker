package spaceicebreaker.gui.game;

import java.awt.Graphics2D;

public class Bullet {
    double x, y, vx, vy;
    boolean fromPlayer;

    Bullet(double x, double y, double vx, double vy, boolean fromPlayer) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.fromPlayer = fromPlayer;
    }

    void update() {
        x += vx;
        y += vy;
    }

    boolean hits(double tx, double ty, double r) {
        return Math.hypot(x - tx, y - ty) < r + 4;
    }

    void draw(Graphics2D g) {
        g.fillOval((int) x - 3, (int) y - 3, 6, 6);
    }
}
