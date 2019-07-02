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

    private int PORT = 8085;

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
                client.sendMsg (activeClient.getClient().getName() + ": " + msg);
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

            activeClient.sendMsg(client.getClient().getName());

        }
    }

    private class ServerWorker implements Runnable {

        private final Socket clientSocket;

        private Client client;

        private PrintWriter out;

        private BufferedReader in;

        public ServerWorker (Socket clientSocket) {

            synchronized (clients) {

                this.clientSocket = clientSocket;

                setupStreams();

                try {
                    client = new Client(askName());
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
                        }

                        msgReceived(line, this);

                    }

                    line = in.readLine();

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

        public Client getClient() {
            return client;
        }
    }



}
