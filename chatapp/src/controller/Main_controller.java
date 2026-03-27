/*
Main controller script to allow the Gui.java script and the ClientServer.java script to communicate between each other.
This allows processing to be done outside of the GUI, and to be able to monitor and scale data-flow easily.
This script is the 'Controller' part of MVC architecture.
The main method is housed within this script.

BY:
AMIN SHEIKH
KIPP SUMMERS
HUGO PIPER
DHARI ALTHUNAYAN
JAHID EMON
*/

package controller;

//All imports
import client_server_model.*;
//GUI doesnt need importing as we've used its full package name every time
import javax.swing.SwingUtilities;  //Used to show the error pop-up GUI at a safe point in time if an error occurs.

class Gui implements Runnable {
    //Starts the GUI.java script as a thread.

    public void run() {
        System.out.println("Starting GUI...");
        gui.Gui.main(null);   // start GUI
    }

}

class JoinServer implements Runnable {
    //Responsible for handling everything related to the user joining a server.

    private String serverIp;
    private Integer serverPort;
    private String userUsername;

    public JoinServer(String serverIp, Integer serverPort, String userUsername) {
        //Constructor method for JoinServer class. Used to create objects with the parameters below.
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.userUsername = userUsername;
    }

    public void run() {
        //Responsible for telling the ClientServer.java script that the user wants to join a server.
        //Creates a new ClientServer object named 'cs' and passes the parameter 'join'.
        System.out.println("[CONTROLLER] Starting the client/server script");
        try {
            ClientServer cs = new ClientServer();
            cs.start("join", serverIp, serverPort, userUsername );
        } catch (Exception e) {
            System.err.println("[CONTROLLER] Failed to launch ClientServer.java script.");
            e.printStackTrace(); //Prints what went wrong with the ClientServer object, used to debug.
        }
    }

    static void send_server_details(String serverIP, Integer serverPort, String userUsername){
        //Used to send the IP, USERNAME & PORT from the GUI to the server code so the user can connect to a server of their choice.
        //This method is called from the Gui.java script which passes the user inputted parameters. A new JoinServer object & thread is made. 
        JoinServer server = new JoinServer(serverIP, serverPort, userUsername);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}

class CreateServer implements Runnable {
    //Handles everything related to the user wanting to create a server.
    private Integer serverPort = 7000;

    public CreateServer(Integer serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        //Creates a new ClientServer object with the parameter 'create' to indicate the user wants to create a server.
        //The ClientServer class is found in ClientServer.jaava script.
        //Also provides the server port the user chose. If none chosen; defaults to 7000.
        System.out.println("[CONTROLLER] Starting server on port " + serverPort + "...");
        try {
            ClientServer cs = new ClientServer();
            cs.start("create", null, serverPort, "Host");
        } catch (Exception e) {
            System.err.println("[CONTROLLER] Failed to launch server.");
            e.printStackTrace();
        }
    }

    static void create_new_server(Integer serverPort) {
        //Creates a new object & thread to run the server on. 
        CreateServer newServer = new CreateServer(serverPort);
        Thread newServerThread = new Thread(newServer);
        newServerThread.start();
    }
}

class DisplayErrorMessage {
    //Handles error messages that arise from the ClientServer.java script and directs them to the GUI.

    public static void displayError(String error_message){
        //Displays error messsages from ClientServer.java script to the Gui.java script.
        gui.Gui guiInstance = gui.Gui.getInstance();
        if (guiInstance != null) {
            guiInstance.showErrorPopup(error_message);
            
        }
    }
}

public class Main_controller {
    /*
    Main class thats responsible for handling data transaction between the ClientServer.java and Gui.java script.
    Compliant with MVC (Model View Controller) architecture.
    */

    private static String message;
    private static boolean messageAvailable = false;
    private static final Object messageLock = new Object();

    public static void joinServerButtonPressed(String serverIP, Integer serverPort, String userUsername ){
        //This method is called from the Gui.java script when the user presses 'Join Server'.
        //The clients details are then processed and sent to the ClientServer.java script to handle networking & socket connections.
        System.out.println("[CONTROLLER] User pressed the 'join' button named 'proceedBtn' with the following attributes:\nSERVER IP: " + serverIP + "\nSERVER PORT: " + serverPort + "\nUSERNAME: " + userUsername);

        if (serverPort < 1 || serverPort > 65535){
            errorOccured("Server Port must be between 1 and 65535. Please try again.");
        }

        JoinServer.send_server_details(serverIP, serverPort, userUsername);
    }

    public static void message_sent_from_user(String message){
        //Sends the message the user typed from the GUI to the ClientServer.java script to be sent to the host server.
        System.out.println("[CONTROLLER] The user sent the message: " + message);
        synchronized(messageLock) {
            Main_controller.message = message;
            messageAvailable = true;
        }
    }

    public static String getMessage(){
        //The ClientServer.java script calls this method repeatedly. If a message exists, it is parsed to the ClientServer script.
        synchronized(messageLock) { //Synchronized threads because sometimes the ClientServer script would be out of sync with the controller script and wouldnt see any messages.
            if (!messageAvailable) {
                return null;
            }
            String temp = message;
            message = null;
            messageAvailable = false;
            return temp;
        }
    }

    public static void displayMessage(String recievedMessage, String senderName, String timestamp){
        //Called from the ClientServer.java script to display a message to the GUI
        gui.Gui guiInstance = gui.Gui.getInstance(); // ask the GUI for itself because its an object.
        if (guiInstance != null) {
            guiInstance.displayRecievedMessage(recievedMessage, senderName, timestamp);
        } else {
            System.err.println("[CONTROLLER] displayMessage(): GUI not initialised, message ignored");
        }
    }

    public static void system_message(String notification){
        //Notifies when users join, coordinator changes or user leaves.
        //The system messages appear in yellow on the GUI.
        gui.Gui guiInstance = gui.Gui.getInstance();
        if (guiInstance != null) {
            guiInstance.addSystemMessage(notification); //Adds a system/notification message into the GUI.
        } else {
            System.err.println("[CONTROLLER] notify_user(): GUI not initialised, notification ignored");
        }
    }

    public static void set_coordinator(String coordName) {
        //Recieves the coordinator name from the ClientServer.java script and sends it to the Gui.java script
        gui.Gui guiInstance = gui.Gui.getInstance();
        if (guiInstance != null) {
            guiInstance.coordinatorUsername = coordName;
            System.out.println("[CONTROLLER] Coordinator set to: " + coordName);
        }
    }

    public static void update_user_list(String data) {
        //Responsible for handling the list of users who are currently connected.
        //Data format of user list: "coordName|user1:ip1:port1,user2:ip2:port2,..."
        gui.Gui guiInstance = gui.Gui.getInstance();
        if (guiInstance == null) {
            System.err.println("[CONTROLLER] update_user_list(): GUI not initialised");
            return;
        }

        // Data format: "coordName|user1:ip1:port1,user2:ip2:port2,..."
        String coordName = "";
        String peerData  = data;

        if (data.contains("|")) {
            String[] split = data.split("\\|", 2);
            coordName = split[0].trim();
            peerData  = split[1].trim();
            guiInstance.coordinatorUsername = coordName;
        }

        //List of users stored in a dynamic array.
        java.util.List<gui.Gui.UserInfo> users = new java.util.ArrayList<>();

        if (!peerData.isEmpty()) {
            for (String entry : peerData.split(",")) {
                String[] parts = entry.split(":", 3);
                if (parts.length >= 3) {
                    String uname    = parts[0].trim();
                    String ip       = parts[1].trim();
                    String port     = parts[2].trim();
                    boolean isCoord = uname.equals(coordName);
                    users.add(new gui.Gui.UserInfo(uname, ip, port, isCoord));
                }
            }
        }

        guiInstance.updateOnlineUsers(users); //Update the list of online users in the GUI.
        System.out.println("[CONTROLLER] Online list updated — " + users.size() + " user(s)");
    }

    public static void createServerButtonPressed(Integer serverPort){
        //This method is called from the GUI. It transfer the users chosen port from the Gui.java script to the ClientServer.java script so we can create a server with the given details.
        CreateServer.create_new_server(serverPort);
    }

    public static void errorOccured(String error_message){
        //Responsible for calling DisplayerErrorMessage class.
        DisplayErrorMessage.displayError(error_message);
    }

    

    public static void handle_rejection(String reason) {
        //Handles errors if the user inputs an incorrect PORT or username.
        gui.Gui guiInstance = gui.Gui.getInstance();
        if (guiInstance == null) return;
        SwingUtilities.invokeLater(() -> {
            guiInstance.showErrorPopup(reason);
            guiInstance.returnToJoinScreen();
        });
    }
    
    public static void main(String[] args) {

        //Create a GUI object and start the GUI thread.
        Thread guiThread = new Thread(new Gui());
        guiThread.start();

        //Wait for the GUI to launch. Don't assume it is running instantly as some machines are slow.
        while (gui.Gui.getInstance() == null) {
            try { Thread.sleep(10); } catch (InterruptedException ignored) {} 
        }
    }
}