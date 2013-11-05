package ATCS;

/**
 * Created with IntelliJ IDEA.
 * User: Elod-Arpad
 * Date: 11/2/13
 * Time: 11:39 AM
 */

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class Airport {

    protected int capacity = 4;
    private AirportSymbol symbol;


    private int simulationSpeed = 60;
    private int probabilityForDefect = 10;
    private int fuelForArrivingPlanes = 2000;
    private int fuelForDepartingPlanes = 1000;
    private int weatherCondition = 1;

    private int maximumX = 530;
    private int maximumY = 478;

    private Point coords = new Point(maximumX/2, maximumY/2);

    protected final int[] xArray =
            {coords.x - 120, coords.x - 80, coords.x + 80, coords.x + 120, coords.x + 120, coords.x + 80, coords.x - 80, coords.x - 120};
    protected final int[] yArray =
            {coords.y - 20, coords.y - 60, coords.y - 60, coords.y - 20, coords.y + 20, coords.y + 60, coords.y + 60, coords.y + 20};

    protected final int[] xFinal = {coords.x - 95, coords.x - 70, coords.x - 35, coords.x + 95, coords.x + 95, coords.x + 70, coords.x + 35, coords.x - 95};
    protected final int[] yFinal = {coords.y + 25, coords.y, coords.y, coords.y - 45, coords.y - 25, coords.y, coords.y, coords.y + 45};

    protected final Point landingPointFromLeftSide = new Point(coords.x + 30, coords.y);
    protected final Point landingPointFromRightSide = new Point(coords.x - 30, coords.y);

    protected HashMap<String, Airplane> planes = new HashMap();
    private HashMap<Airplane, GuideInQueue> queueMap = new HashMap();
    private PriorityBlockingQueue<Airplane> queue = new PriorityBlockingQueue();
    private AirportCommunicator communicator = null;
    private String city = "";

    protected String getCity() {
        return city;
    }

    private Airport() {}

    private static class SingletonHolder {
        private static final Airport INSTANCE = new Airport();
    }

    protected static Airport getInstance() {
        return SingletonHolder.INSTANCE;
    }

    protected Point getCoords() {
        return coords;
    }

    protected synchronized int getWeatherCondition() {
        return weatherCondition;
    }

    protected synchronized void setWeatherCondition(int weatherCondition) {
        this.weatherCondition = weatherCondition;
    }

    protected synchronized int getSimulationSpeed() {
        return simulationSpeed;
    }

    protected synchronized void setSimulationSpeed(int simulationSpeed) {
        this.simulationSpeed = simulationSpeed;
    }

    protected synchronized int getProbabilityForDefect() {
        return probabilityForDefect;
    }

    protected synchronized void setProbabilityForDefect(int probabilityForDefect) {
        this.probabilityForDefect = probabilityForDefect;
    }

    protected synchronized int getFuelForArrivingPlanes() {
        return fuelForArrivingPlanes;
    }

    protected synchronized void setFuelForArrivingPlanes(int fuelForArrivingPlanes) {
        this.fuelForArrivingPlanes = fuelForArrivingPlanes;
    }

    protected synchronized int getFuelForDepartingPlanes() {
        return fuelForDepartingPlanes;
    }

    protected synchronized void setFuelForDepartingPlanes(int fuelForDepartingPlanes) {
        this.fuelForDepartingPlanes = fuelForDepartingPlanes;
    }

    protected AirportCommunicator getCommunicator(String dbHost) {
        if (this.communicator == null) {
            this.communicator = new AirportCommunicator(this, dbHost);
        }
        return communicator;
    }

    protected boolean communicatorIsNull() {
        return (communicator == null);
    }

    protected void setupNewNet() {
        communicator.setIdentity();
        communicator.startListener();
    }

    protected void setCity(String city) {
        this.city = city;
        Collection c = planes.values();
        Iterator itr = c.iterator();
        Airplane.namePrefix = city.substring(0, 4) + "_";
        while(itr.hasNext())
        {
            Airplane nextPlane = (Airplane)itr.next();
            nextPlane.setID(city.substring(0, 4) + "_" + nextPlane.getID());
        }
    }

    public void setSymbol(AirportSymbol symbol) {
        this.symbol = symbol;
    }

    protected void addPlane(String ID, Airplane plane) {
        planes.put(ID, plane);
    }

    protected void removePlane(String ID) {
        planes.remove(ID);
    }

    protected synchronized void queuePlane(Airplane plane, GuideInQueue guide) {
        Airplane oldTopPlane = null;
        queueMap.put(plane, guide);
        if (queue.peek() != null) oldTopPlane = queue.peek();
        queue.add(plane);
        if ((queue.peek() == plane) && (capacity > 0)) {
            if (oldTopPlane != null) queueMap.get(oldTopPlane).permitLanding(false);
            queueMap.get(plane).permitLanding(true);

        }
    }
    private synchronized void unQueuePlane(Airplane plane) {
        queueMap.remove(plane);
    }

    protected void park(Airplane plane) {
        if (capacity > 0) capacity--;
        symbol.repaint();
        unQueuePlane(plane);
        queue.remove(plane);
        if ((capacity > 0) && (queueMap.get(queue.peek()) != null))  queueMap.get(queue.peek()).permitLanding(true);
    }

    protected void sendPlane(Airplane plane) {
        plane.setPlaneOrientation(2);
        plane.setHeading(new Point(maximumY, coords.y));
        if (capacity < 4) capacity++;
        if ((capacity > 0 && (queueMap.get(queue.peek()) != null))) queueMap.get(queue.peek()).permitLanding(true);
        symbol.repaint();
    }

    protected void releasePermission(Airplane plane) {
        unQueuePlane(plane);
        queue.remove(plane);
        if ((capacity > 0 && (queueMap.get(queue.peek()) != null))) queueMap.get(queue.peek()).permitLanding(true);
    }

    protected Airplane newPlaneRight() {
        int relativePosition = (int)(Math.random() * 100) - 50;
        if ((relativePosition  % 2) == 0)
            return new Airplane(fuelForArrivingPlanes, maximumX, (int)(maximumY * 1/4) + relativePosition, this);
        else
            return new Airplane(fuelForArrivingPlanes, maximumX, (int)(maximumY * 3/4) + relativePosition, this);
    }

    protected Airplane newPlaneLeft() {
        int relativePosition = (int)(Math.random() * 100) - 50;
        if ((relativePosition  % 2) == 0)
            return new Airplane(fuelForArrivingPlanes, 0, (int)(maximumY * 1/4) + relativePosition, this);
        else
            return new Airplane(fuelForArrivingPlanes, 0, (int)(maximumY * 3/4) + relativePosition, this);
    }

    protected Airplane newPlaneUp() {
        int relativePosition = (int)(Math.random() * 100) - 50;
        if ((relativePosition  % 2) == 0)
            return new Airplane(fuelForArrivingPlanes, (int)(maximumX * 1/4) + relativePosition, 0, this);
        else
            return new Airplane(fuelForArrivingPlanes, (int)(maximumX * 3/4) + relativePosition, 0, this);
    }

    protected Airplane newPlaneDown() {
        int relativePosition = (int)(Math.random() * 100) - 50;
        if ((relativePosition  % 2) == 0)
            return new Airplane(fuelForArrivingPlanes, (int)(maximumX * 1/4) + relativePosition, maximumY, this);
        else
            return new Airplane(fuelForArrivingPlanes, (int)(maximumX * 3/4) + relativePosition, maximumY, this);
    }

    protected Airplane newLeavingPlane() {
        Airplane newPlane = new Airplane(fuelForDepartingPlanes, coords.x, coords.y, this);
        return newPlane;
    }

    protected Airplane newLeavingPlane(String destinationPort) {
        Airplane newPlane = new Airplane(fuelForDepartingPlanes, coords.x, coords.y, this, true);
        AirplaneSender sender = new AirplaneSender(newPlane, this, destinationPort);
        sender.start();
        newPlane.setSender(sender);
        return newPlane;
    }

    protected Airplane newIncomingPlane(String ID, int fuel) {
        Airplane newPlane = newPlane();
        newPlane.setID(ID);
        newPlane.setFuel(fuel);
        return newPlane;
    }

    protected Airplane newPlane() {
        int fromWhere = (int)(Math.random() * 4);
        if (fromWhere == 4) fromWhere = 3;
        switch (fromWhere) {
            case 0: return newPlaneUp();
            case 1: return newPlaneRight();
            case 2: return newPlaneDown();
            case 3: return newPlaneLeft();
            default: return newPlaneUp();
        }
    }



    protected GuideInQueue requestToQueue(Airplane plane) {
        if (plane.getRange() < 70) {
            System.out.println("Airplane " + plane.getID() + " doesn't have enough fuel to perform a proper landing and it will be destroyed.");
            return null;
        }

        if (weatherCondition >= 3) return null;
        GuideInQueue guide = new GuideInQueue(this, plane);
        guide.start();
        return guide;
    }

}
