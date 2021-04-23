package com.chat.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * The bot what may send current date and time when it be requested
 */
public class BotClient extends Client{

    public static void main(String[] args) {
        new BotClient().run();

    }


    /**
     * Create new instance BotSocketThread
     * @return new instance BotSocketThread
     */
    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }


    /**
     * In current version this method always return false
     * Text will not sent in console
     * @return false
     */
    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }


    /**
     * Generate bot name
     * @return bot name2
     */
    @Override
    protected String getUserName() {
        return "date_bot_" + (int)(Math.random() * 100);
    }


    public class BotSocketThread extends SocketThread{

        /**
         * General loop for processing messages from chat bot
         * Send all users greeting and list command
         * @throws IOException when message is incorrect
         * @throws ClassNotFoundException when problem with class Message by serialization
         */
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        /**
         * Print text message ib console
         * Print answer of client command
         * @param message this text will printed in console and then
         * processed and if correct command contains it will formed answer
         */
        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);
            if(!message.contains(": ")) return;

            String name = message.substring(0, message.lastIndexOf(": ") + 2);
            String command = message.substring(message.lastIndexOf(": ") + 2);

            String pattern;

            if ("дата".equals(command)) {
                pattern = "d.MM.YYYY";
            } else if ("день".equals(command)) {
                pattern = "d";
            } else if ("месяц".equals(command)) {
                pattern = "MMMM";
            } else if ("год".equals(command)) {
                pattern = "YYYY";
            } else if ("время".equals(command)) {
                pattern = "H:mm:ss";
            } else if ("час".equals(command)) {
                pattern = "H";
            } else if ("минуты".equals(command)) {
                pattern = "m";
            } else if ("секунды".equals(command)) {
                pattern = "s";
            }else {
                return;
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String answer = simpleDateFormat.format(Calendar.getInstance().getTime());
            //if message is command then formed answer
            sendTextMessage(String.format("Информация для %s%s", name, answer));
        }
    }
}
