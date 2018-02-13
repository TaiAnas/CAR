package com;

import cmd.DevServer;
import cmd.User;
import java.io.*;
import java.util.logging.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Serveur {

    User u = new User();
    public void execute(){

        DevServer toolServer = new DevServer();
        try {
            ServerSocket serverSocket = new ServerSocket(toolServer.getPort());
            System.out.println("le serveur à l'ecoute sur le port : " + toolServer.getPort());

            while(true)
            {
                Socket socket = serverSocket.accept();
                Thread T = new Thread(new FtpRequest(socket, u.getUsers()));
                T.start();
            }

        } catch (IOException ex) {
            Logger.getLogger(Serveur.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args){
        Serveur server = new Serveur();
        server. execute();
    }

}
