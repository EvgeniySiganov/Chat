package com.chat.client;


import com.chat.Connection;
import com.chat.ConsoleHelper;
import com.chat.Message;
import com.chat.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * Client exchange messages with Server
 */
public class Client {
    protected Connection connection;


    /**
     * clientConnected is true when Client connected with Server
     */
    private volatile boolean clientConnected;


    public static void main(String[] args) {
        new Client().run();
    }


    /**
     * Create support thread and wait until it connect with Server.
     * Read message with console and send it to Server
     * Enter "exit" for finish typing text
     */
    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Error during instant connecting with Server from address: " +
                        connection.getRemoteSocketAddress());
                return;
            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено.\n" +
                    "Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            return;
        }
        //this is were are sent messages from to the server when connect fixed
        while (clientConnected) {
            String text = ConsoleHelper.readString();
            if ("exit".equals(text)) {
                break;
            }
            if(shouldSendTextFromConsole()){
                //main thread writing to
                sendTextMessage(text);
            }
        }
    }


    /**
     * Request server address
     * @return String server address
     */
    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server address");
        return ConsoleHelper.readString();
    }


    /**
     * Request server port
     *
     * @return int server port
     */
    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server port");
        return ConsoleHelper.readInt();
    }


    /**
     * Request user name
     *
     * @return String user name
     */
    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter your name");
        return ConsoleHelper.readString();
    }


    /**
     * In current version this Client always return true
     * Further if Client will don`t must send text in console, this method will override and return false
     *
     * @return boolean true
     */
    protected boolean shouldSendTextFromConsole() {
        return true;
    }


    /**
     * Create new instance SocketTread
     *
     * @return new instance SocketTread
     */
   protected SocketThread getSocketThread() {
        return new SocketThread();
    }


    /**
     * Create text message and send it to Server
     * If IOException will be thrown the field clientConnected value will be set false
     *
     * @param text content that will be send to Server
     */
    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Error during sending text message from address: "
                    + connection.getRemoteSocketAddress());
            clientConnected = false;
        }
    }


    /**
     * SocketTread response for socket connection and reading Server messages
     */
    public class SocketThread extends Thread {

        @Override
        public void run() {
            String address = getServerAddress();
            int port = getServerPort();
            try (Socket socket = new Socket(address, port)){
                connection = new Connection(socket);
                clientHandshake();
                //additional thread listening to
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }

        /**
         * This method represents Client for Server
         * @throws IOException when message type incorrect
         * @throws ClassNotFoundException when deserialize is failed
         */
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message messageReceive = connection.receive();
                if(messageReceive.getType() == MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                }
                else if(messageReceive.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }


        /**
         * General loop for processing messages
         * @throws IOException when message type incorrect
         * @throws ClassNotFoundException when deserialize is failed
         */
        protected void clientMainLoop()throws IOException, ClassNotFoundException{
            while (true){
                Message messageReceive = connection.receive();
                if(messageReceive.getType() == MessageType.TEXT){
                    processIncomingMessage(messageReceive.getData());
                }
                else if(messageReceive.getType() == MessageType.USER_ADDED){
                    informAboutAddingNewUser(messageReceive.getData());
                }
                else if(messageReceive.getType() == MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(messageReceive.getData());
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }


        /**
         * Print text message in console
         * @param message this text will print in console
         */
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }


        /**
         * Print notification about joining user to chat
         * @param userName this name will print in console
         */
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " joined to chat");
        }


        /**
         * Print notification about deleting user from chat
         * @param userName this name will print in console
         */
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " going out from chat");
        }


        /**
         * Sets the value clientConnected for outer class Client
         * Notify waiting thread started from outer class Client
         * @param clientConnected is true for Client connected with Server
         */
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }
    }
}
