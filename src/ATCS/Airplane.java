package ATCS;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Elod-Arpad
 * Date: 11/2/13
 * Time: 11:44 AM
 */

public class Airplane extends JComponent implements Runnable, Comparable<Airplane>{

    private int fuel;
    private String ID;
    private static int count = 1;
    private Point planePosition;
    private Point heading;
    private Point startOfHeading;
    private int directionDuration;
    /*
     * planeOrientation can have values from 0 to 7, each of them indicating
     * a direction, starting clockwise, from 12:00
     */
    private int planeOrientation;
    private Airport airport;
    private int maximumX = 530;
    private int maximumY = 478;
    private int minDuration = 5;
    private int hysteresis = 10;
    private boolean orientationChanged = false;
    private boolean inDirection = false;
    private int state;
    private GuideInQueue guide;
    private AirplaneSender airplaneSender;
    private boolean waitingForGuidance;
    protected static String namePrefix = "";
    protected boolean permission = false; //for color changes

     protected static int getCount() {
        return count;
    }

      protected Airplane(int fuel, int x, int y, Airport airport) {
        this.fuel = fuel;
        this.planePosition = new Point(x, y);
        this.ID = namePrefix + count;
        this.airport = airport;
        this.directionDuration = 0;
        this.waitingForGuidance = false;
        setHeading(new Point(airport.getCoords()));

        if ((x == 0) && (y == 0)) {
            this.planeOrientation = 3;
        } else if ((x == maximumX) && (y == 0)) {
            this.planeOrientation = 5;
        } else if ((x == maximumX) && (y == maximumY)) {
            this.planeOrientation = 7;
        } else if ((x == 0) && (y == maximumY)) {
            this.planeOrientation = 1;
        } else if (x == 0) {
            this.planeOrientation = 2;
        } else if (y == 0) {
            this.planeOrientation = 4;
        } else if (x == maximumX) {
            this.planeOrientation = 6;
        } else if (y == maximumY) {
            this.planeOrientation = 0;
        } else {
            this.planeOrientation = (int) (Math.random() * 8);
            if (this.planeOrientation == 8) {
                this.planeOrientation = 7;
            }
        }
        airport.addPlane(this.ID, this);
        count++;
        state = 0;
        new Thread(this).start();
    }

    protected Airplane(int fuel, int x, int y, Airport airport, boolean leaving) {
        this(fuel, x, y, airport);
         state = -2;
    }

    protected void setSender (AirplaneSender sender) {
        this.airplaneSender = sender;
    }

    protected String getID() {
        return ID;
    }

    protected void setID(String ID) {
        this.ID = ID;
    }

    private void setState(int state) {
        this.state = state;
    }

    protected int getFuel() {
        return fuel;
    }

    protected void setFuel(int fuel) {
        this.fuel = fuel;
    }

        //how much plane can travel in given weather conditions
    protected int getRange() {
        if (airport.getWeatherCondition() == 4) {
            return 0;
        }
        if (airport.getWeatherCondition() == 0) {
            return Integer.MAX_VALUE;
        }
        return fuel / airport.getWeatherCondition();
    }

    protected synchronized Point getPlanePosition() {
        return planePosition;
    }

    protected void setPlaneOrientation(int planeOrientation) {
        if ((planeOrientation <= 7) && (planeOrientation >= 0))
            this.planeOrientation = planeOrientation;
    }

    private void drawPlane(Graphics g, Point p1, Point p2, Point p3) {
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
        g.drawLine(p2.x, p2.y, p3.x, p3.y);
        g.drawLine(p3.x, p3.y, p1.x, p1.y);
    }

    @Override
    //g == mainwindow
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (permission) g.setColor(Color.BLUE);
        else g.setColor(Color.BLACK);
        switch (planeOrientation) {
            case 0:
                drawPlane(g, new Point(planePosition.x, planePosition.y - 5), new Point(planePosition.x + 5, planePosition.y + 10), new Point(planePosition.x - 5, planePosition.y + 10));
                break;
            case 1:
                drawPlane(g, new Point(planePosition.x + 3, planePosition.y - 3), new Point(planePosition.x - 12, planePosition.y + 3), new Point(planePosition.x - 5, planePosition.y + 11));
                break;
            case 2:
                drawPlane(g, new Point(planePosition.x + 5, planePosition.y), new Point(planePosition.x - 10, planePosition.y - 5), new Point(planePosition.x - 10, planePosition.y + 5));
                break;
            case 3:
                drawPlane(g, new Point(planePosition.x + 3, planePosition.y + 3), new Point(planePosition.x - 12, planePosition.y - 3), new Point(planePosition.x - 5, planePosition.y - 11));
                break;
            case 4:
                drawPlane(g, new Point(planePosition.x, planePosition.y + 5), new Point(planePosition.x + 5, planePosition.y - 10), new Point(planePosition.x - 5, planePosition.y - 10));
                break;
            case 5:
                drawPlane(g, new Point(planePosition.x - 3, planePosition.y + 3), new Point(planePosition.x + 12, planePosition.y - 3), new Point(planePosition.x + 5, planePosition.y - 11));
                break;
            case 6:
                drawPlane(g, new Point(planePosition.x - 5, planePosition.y), new Point(planePosition.x + 10, planePosition.y - 5), new Point(planePosition.x + 10, planePosition.y + 5));
                break;
            case 7:
                drawPlane(g, new Point(planePosition.x - 3, planePosition.y - 3), new Point(planePosition.x + 12, planePosition.y + 3), new Point(planePosition.x + 5, planePosition.y + 11));
                break;
            default:
                System.out.println("Error in the airplane's orientation!");
                g.drawArc(planePosition.x, planePosition.y, 10, 10, 0, 360);
        }
        g.drawString(ID, planePosition.x, planePosition.y);
        g.drawLine(startOfHeading.x, startOfHeading.y, heading.x, heading.y);
        if (heading.distance(planePosition) > getRange()) {
            g.setColor(Color.red);
        } else {
            g.setColor(Color.green);
        }

        g.drawArc(planePosition.x - this.getRange(), planePosition.y - this.getRange(), this.getRange() * 2, this.getRange() * 2, 0, 360);
    }

    public void run() {
        try {
            System.out.println("Airplane " + ID + " appeared.");
            state++;
            while ((planePosition.x >= 0) && (planePosition.x <= maximumX)
                    && (planePosition.y >= 0) && (planePosition.y <= maximumY)
                    && (fuel > 0) && (state < 4)) {
                this.repaint();

                this.directionDuration++;
                if (orientationChanged) {
                    this.directionDuration = 0;
                    orientationChanged = false;
                }

                moveToNextPoint();

                switch (state) {
                    case -1: //headed for another airport
                        if (planePosition.distance(heading) <= 1) {
                            synchronized(this.airplaneSender) {
                                airplaneSender.notify();
                            }
                        } //airplane dies after this
                        break;

                    case 1: //plane appears + approaching the airport
                        if (heading.equals(airport.getCoords()) && (heading.distance(planePosition) < 60)) {
                            guide = airport.requestToQueue(this);
                            if (guide != null) {
                                state++;
                                this.waitingForGuidance = true;
                            }
                        }

                        if (directionDuration > minDuration) {
                            changeOrientation();
                        }
                        break;

                    case 2: //in holding pattern with continuous guidance of the airport
                        if (planePosition.distance(heading) < 1) {
                            waitingForGuidance = true;
                            synchronized(this.guide) {
                                guide.notify();
                            }
                        }
                        if (waitingForGuidance) {
                            synchronized(this) {
                                while (!guide.headingSet)
                                    wait();
                                if ((this.heading.equals(airport.landingPointFromLeftSide)) || (this.heading.equals(airport.landingPointFromRightSide))) state++;
                                guide.headingSet = false;
                                waitingForGuidance = false;
                            }
                        }

                        break;

                    case 3: //landing
                        if (planePosition.distance(heading) < 1) {
                            state++;
                            airport.park(this);
                        }
                        break;
                }

                generateDefect();

                if (airport.getWeatherCondition() < 4) {
                    fuel = fuel - airport.getWeatherCondition();
                } else {
                    fuel = 0;
                }

                Thread.sleep(airport.getSimulationSpeed());

            }
        } catch (InterruptedException e) {
            System.out.println("Airplane " + ID + " caught an InterruptedException!");
        } finally {
            airport.removePlane(ID);
            if (guide != null) guide.kill();
            if (state == 4)
                System.out.println("Airplane " + ID + " landed.");
            else
                System.out.println("Airplane " + ID + " disappeared.");
            this.setVisible(false);

        }
    }

    private boolean movesDown() {
        if ((planeOrientation <= 5) && (planeOrientation >= 3)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean movesUp() {
        if ((planeOrientation == 0) || (planeOrientation == 1) || (planeOrientation == 7)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean movesRight() {
        if ((planeOrientation <= 3) && (planeOrientation >= 1)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean movesLeft() {
        if ((planeOrientation <= 7) && (planeOrientation >= 5)) {
            return true;
        } else {
            return false;
        }
    }

    private void increaseOrientation() {
        if (planeOrientation == 7) {
            planeOrientation = 0;
        } else {
            planeOrientation++;
        }
        orientationChanged = true;
    }

    private void decreaseOrientation() {
        if (planeOrientation == 0) {
            planeOrientation = 7;
        } else {
            planeOrientation--;
        }
        orientationChanged = true;
    }

    private void changeOrientation() {

        if (!inDirection) {
            if ((planePosition.x < heading.x)
                    && (planePosition.y < heading.y)
                    && !(movesRight() && movesDown())) {
                if ((planeOrientation > 6) || (planeOrientation < 3)) {
                    increaseOrientation();
                } else {
                    decreaseOrientation();
                }
                return;
            }

            if ((planePosition.x < heading.x)
                    && (planePosition.y > heading.y)
                    && !(movesRight() && movesUp())) {
                if ((planeOrientation > 5) || (planeOrientation < 2)) {
                    increaseOrientation();
                } else {
                    decreaseOrientation();
                }
                return;
            }

            if ((planePosition.x > heading.x)
                    && (planePosition.y < heading.y)
                    && !(movesLeft() && movesDown())) {
                if ((planeOrientation > 1) && (planeOrientation < 5)) {
                    increaseOrientation();
                } else {
                    decreaseOrientation();
                }
                return;
            }

            if ((planePosition.x > heading.x)
                    && (planePosition.y > heading.y)
                    && !(movesLeft() && movesUp())) {
                if (planeOrientation > 3) {
                    increaseOrientation();
                } else {
                    decreaseOrientation();
                }
                return;
            }
        }

        if (!inDirection) {
            inDirection = true;
            startOfHeading = new Point(planePosition);
        }

        if (inDirection) {
            if ((lineToMoveOn(planePosition.x) > (planePosition.y + hysteresis))
                    && (planePosition.x > heading.x - hysteresis)) {
                decreaseOrientation();
            }

            if ((lineToMoveOn(planePosition.x) > (planePosition.y + hysteresis))
                    && (planePosition.x < heading.x + hysteresis)) {
                increaseOrientation();
            }

            if ((lineToMoveOn(planePosition.x) < (planePosition.y - hysteresis))
                    && (planePosition.x < heading.x + hysteresis)) {
                decreaseOrientation();
            }

            if ((lineToMoveOn(planePosition.x) < (planePosition.y - hysteresis))
                    && (planePosition.x > heading.x - hysteresis)) {
                increaseOrientation();
            }
        }
    }

    protected boolean setHeading(Point p) {
        this.heading = new Point(p);
        this.startOfHeading = new Point(this.planePosition);
        inDirection = false;
        if (heading.distance(planePosition) > this.getRange()) {
            System.out.println("Airplane " + ID + " received an UNREACHABLE heading.");
            return false;
        }
        if (state == 1) System.out.println("Airplane " + ID + " received a new heading.");
        return true;
    }

    private int lineToMoveOn(int xparam) {
        if (heading.x != startOfHeading.x) {
            //equation of line going through the destination point and the starting point
            double m = (double) (heading.y - startOfHeading.y) / (double) (heading.x - startOfHeading.x);
            return (int) (startOfHeading.y + m * (xparam - startOfHeading.x));
        } else {
            return 0;
        }
    }

    private synchronized void moveToNextPoint() {
        switch (planeOrientation) {
            case 0:
                planePosition.y--;
                break;
            case 1:
                planePosition.y--;
                planePosition.x++;
                break;
            case 2:
                planePosition.x++;
                break;
            case 3:
                planePosition.x++;
                planePosition.y++;
                break;
            case 4:
                planePosition.y++;
                break;
            case 5:
                planePosition.x--;
                planePosition.y++;
                break;
            case 6:
                planePosition.x--;
                break;
            case 7:
                planePosition.x--;
                planePosition.y--;
                break;
            default:
                System.out.println("Error in the airplane's orientation!");
        }
    }

    private void generateDefect() {
        if (Math.random() * 10000 < airport.getProbabilityForDefect()) {
            //decrease fuel by 10 - 30 %
            int oldfuel = fuel;
            fuel = fuel - (int) (((Math.random() * 20 + 10) / 100) * fuel);
            System.out.println("Airplane " + ID + " suffered a defect. Fuel lost: " + (oldfuel - fuel) + " out of " + oldfuel + ".");
        }
    }

    public int compareTo(Airplane o) {
        if(this.fuel > o.fuel)
            return +1;
        else if (this.fuel == o.fuel)
            return 0;
        return -1;
    }
}
