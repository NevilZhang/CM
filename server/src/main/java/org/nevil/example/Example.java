package org.nevil.example;

import org.nevil.server.Server;

public class Example {
    public static void main(String[] args) {
        try {
            Server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
