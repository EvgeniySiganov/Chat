package com.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * General server class, support multiple connection with Clients
 */
public class Server {


    /**
     * Key - client name
     * Value - client connection
     */
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();


    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Enter your port");
        //Create Server socket connection
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Server is started");
            while (true) {
                //waiting until any Client connect with socket
                Socket socket = serverSocket.accept();
                //exchange message with Client
                new Handler(socket).start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Exception by create server");
        }
    }


    /**
     * Send message from all chat members in connectionMap
     * @param message will be send
     */
    public static void sendBroadcastMessage(Message message) {
        try {
            for (Connection connection :
                    connectionMap.values()) {
                connection.send(message);
            }
        } catch (IOException e) {
            System.out.println("failed to send this message");
        }
    }

    /**
     * Realise protocol communication with Client
     */
    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }


        @Override
        public void run() {
            String userName = null;
            ConsoleHelper.writeMessage("Install new connection with " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket)){
                //stage 1, registration
                userName = serverHandshake(connection);
                //stage 1.2, notification other users
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                //stage 2, display all user
                notifyUsers(connection, userName);
                //stage 3,
                serverMainLoop(connection, userName);
            }catch (IOException | ClassNotFoundException e){
                ConsoleHelper.writeMessage("Error with exchange data between remote address");
            }finally {
                if(!userName.isEmpty()){
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
                ConsoleHelper.writeMessage("Connection is closed");
            }
        }


        /**
         * First stage set connection with Client
         * @param connection with Client
         * @return name new Client
         * @throws IOException when problems with client connection
         * @throws ClassNotFoundException when problem by deserialization with class Message
         */
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message messageAnswer = connection.receive();
                if (messageAnswer.getType() != MessageType.USER_NAME) {
                    ConsoleHelper.writeMessage("Incorrect type message");
                    continue;
                }
                if (messageAnswer.getData() == null || messageAnswer.getData().isEmpty()) {
                    ConsoleHelper.writeMessage("Entered nickname is empty");
                    continue;
                }
                if (connectionMap.containsKey(messageAnswer.getData())) {
                    ConsoleHelper.writeMessage("This nickname already exist");
                    continue;
                }
                //if correct name been receive then new Client add in map
                connectionMap.put(messageAnswer.getData(), connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                return messageAnswer.getData();
            }
        }


        /**
         * Second stage, sending from Client messages about in the presence of another Clients
         * @param connection current Client
         * @param userName for non notify himself
         * @throws IOException when problem with socket connection
         */
        private void notifyUsers(Connection connection, String userName) throws IOException{
            for (String s: connectionMap.keySet()
                 ) {
                if(!userName.equals(s)){
                    connection.send(new Message(MessageType.USER_ADDED, s));
                }
            }
        }


        /**
         * Third stage, receive the text message of Client and sends out it the others members
         * Here is main process chat
         * @param connection with early added Client
         * @param userName for personalisation text message
         * @throws IOException when problem with socket connection
         * @throws ClassNotFoundException when problem with class Message by serialization
         */
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if(message.getType() != MessageType.TEXT){
                    ConsoleHelper.writeMessage("This message have incorrect type");
                    continue;
                }
                else{
                    String text = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, text));
                }
            }
        }
    }
}
