package spaceicebreaker.gui.game;

import java.awt.Color;
import java.awt.Graphics2D;

public class Asteroid {
    double x, y, vx, vy;
    int r;

    Asteroid(int x, int y) {
        this.x = x;
        this.y = y;
        this.vx = (Math.random() - 0.5) * 2;
        this.vy = 1 + Math.random() * 2;
        this.r = 12 + (int) (Math.random() * 14);
    }

    void update() {
        x += vx;
        y += vy;
    }

    boolean hits(int px, int py) {
        return Math.hypot(x - px, y - py) < r + 12;
    }

    void draw(Graphics2D g) {
        g.setColor(new Color(120, 110, 100));
        g.fillOval((int) (x - r), (int) (y - r), r * 2, r * 2);
        g.setColor(new Color(90, 80, 70));
        g.drawOval((int) (x - r), (int) (y - r), r * 2, r * 2);
    }
}
