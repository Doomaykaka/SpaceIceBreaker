package spaceicebreaker.gui.game;

import java.awt.Color;
import java.awt.Graphics2D;

public class Enemy {
    double x, y, vy = 1.5;

    Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void update() {
        y += vy;
    }

    boolean hits(int px, int py) {
        return Math.hypot(x - px, y - py) < 22;
    }

    void draw(Graphics2D g) {
        int[] xpts = {(int) x, (int) x - 16, (int) x + 16};
        int[] ypts = {(int) y + 14, (int) y - 14, (int) y - 14};
        g.setColor(new Color(220, 80, 80));
        g.fillPolygon(xpts, ypts, 3);
        g.setColor(Color.PINK);
        g.drawPolygon(xpts, ypts, 3);
    }
}
