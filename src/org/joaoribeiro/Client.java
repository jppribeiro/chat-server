package org.joaoribeiro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class Client {

    private String name;

    public Client (String name) {

        this.name = name;

    }



    public String getName() {
        return name;
    }
}
