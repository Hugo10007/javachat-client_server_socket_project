/*
ClientServer.java is used to handle all the networking, coordinator and user logic.
Controlled by the Main_controller.java script.
One of its primary features is creating a new thread for each connected user and handling all the users communcation & logic.
Responsbile for joining and ensuring a stable connection to a server using a users IP, port and username.
Responsible for creating servers with a given IP and port.

BY:
AMIN SHEIKH
KIPP SUMMERS
HUGO PIPER
DHARI ALTHUNAYAN
JAHID EMON
*/

package client_server_model;

//We don't need to import the controller because we used its full package name every time.

//All imports
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.*;

class ClientThread implements Runnable{
    /*
    Responsible for handling each of the users which are connected. Client-side processing.
        - Handles the user list
        - Handles periodic ping
        - Handles coordinator logic
        - Handles the broadcast of direct messages
        - Handles the broadcast of regular messages
        - Displays yellow system messages
    */
    private Socket socket;
    private Scanner in;
    private PrintWriter out;
	private String username;
	private volatile long lastPong = System.currentTimeMillis(); //needs to be volatile so threads can update individually. Pulled my hair out over this
	private boolean coordinator = false;
    private static ConcurrentHashMap<String, ClientThread> clients = new ConcurrentHashMap<>();
    

    public ClientThread (Socket socket) throws IOException{
        this.socket = socket;
        this.in = new Scanner(socket.getInputStream());
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }
    @Override
    public void run(){
        try{
        	username = in.nextLine();
            //Reject if username is already taken
            if (clients.containsKey(username)) {
                out.println("reject Username '" + username + "' is already taken.");
                socketClose();
                return;
            }
            
            //Yellow system message when a user joins
            clients.put(username, this);
            broadcast("system " + username + " has joined");

            //Assigns the first user who joins the role of coordinator.
			if (clients.size() == 1) {
                coordinator = true;
                broadcast("coord " + username);
                System.out.println("[SERVER] " + username + " is the coordinator");
            	ping();
			} else {
                //If there is more than 1 person in the chat, notify the user who the current coordinator is.
                ClientThread currentCoord = getCoordinator();
                if (currentCoord != null) {
                    out.println("system The current coordinator is " + currentCoord.username);
                }
            }

            //Server sends the Username, IP and PORT when a new user joins to the coordinator directly.
            sendToCoordinator("peerinfo " + username + "|" + socket.getInetAddress().getHostAddress() + "|" + socket.getPort());
            

            while (in.hasNextLine()){
                //Periodic ping logic that is executed by the coordinator.
                String message = in.nextLine();
				if (message.equals("pong")) {
                    System.out.println("[COORDINATOR] Pong received from: " + username);
                    lastPong = System.currentTimeMillis();
                }
				
                else if (message.startsWith("/dm")) {
                    //Handles direct messages that begin with '/dm'
                    sendDirectMessage(message);
                }
                else if (message.startsWith("system")){
                    //Handles yellow system messages
                    controller.Main_controller.system_message(message);
                }
                else if (message.startsWith("userlist ")) {
                    //Handles the broadcast of the current active users.
                    broadcast(message);
                    System.out.println("[SERVER] Recieved active user list from coordinator: \n" + message + "\n");
                }
                else if (message.startsWith("timezone ")){
                    broadcast(message);
                    System.out.println("[SERVER] Coordinator timezone shared: " + message);
                }
                else {
                    //If the message doesnt start with the above prefixes, the message is from a user.
                    //THIS IS THE FORMAT THAT MESSAGES ARE SENT FROM EACH USER: username|time|message
                    broadcast(username + "|" + System.currentTimeMillis() + "|" + message); //"|" is a NECESSITY here due to how regex handling works of the recieved messages.
                    System.out.println("[SERVER] Message processed from " + username);
                }
            }
        }
        finally{
            //Handles logic of when a user leaves. If they're a coordinator, assigns a new coordinator.
            if (username != null && clients.remove(username, this)) { // only proceeds if remove actually succeeded (stops glitch with duplicate usernames)
                broadcast("system " + username + " has left the chat");
                System.out.println("[SERVER] " + username + " has disconnected.");
                if (coordinator) {
                    assignNewCoordinator();
                }
                //Tells the coordinator that someone has left and to update the 'online' list.
                sendToCoordinator("depart " + username);
            }
            //Closes the socket connection.
            socketClose();
        }
    }

	private void ping() {
        //Periodic ping logic that is handled by the coordinator.
        Thread pingThread = new Thread(() -> {
            while (this.coordinator) {
                long pingSentAt = System.currentTimeMillis();
                //Sends a ping to all non-coordinator clients
                for (ClientThread client : clients.values()) {
                    if (!client.coordinator) {
                        client.out.println("ping");
                    }
                }

                //Waits 3 secs for pongs to arrive, used because it takes a little while for a pong to come through (and causes premature server shutdown).
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    return;
                }

                //Check who hasn't responded since the ping was sent
                for (ClientThread client : new java.util.ArrayList<>(clients.values())) {
                    if (!client.coordinator && client.lastPong < pingSentAt) {
                        System.out.println("[SERVER] " + client.username + " timed out, removing.");
                        clients.remove(client.username);
                        broadcast("system " + client.username + " has left the chat");
                        sendToCoordinator("depart " + client.username);
                        try { client.socketClose(); } catch (Exception ignored) {}
                    }
                }

                //Waits before sending the next ping (coursework gives 20sec as an example)
                try {
                    Thread.sleep(7000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        pingThread.setDaemon(true);
        pingThread.start();
    }

	private ClientThread getCoordinator() {
        //Used to fetch the coordinators username
    	for (ClientThread client : clients.values()) {
    		if (client.coordinator == true) {
    			return(client);
    		}
    	}
    	return(null);
    }

    private void broadcast(String message) {
        //Used to send messages to ALL users
        for (ClientThread client : clients.values()) {
            client.out.println(message);}
    }

    private void sendToCoordinator (String message) {
        //Sends a message from any member to the coordinator. Used for requesting IP, Username and PORT in the user online list.
        ClientThread coord = getCoordinator();
        if (coord != null){
            coord.out.println(message);
        }
    }

	private void assignNewCoordinator() {
        //Handles assigning a new coordinator if there is none or if someone leaves
    	if (clients.size() == 0) {
    		System.out.println("[SERVER] no viable members to become coordinator");
    	}
    	else {
	        for (ClientThread client : clients.values()) {
	            client.coordinator = true;
                broadcast("coord " + client.username);
                System.out.println("\n[SERVER] " + client.username + " is the new coordinator");
	            break;
	        }
    	}
    }
	
	private void sendDirectMessage(String message) {
        //Handles a user sending a direct message to another user. Ensures message isnt displayed to other clients.
        System.out.println("[SERVER] DM recieved: " + message);

    	String[] splitMsg = message.split(" ",3);
    	
    	if (splitMsg.length < 3) {
    		out.println("system use this format: /dm <username> <message>");
    		return;
    	}
    	
    	String user = splitMsg[1];
    	String dm = splitMsg[2];
        //out.println("system User: " + user + " Message: " + dm);
    	
        //Assigns the 'target' (recipient) of the DM as a ClientThread object
    	ClientThread target = clients.get(user);
    	
    	if (target == null) {
    		out.println("system That member does not exist.");
    		return;
    	} 
        else {
            long timestamp = System.currentTimeMillis();
            target.out.println("dm|" + username + "|" + timestamp + "|" + dm); //Use whitespaces here instead of real spaces so the username doesnt end up in the text field lol.
    	    out.println("DM to "+ user + "- "+ dm);
        }
    	
    }

    private void socketClose() {
        //Handles shutting the server down.
        try {
            socket.close();
        } 
        catch (IOException e) {
            System.err.println("Error closing socket");
        }
    }
}

class Server{
    /*
    Responsible for handling everything related to sockets and networking.
        - Handles the logic for joining a server
        - Handles the logic for creating a server
        - Finds clients private IPv4 addresses
        - Processes and formats the coordinators online user list and transmits it to all the users. 
    */
    
    private static void attempt_to_join_server(String ip_Address, int serverPort, String userUsername) {
        System.out.println("ATTEMPTING TO JOIN SERVER AT IP: " + ip_Address + " ON PORT " + serverPort);

        //Initialises a socket in an attempt to join a server based on its IP and PORT
        try (Socket socket = new Socket(ip_Address, serverPort); 
            Scanner fromServer = new Scanner(socket.getInputStream());
            PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true)){

            //Atomic variables so we don't encounter threading issues.
            java.util.concurrent.atomic.AtomicBoolean isCoordinator = new java.util.concurrent.atomic.AtomicBoolean(false);
            java.util.concurrent.atomic.AtomicReference<String> lastUserlistData = new java.util.concurrent.atomic.AtomicReference<>("");
            java.util.concurrent.ConcurrentHashMap<String, String[]> peerMap = new java.util.concurrent.ConcurrentHashMap<>();
            java.util.concurrent.atomic.AtomicReference<String> coordinatorTimeZone = new java.util.concurrent.atomic.AtomicReference<>("UTC");

            //Responsible for sending messages to the clients with different prefixes depending on the nature of the message
            Thread listenerThread = new Thread(() -> {
                while (fromServer.hasNextLine()) {
                    String message = fromServer.nextLine();
                    System.out.println("[CLIENTSERVER] Received: " + message);

                    //Displays a system error if message begins with 'reject'. Is filtered if a user tries to send a starting with 'reject'
                    if (message.startsWith("reject ")) {
                        String reason = message.substring(7).trim();
                        controller.Main_controller.handle_rejection(reason);
                        return; // Stop the listener thread
                    }

                    //Used to set the coordinator of a specific user.
                    if (message.startsWith("coord ")) {
                        String coordName = message.substring(6).trim();
                        boolean isSelf   = coordName.equals(userUsername);
                        isCoordinator.set(isSelf);
                        controller.Main_controller.set_coordinator(coordName);
                        controller.Main_controller.system_message(coordName + " is the coordinator");

                        //Assigns a map to the coordinator so they can handle the online user list themselves.
                        if (isSelf) {
                            synchronized(toServer){
                                //Sends the timezone from the coordinator so all the users are synched to the same timezone.
                                toServer.println("timezone " + java.time.ZoneId.systemDefault().getId());
                            }
                            peerMap.clear();
                            peerMap.put(userUsername, new String[]{"coordinator", "-"}); // Coordinator's own entry
                            String lastData = lastUserlistData.get();
                            if (!lastData.isEmpty()) {
                                for (String entry : lastData.split(",")) {
                                    String[] parts = entry.split(":", 3);
                                    if (parts.length >= 3) {
                                        peerMap.put(parts[0].trim(), new String[]{parts[1].trim(), parts[2].trim()});
                                    }
                                }
                            }
                        }
                    }

                    //If someone joins, the users info is sent to the coordinator in the format 'USERNAME|IP|PORT'
                    else if (message.startsWith("peerinfo ") && isCoordinator.get()) {
                        String payload = message.substring(9); // strip peerinfo prefix
                        String[] parts = payload.split("\\|", 3);
                        if (parts.length >= 3) {
                            peerMap.put(parts[0], new String[]{parts[1], parts[2]});
                            broadcastUserList(toServer, peerMap, userUsername);
                        }
                    }

                    //Sends the username of who just left to the coordinator so they can remove the user from the list
                    else if (message.startsWith("depart ") && isCoordinator.get()) {
                        String departedUser = message.substring(7).trim();
                        peerMap.remove(departedUser);
                        broadcastUserList(toServer, peerMap, userUsername);
                    }

                    //Recieves the online users list that the coordinator broadcasts. Format 'user1|ip1|port1,user2|ip2|port2,...userN|ipN|portN'
                    else if (message.startsWith("userlist ")) {
                        String data = message.substring(9).trim();
                        //Strips the coordName| prefix before storing so peerMap reconstruction on coordinator handover gets clean entries.
                        String peerOnly = data.contains("|") ? data.split("\\|", 2)[1] : data;
                        lastUserlistData.set(peerOnly);
                        controller.Main_controller.update_user_list(data);
                        System.out.println("[CLIENTSERVER] RECIEVED USER LIST FROM COORDINATOR: \n" + data);
                    }

                    else if (message.equals("ping")) {
                        synchronized(toServer) { toServer.println("pong"); }
                        System.out.println("[CLIENTSERVER] Ping received, sent pong");
                    }

                    else if (message.startsWith("system ")) {
                        String content = message.substring(7).trim();
                        controller.Main_controller.system_message(content);
                    }

                    else if (message.startsWith("timezone ")){
                        String zoneId = message.substring(9).trim(); //Removed the 'timezone' prefix.
                        coordinatorTimeZone.set(zoneId);
                        //Updates the GUIs timezone so the same timezone is displayed for all users.
                        gui.Gui guiInstance = gui.Gui.getInstance();
                        if (guiInstance != null) {
                            guiInstance.coordinatorTimeZone = zoneId;
                            System.out.println("[CLIENTSERVER] Coordinator timezone: " + zoneId);
                        }
                    }

                    else if (message.startsWith(userUsername + "|")) {
                        // Own message echoed back — ignore
                    }
                    
                    else if (message.startsWith("dm|")) {
                        String[] parts = message.split("\\|", 4);

                        if (parts.length == 4) {
                            String sender = parts[1];
                            String time = formatTimeStamp(parts[2], coordinatorTimeZone.get());
                            String dmMessage = parts[3];

                            controller.Main_controller.displayMessage(dmMessage, sender + " SENT YOU A DM", time);
                        }
                    }

                    else {
                        String[] sliced = message.split("\\|", 3);
                        if (sliced.length >= 3) {
                            String formattedTime = formatTimeStamp(sliced[1], coordinatorTimeZone.get());
                            controller.Main_controller.displayMessage(sliced[2], sliced[0], formattedTime);
                        }
                    }
                }
                //If the code reaches this stage, the server has been shut down.
                controller.Main_controller.system_message("The server has been shut down.");
            });
            listenerThread.setDaemon(true);
            listenerThread.start();

            synchronized (toServer) { toServer.println(userUsername); }
            System.out.println("[CLIENTSERVER] Connected as: " + userUsername);

            while (true) {
                String msg = controller.Main_controller.getMessage();
                if (msg != null && !msg.isEmpty()) {
                    System.out.println("[CLIENTSERVER] Sending: " + msg);
                    synchronized (toServer) { toServer.println(msg); }
                }
            }

        } catch (Exception e) {
            System.err.println("[CLIENTSERVER] Could not connect to the server.");
            gui.Gui guiInstance = gui.Gui.getInstance();
            if (guiInstance != null) {
                guiInstance.showErrorPopup("Could not connect to the server.\nCheck the IP and port and try again.");
                guiInstance.returnToJoinScreen();
                }
            }
        }

    private static void initialise_server(String serverIpAddress, Integer serverPort) throws IOException {

        System.out.println("SERVER LAUNCHING ON IP ADDRESS: " + serverIpAddress + " USING PORT " + serverPort);

        //No catch required as IOException contains a catch block.
        try (ServerSocket coordinator = new ServerSocket(serverPort);){
            while (true){
                Socket clientSocket = coordinator.accept();

                System.out.println("[CLIENTSERVER] New client connected: " + clientSocket.getInetAddress().getHostAddress());

                ClientThread client = new ClientThread(clientSocket);
                Thread clientThread = new Thread(client);
                clientThread.start();
            }
        }
    }


    public static String get_ip_address() throws UnknownHostException {
        //Returns the private IP address of the user as a string. A seperate method was used to keep code clean.
        InetAddress localhost = InetAddress.getLocalHost();
        String private_ip = localhost.getHostAddress().trim();

        return private_ip;
    }

    public static void create(Integer serverPort) throws Exception{

        String serverIpAddress = get_ip_address(); 

        try {
            System.out.println("The users port is: " + serverPort);
            if (serverPort >= 1 && serverPort <= 65534){
                //Attempts to create the server with provided PORT
                initialise_server(serverIpAddress, serverPort);
            } else {
                System.err.println("[CLIENTSERVER] Port is either too small or too large. Must be within 1 - 65534. EXITING");
                System.exit(3);
            }

        } catch(Exception e) {
            System.err.println("[CLIENTSERVER] Port is already in use or invalid.");
            gui.Gui guiInstance = gui.Gui.getInstance();
            if (guiInstance != null) {
                    guiInstance.showErrorPopup("Port " + serverPort + " is already in use.\nPlease choose a different port.");
                    guiInstance.returnToCreateScreen();
                };
            }
        }

    public static void join(String serverIP, Integer serverPort, String userUsername){
        
        System.out.println("[CLIENTSERVER] The user chose to join server with attributes:" + serverIP + ", " + serverPort + ", " + userUsername);

        try {
            if (serverPort >= 1 && serverPort <= 65534){
                Server.attempt_to_join_server(serverIP, serverPort, userUsername);
            } else {
                System.err.println("[CLIENTSERVER] Invalid PORT. Must be between 1 and 65534. EXITING");
                System.exit(5);
            }
        } catch (Exception e) {
            System.err.println("[CLIENTSERVER] Error joining server. EXITING");
            System.exit(5);
        }
    }

    private static void broadcastUserList(PrintWriter toServer, java.util.concurrent.ConcurrentHashMap<String, String[]> peerMap, String coordinatorName) {
        StringBuilder sb = new StringBuilder("userlist ");
        sb.append(coordinatorName).append("|");
        boolean first = true;
        for (java.util.Map.Entry<String, String[]> entry : peerMap.entrySet()) {
            if (!first) sb.append(",");
            // Data format: "coordName|user1:ip1:port1,user2:ip2:port2,..."
            sb.append(entry.getKey()).append(":").append(entry.getValue()[0]).append(":").append(entry.getValue()[1]);
            first = false;
        }
        synchronized (toServer) { toServer.println(sb.toString()); }
        System.out.println("[CLIENTSERVER] Coordinator broadcast: " + sb.toString());
    }

    private static String formatTimeStamp(String epochMillis, String zoneId){
        try {
            long millis = Long.parseLong(epochMillis);
            java.time.Instant instant = java.time.Instant.ofEpochMilli(millis);
            java.time.ZoneId zone = java.time.ZoneId.of(zoneId);
            java.time.ZonedDateTime zdt = instant.atZone(zone);
            return java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(zdt);
        } catch (Exception e) {
            return "";
        }

    }
}

public class ClientServer {
    //Responsible for selecting whether a user chose to create or join a server

    public void start(String user_selection, String serverIp, Integer serverPort, String userUsername) throws Exception {
        /*Called by the controller script.
            - "Create" creates a server.
            - "Join" joins a server.
        */
        System.out.println("[SERVER] The user chose the choice: " + user_selection);
        
        if (user_selection.equals("create")){
            Server.create(serverPort);
        }
        else if (user_selection.equals("join")){
            Server.join(serverIp, serverPort, userUsername);
        }
        
        assert (user_selection != "create" || user_selection != "join"): "The GUI did not catch incorrect input. The program should have never reached here.";
    }
}
