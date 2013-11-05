package ATCS;

/**
 * Created with IntelliJ IDEA.
 * User: Elod-Arpad
 * Date: 11/3/13
 * Time: 3:17 PM
 */
public class AirplaneSender extends Thread {

    final Airplane airplane;
    final Airport airport;
    final String destination;
    private boolean die;

    protected AirplaneSender(Airplane airplane, Airport airport, String destination) {
        this.airplane = airplane;
        this.airport = airport;
        this.destination = destination;
    }

    protected synchronized void kill() {
        die = true;
        this.notify();
    }

    @Override
    public void run() {

        System.out.println("Airplane " + airplane.getID() + " is going to " + destination + ".");
        try {
            synchronized(this) {
                wait();
                System.out.println("Airplane " + airplane.getID() + " has been sent to " + destination + ".");
                airport.getCommunicator("").sendPlane(destination, airplane);
            }
        } catch (InterruptedException e) {
            System.err.println("Sender of airplane " + airplane.getID() + "got interrupted.");
        }
    }
}
