package ATCS;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Elod-Arpad
 * Date: 11/3/13
 * Time: 7:46 AM
 */
public class AirportSymbol extends JComponent {

    Airport airport;

    protected AirportSymbol(Airport airport) {
        this.airport = airport;
    }

    private void drawPlane(Graphics g, Point p1, Point p2, Point p3) {
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
        g.drawLine(p2.x, p2.y, p3.x, p3.y);
        g.drawLine(p3.x, p3.y, p1.x, p1.y);
    }

    private void drawPlaneUp(Graphics g, int x, int y) {
        drawPlane(g, new Point(x, y - 5), new Point(x + 5, y + 10), new Point(x - 5, y + 10));
    }

    private void drawPlaneDown(Graphics g, int x, int y) {
        drawPlane(g, new Point(x, y + 5), new Point(x + 5, y - 10), new Point(x - 5, y - 10));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int centerPositionX = this.getBounds().width/2;
        int centerPositionY = this.getBounds().height/2;


        g.drawLine(centerPositionX - 35, centerPositionY - 10, centerPositionX + 35, centerPositionY - 10);
        g.drawLine(centerPositionX - 35, centerPositionY + 10, centerPositionX + 35, centerPositionY + 10);

        g.drawLine(centerPositionX - 35, centerPositionY - 10, centerPositionX - 35, centerPositionY - 30);
        g.drawLine(centerPositionX + 35, centerPositionY - 10, centerPositionX + 35, centerPositionY - 30);
        g.drawLine(centerPositionX, centerPositionY - 10, centerPositionX, centerPositionY - 30);

        g.drawLine(centerPositionX - 35, centerPositionY + 10, centerPositionX - 35, centerPositionY + 30);
        g.drawLine(centerPositionX + 35, centerPositionY + 10, centerPositionX + 35, centerPositionY + 30);
        g.drawLine(centerPositionX, centerPositionY + 10, centerPositionX, centerPositionY + 30);

        g.setColor(Color.LIGHT_GRAY);
        g.drawArc(centerPositionX - 60, centerPositionY - 60, 120, 120, 0, 360);

        g.drawPolygon(airport.xArray, airport.yArray, airport.xArray.length); //holding pattern

        //final approach left
        g.drawLine(centerPositionX - 35, centerPositionY, centerPositionX - 70, centerPositionY);
        g.drawLine(centerPositionX - 70, centerPositionY, centerPositionX - 95, centerPositionY + 25);
        g.drawLine(centerPositionX - 95, centerPositionY + 25, centerPositionX - 95, centerPositionY + 45);

        //final approach right
        g.drawLine(centerPositionX + 35, centerPositionY, centerPositionX + 70, centerPositionY);
        g.drawLine(centerPositionX + 70, centerPositionY, centerPositionX + 95, centerPositionY - 25);
        g.drawLine(centerPositionX + 95, centerPositionY - 25, centerPositionX + 95, centerPositionY - 45);

        g.setColor(Color.black);
        switch (airport.capacity) {
            case 0:
                drawPlaneUp(g, centerPositionX - 17, centerPositionY - 23);
                drawPlaneUp(g, centerPositionX + 17, centerPositionY - 23);
                drawPlaneDown(g, centerPositionX - 17, centerPositionY + 23);
                drawPlaneDown(g, centerPositionX + 17, centerPositionY + 23);
                break;
            case 1:
                drawPlaneUp(g, centerPositionX - 17, centerPositionY - 23);
                drawPlaneUp(g, centerPositionX + 17, centerPositionY - 23);
                drawPlaneDown(g, centerPositionX - 17, centerPositionY + 23);
                break;
            case 2:
                drawPlaneUp(g, centerPositionX - 17, centerPositionY - 23);
                drawPlaneUp(g, centerPositionX + 17, centerPositionY - 23);
                break;
            case 3:
                drawPlaneUp(g, centerPositionX - 17, centerPositionY - 23);
                break;
        }

    }
}
