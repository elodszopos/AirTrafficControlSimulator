package ATCS;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: Elod-Arpad
 * Date: 11/2/13
 * Time: 12:45 PM
 */
public class GuideInQueue extends Thread{

    final Airplane airplane;
    final Airport airport;

//      state -1: initial state
//      state 0: approaching circular trajectory
//      state 1: awaiting permission, circulating around airport
//      state 2: landing
//      state 3: control released, exiting queue
//

    private int state;
    private boolean die = false;
    protected boolean headingSet = false;
    private boolean landingPermission = false;

    protected GuideInQueue(Airport airport, Airplane airplane) {
        this.airplane = airplane;
        this.airport = airport;
        this.state = -1;
    }

    protected synchronized void kill() {
        if (landingPermission) airport.releasePermission(airplane);
        die = true;
        this.notify();
    }

    protected void permitLanding(boolean permit) {
        if (permit) {
            landingPermission = true;
            System.out.println("Airplane " + airplane.getID() + " got permission to land.");
        } else {
            landingPermission = false;
            System.out.println("Airplane " + airplane.getID() + "'s landing permission has been revoked.");
        }
        airplane.permission = permit;
    }

    private Point nextPoint(int i) {
        if (state == 1)
            return new Point(airport.xArray[i], airport.yArray[i]);
        else
            return new Point(airport.xFinal[i], airport.yFinal[i]);
    }

    @Override
    public void run() {
        try {
            System.out.println("Airplane " + airplane.getID() + " is approaching.");
            state = 0;
            int movingToWhichPoint = 0;
            while ((state != 4) && (airplane != null) && !die)
                switch (state) {
                    case 0: //leading plane to holding pattern
                        if (airplane.getPlanePosition().y < airport.getCoords().y) {
                            airplane.setPlaneOrientation(2);
                            if (airplane.getPlanePosition().y < airport.getCoords().y - 20) {
                                airplane.setHeading(new Point(airport.getCoords().x + 120 - (airport.getCoords().y - airplane.getPlanePosition().y) + 20, airplane.getPlanePosition().y));
                                movingToWhichPoint = 3;
                            } else {
                                airplane.setHeading(new Point(airport.getCoords().x + 120, airplane.getPlanePosition().y));
                                movingToWhichPoint = 4;
                            }
                        } else {
                            airplane.setPlaneOrientation(6);
                            if (airplane.getPlanePosition().y > airport.getCoords().y + 20) {
                                airplane.setHeading(new Point(airport.getCoords().x - 120 + (airplane.getPlanePosition().y - airport.getCoords().y) - 20, airplane.getPlanePosition().y));
                                movingToWhichPoint = 7;
                            } else {
                                airplane.setHeading(new Point(airport.getCoords().x - 120, airplane.getPlanePosition().y));
                                movingToWhichPoint = 0;
                            }
                        }
                        headingSet = true;
                        airport.queuePlane(airplane, this);
                        state++;
                        break;
                    case 1: //plane in holding pattern, on circular trajectory above airport
                        synchronized(this.airplane) {
                            airplane.notify();
                        }

                        synchronized(this) {
                            wait();
                            //System.out.println("Sending new heading to airplane " + airplane.getID());
                            if (landingPermission &&
                                    (((movingToWhichPoint == 7) && airplane.getPlanePosition().x > airport.getCoords().x - 95)
                                            || ((movingToWhichPoint == 3) && airplane.getPlanePosition().x < airport.getCoords().x + 95))) state++;
                            airplane.setHeading(nextPoint(movingToWhichPoint));
                            airplane.setPlaneOrientation(movingToWhichPoint);
                            if (movingToWhichPoint < 7) movingToWhichPoint++;
                            else movingToWhichPoint = 0;
                            headingSet = true;
                        }
                        break;
                    case 2: //final approach
                        synchronized(this.airplane) {
                            airplane.notify();
                        }
                        synchronized(this) {
                            wait();
                            airplane.setHeading(nextPoint(movingToWhichPoint));
                            airplane.setPlaneOrientation(movingToWhichPoint);
                            if (movingToWhichPoint == 2) { movingToWhichPoint = 5; state++; }
                            if (movingToWhichPoint == 6) { movingToWhichPoint = 1; state++; }
                            if (movingToWhichPoint < 7) movingToWhichPoint++;
                            else movingToWhichPoint = 0;
                            headingSet = true;
                        }
                        break;
                    case 3: //landing
                        synchronized(this.airplane) {
                            airplane.notify();
                        }
                        synchronized(this) {
                            wait();
                            if (airplane.getPlanePosition().x > airport.getCoords().x) {
                                airplane.setHeading(airport.landingPointFromRightSide);
                                airplane.setPlaneOrientation(6);
                            } else {
                                airplane.setHeading(airport.landingPointFromLeftSide);
                                airplane.setPlaneOrientation(2);
                            }
                            headingSet = true;
                        }
                        break;


                }
        } catch (InterruptedException e) {
            System.out.println("Guide of airplane " + airplane.getID() + " caught an InterruptedException!");
        }
    }
}