package org.joaoribeiro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ChatServer {

    private final LinkedList<ServerWorker> clients = new LinkedList<>();

    private ServerSocket serverSocket;

    private int PORT = 7070;

    public void start() {

        //clients = new LinkedList<>();

        try {

            serverSocket = new ServerSocket(PORT);

            while(true) {

                Thread serverWorker = new Thread (new ServerWorker (serverSocket.accept()));

                serverWorker.start();

            }

        } catch (IOException ioEx) {
            System.out.println("IO Exception while starting the server.");
        }
    }

    private void msgReceived(String msg, ServerWorker activeClient) {

        // BROADCAST TO CLIENTS

        for (ServerWorker client:
             clients) {
            if (!client.equals(activeClient)) {
                client.sendMsg (activeClient.getClientName() + ": " + msg);
            }
        }
    }

    private void clientQuit(ServerWorker quittingClient) {

        synchronized (clients) {
            for (int i = 0; i < clients.size(); i++) {

                if (clients.get(i).equals(quittingClient)) {

                    try {
                        clients.get(i).closeClient();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    clients.remove(i);
                    break;

                }
            }
        }
    }

    private void displayList(ServerWorker activeClient) {

        for (ServerWorker client:
             clients) {

            activeClient.sendMsg(client.getClientName());

        }
    }

    private void sendToClient(String clientName, String msg) {

        for (ServerWorker client : clients) {
            if (client.getClientName().equals(clientName)) {

                client.sendMsg(msg);

            }
        }

    }

    private class ServerWorker implements Runnable {

        private final Socket clientSocket;

        private String clientName;

        private PrintWriter out;

        private BufferedReader in;

        public ServerWorker (Socket clientSocket) {

            synchronized (clients) {

                this.clientSocket = clientSocket;

                setupStreams();

                try {
                    clientName = askName();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                clients.add(this);

                System.out.println("New client connection");
            }
        }

        public void run() {

            try {

                String line = in.readLine();

                while (!line.equals("/quit")) {

                    synchronized (clients) {

                        switch (line) {
                            case "/list":
                                displayList(this);
                                line = in.readLine();
                                continue;
                            case "/whisper":
                                String whisperName = whisperClient();
                                String msg = whisperMsg();
                                sendToClient(whisperName, msg);
                                line = in.readLine();
                                continue;
                        }

                        msgReceived(line, this);

                    }

                    line = in.readLine();

                    System.out.println(line);
                    if (line.isEmpty()) {
                        break;
                    }
                }

                clientQuit(this);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void sendMsg(String msg) {

            out.println(msg);

        }



        private String askName() throws IOException{

            out.println("Welcome, what's your name?");

            return in.readLine();

        }


        private String whisperClient() throws IOException {
            out.println("Client name: ");

            return in.readLine();
        }

        private String whisperMsg() throws IOException {
            out.println("Private message: ");
            return in.readLine();
        }

        private void closeClient() throws IOException {
            in.close();
            out.close();
            clientSocket.close();
        }



        private void setupStreams() {

            try {

                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

            } catch (IOException ex) {
                System.out.println("IOException while setting up client streams.");
            }
        }

        public String getClientName() {
            return clientName;
        }
    }



}
