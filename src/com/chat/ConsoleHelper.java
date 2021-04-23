package com.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Support class for read and write in console
 * All operations with console be there
 */
public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    /**
     * Output the message in console
     * @param message will be write
     */
    public static void writeMessage(String message){
        System.out.println(message);
    }


    /**
     * Input the text in console
     * @return String read out from console
     */
    public static String readString(){
        while (true) {
            try {
                return reader.readLine();
            } catch (IOException e) {
                writeMessage("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }
    }


    /**
     * Input the number in console
     * @return int read out from console
     */
    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(readString());
            } catch (NumberFormatException e) {
                writeMessage("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
    }
}
