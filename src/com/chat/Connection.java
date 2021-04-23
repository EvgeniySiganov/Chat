package com.chat;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketAddress;


/**
 * Connection between Client and Server through Socket connection
 * Their talks between self by Message
 */
public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        //Output stream create before Input stream for avoid mutual block
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }


    /**
     * Thread save method record (serialize) parameter message
     * @param message will be serialize
     * @throws IOException when occur problem with socket connection
     */
    public void send(Message message) throws IOException{
        synchronized (out){
            out.writeObject(message);
        }
    }


    /**
     * Thread save method for read (deserialize) Message
     * @return deserialize Message
     * @throws IOException when occur problem with socket connection
     * @throws ClassNotFoundException when occur problem with class Message
     */
    public Message receive() throws IOException, ClassNotFoundException {
        Message message = null;
        synchronized (in){
            return message = (Message) in.readObject();
        }
    }



    public SocketAddress getRemoteSocketAddress(){
        return socket.getRemoteSocketAddress();
    }


    /**
     * Close all current resources
     * @throws IOException when occur problems with close resource
     */
    public void close() throws IOException{
        out.close();
        in.close();
        socket.close();
    }
}
