package ATCS;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Elod-Arpad
 * Date: 11/2/13
 * Time: 11:44 AM
 */
public class AirportCommunicator {

    private Airport airport;
    private Connection connection;
    private HashMap<String, Integer> distanceToCityMap;
    private Listener listener = null;
    private String dbHost; //localhost unless elsewhere
    private String name;
    private int port;

    protected AirportCommunicator(Airport airport, String dbHost) {
        this.airport = airport;
        this.dbHost = dbHost;
        try  {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://" + dbHost + "/ATC", "root", "");
        } catch (Exception e) {
            System.err.println("Error connecting to database.");
            //e.printStackTrace();
        }
    }

    @Override
    protected void finalize() {
        try {
            sendGoodbye();
            Statement s = connection.createStatement();
            s.executeUpdate("UPDATE PORTS SET TAKEN=0 WHERE CITY='" + this.name + "'");
            s.executeUpdate("UPDATE PORTS SET HOST='localhost' WHERE CITY='" + this.name + "'");
        } catch (Exception e) {
        } finally {
            try {
                super.finalize();
            } catch (Throwable ex) { //freakin' love to finalize stuff  }
        }
        }
    }


    private void sendGoodbye() {
        try {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PORTS WHERE TAKEN=1");
            while (rs.next()) {
                if (!rs.getString("CITY").equals(name)) {
                    Socket socketToNet = new Socket(InetAddress.getByName(rs.getString("HOST")), rs.getInt("CLIENT"));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketToNet.getOutputStream())));
                    BufferedReader in = new BufferedReader(new InputStreamReader(socketToNet.getInputStream()));
                    out.println("//Bye//~" + name);
                    out.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String[] getEverything() {
        return (String[])getEverythingAsSet().toArray(new String[0]);
    }

    private Set getEverythingAsSet() {
        ResultSet rs = null;
        Set allAirports = new HashSet();
        try {
            Statement s = connection.createStatement();
            rs = s.executeQuery("SELECT CITY FROM PORTS WHERE TAKEN=0");
            while (rs.next()) {
                allAirports.add(rs.getString("CITY"));
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to database.");
            //e.printStackTrace();
        }
        return allAirports;
    }

    protected String[] getDestinations() {
        return (String[])getDestinationsAsSet().toArray(new String[0]);
    }

    private Set getDestinationsAsSet() {
        ResultSet rs = null;
        Set allAirports = new HashSet();
        try {
            Statement s = connection.createStatement();
            rs = s.executeQuery("SELECT CITY FROM PORTS WHERE TAKEN=1");
            while (rs.next()) {
                if (!rs.getString("CITY").equals(name))
                    allAirports.add(rs.getString("CITY"));
            }
        } catch (Exception e) {
            System.err.println("Error connecting to database.");
            //e.printStackTrace();
        }
        return allAirports;
    }

    protected void sendPlane(String destName, Airplane plane) {
        try {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM PORTS WHERE CITY='" + destName + "'");
            rs.next();
            Socket socketToNet = new Socket(InetAddress.getByName(rs.getString("HOST")), rs.getInt("CLIENT"));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketToNet.getOutputStream())));
            BufferedReader in = new BufferedReader(new InputStreamReader(socketToNet.getInputStream()));
            out.println("//Airplane//~" + plane.getID() + ":" + plane.getFuel());
            out.flush();
            try {
                out.close();
                in.close();
                socketToNet.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void setIdentity() {
        try {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT CLIENT FROM PORTS WHERE CITY='" + airport.getCity() + "'");
            rs.next();
            this.name = airport.getCity();
            this.port = rs.getInt("CLIENT");
            s = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            s.executeUpdate("UPDATE PORTS SET TAKEN=1 WHERE CITY='" + this.name + "'");
            s.executeUpdate("UPDATE PORTS SET HOST='" + InetAddress.getLocalHost().getHostName() + "' WHERE CITY='" + this.name + "'");

            rs = s.executeQuery("SELECT * FROM PORTS WHERE TAKEN=1");
            while (rs.next()) {
                if (!rs.getString("CITY").equals(name)) {
                    Socket socketToNet = new Socket(InetAddress.getByName(rs.getString("HOST")), rs.getInt("CLIENT"));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketToNet.getOutputStream())));
                    BufferedReader in = new BufferedReader(new InputStreamReader(socketToNet.getInputStream()));
                    out.println("//Hello//~" + name);
                    out.flush();
                    try {
                        out.close();
                        in.close();
                        socketToNet.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void startListener() {
        if (this.listener == null) {
            this.listener = new Listener(port);
            listener.start();
        }
    }



    class Listener extends Thread {
        ServerSocket ss;
        int port;

        protected Listener(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            try {
                ss = new ServerSocket(port);
                System.out.println("Waiting for connections on port " + this.port);
                while (true) {
                    Socket socket = ss.accept();
                    (new TreatClient(socket)).start();
                }

            } catch (IOException e) {
                System.err.println("Error creating listener socket.");
              //  e.printStackTrace();
            } finally {
                try{
                    ss.close();
                }catch(Exception ex2){
                  //  ex2.printStackTrace();
                }
            }
        }
    }


    class TreatClient extends Thread {
        Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        protected TreatClient(Socket s) {
            this.socket = s;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            } catch (IOException e) {
                System.err.println("Error creating listener input reader.");
               // e.printStackTrace();
            }
        }

        @Override
        public void run() {
            System.out.println("New client connected.");
            String operation = null;
            String parameter = null;
            try {
                String command = in.readLine();
                operation = command.split("~")[0];
                parameter = command.split("~")[1];
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (operation.equals("//Hello//")) {
                System.out.println(parameter + " airport said hello.");
                MainWindow.getInstance().saidHelloActionPerformed(parameter);
            } else if (operation.equals("//Airplane//")) {
                String planeID = parameter.split(":")[0];
                int planeFuel = Integer.parseInt(parameter.split(":")[1]);
                System.out.println("Expecting airplane " + planeID);
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MainWindow.getInstance().newIncomingPlaneActionPerformed(planeID, planeFuel);
            } else if (operation.equals("//Bye//")) {
                System.out.println(parameter + " airport said bye.");
                MainWindow.getInstance().saidByeActionPerformed(parameter);
            } else System.out.println("Received unknown command: " + operation + parameter);

            try {
                out.close();
                in.close();
                socket.close();
                this.finalize();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

}


