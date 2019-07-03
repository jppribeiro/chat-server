package org.joaoribeiro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void init() throws IOException {

        // SETUP SOCKET
        Socket socket = new Socket(InetAddress.getByName("localhost"), 7070);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // SETUP CONSOLE STREAMS
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));

        // START RECEIVING MESSAGES IN NEW THREAD
        Thread receiver = new Thread(new MsgReceiver(socket));
        receiver.start();

        // START SENDING MESSAGES
        sendMsg(socket, out, bReader);

    }

    private static void sendMsg(Socket socket, PrintWriter out, BufferedReader bReader) throws IOException{

        while (!socket.isClosed()) {

            out.println(bReader.readLine());

        }

    }

    private static class MsgReceiver implements Runnable {

        private Socket socket;
        private BufferedReader in;

        public MsgReceiver(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            try {

                String line = in.readLine();

                while (!socket.isClosed()) {

                    System.out.println(line);

                    line = in.readLine();

                    if (line == null) {
                        socket.close();
                    }

                }

                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }



        }

    }

}
