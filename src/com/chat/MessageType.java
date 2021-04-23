package com.chat;


/**
 * Type messages sends between Client and Server
 */
public enum MessageType {
    NAME_REQUEST,//when new Client connect to Server, Server request Client name
    USER_NAME,//when new Client receive the request, Client send his name to Server
    NAME_ACCEPTED,//when the Server receive the Client name, Server accepted name or requesting new name
    TEXT,//when Server the receive this type message, Server are sends this text for all Clients
    USER_ADDED,//when the Client join in chat the Server send this message type all Clients
    USER_REMOVED//when the Client going out the chat the Server send this message all Clients
}
