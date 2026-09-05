package spaceicebreaker.gui.game;

import java.awt.Color;
import java.awt.Graphics2D;

public class Particle {
    double x, y, vx, vy;
    int life = 20;
    Color color;

    Particle(double x, double y, double vx, double vy, Color c) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = c;
    }

    void update() {
        x += vx;
        y += vy;
        life--;
    }

    void draw(Graphics2D g) {
        int alpha = Math.max(0, life * 12);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
        g.fillOval((int) x - 2, (int) y - 2, 4, 4);
    }
}
