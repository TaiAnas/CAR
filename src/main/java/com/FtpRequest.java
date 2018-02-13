package com;
import cmd.*;
import cmd.Exception.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.nio.file.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


public class FtpRequest extends Thread {



    private boolean isAuthenticated;
    private boolean isConnected;
    private boolean isPassive;

    public BufferedReader in;
    private final ServerSocket passiveSocket;
    public Socket socket;
    private InetAddress clientAddr;
    private final InputStream dataIn;
    private final OutputStream dataOut;
    private Socket socketData;
    private int communicationPort;

    private ServerSocket serverSocket;
    public String login, password, message;

    public User auth;
    public DevServer parServ;

    public FtpRequest(final Socket socket) throws IOException {
        this.socket = socket;
        this.auth = new User();
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.dataIn = this.socket.getInputStream();
        this.dataOut = this.socket.getOutputStream();
        this.clientAddr = this.socket.getInetAddress();
        this.communicationPort = this.socket.getPort();
        this.isAuthenticated = false;
        this.isConnected = false;
        this.isPassive = false;
        this.passiveSocket = new ServerSocket(0);
    }


    private String cleanCmd(final String cmd) {
        return cmd.replaceAll("\n|\r", "");
    }

    public String[] Spplit(String cmd) {

        String[] tab;
        tab = cmd.split(" ");
        return tab;
    }

    @Override
    public void run() {
        try {
            this.sendMessage(Commande.WELCOME);

            while (true) {
                message = in.readLine();
                if (message != null) {
                    processRequest(message);
                }
            }

        } catch (IOException er) {
            System.out.println("erreur : " + er);
        }
    }

    public boolean isPassive() {
        return this.isPassive;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    private void sendMessage(String msg) throws IOException {
        msg += Commande.END_LINE;
        this.dataOut.write(msg.getBytes());
        this.dataOut.flush();
    }

    private void processRequest(String message) {


        System.out.println("serv:  " + message + "\n");

        try {
            if (message != null) {
                String tab[] = message.split(" ");
                if (tab[0].equals("USER")) {
                    processUSER(tab[1]);
                } else if (tab[0].equals("PASS")) {
                    processPASS(tab[1]);
                } else if (tab[0].equals("QUIT")) {
                    processQUIT();
                } else if (tab[0].equals("OPTS")) {
                    processOPTS();
                } else if (tab[0].equals("PORT")) {
                    processPORT(tab[1]);
                } else if (tab[0].equals("PASV")) {
                    processPASV();
                } else if (tab[0].equals("LIST") || tab[0].equals("NLST")) {
                    processLIST();
                } else if (tab[0].equals("RETR")) {
                    processRetr(tab[1]);
                } else if (tab[0].equals("STOR")) {
                    processStor(tab[1]);
                } else {
                    this.sendMessage(Commande.ERR_BAD_CMD);
                    System.out.println("others de switch\n");
                    throw new NotExistCommandException();
                }
            }
        } catch (NotAuthentificationException b) {
            System.out.println("erreur: " + b);
        } catch (NotDirectoryException ND) {
            System.out.println("erreur : " + ND);
        } catch (NotExistCommandException ExCmd) {
            System.out.println("Erreur : " + ExCmd);
        } catch (BadOrderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processUSER(String user) throws IOException {

        if (!this.isConnected) {

            this.login = user;
            if (auth.CorrectUser(user)) {
                this.sendMessage(Commande.USER_OK);
            } else {
                this.sendMessage(Commande.USER_KO);
            }

        } else {
            this.sendMessage(Commande.ERR_CHANGE_USER);
        }
    }

    private void processPASS(String password) throws BadOrderException, IOException {
        this.password = password;

        if (auth.CorrectPass(password)) {
            this.isConnected = true;

            this.sendMessage(Commande.PASS_OK);
        } else {
            this.isConnected = false;

            try {
                this.sendMessage(Commande.PASS_KO);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void processQUIT() throws IOException {
        this.isConnected = false;

        this.sendMessage(Commande.QUIT_OK);
        socket.close();
    }

    public void processPORT(String tab) throws IOException {
        String AdrrPort[] = tab.split(",");
        communicationPort = (Integer.parseInt(AdrrPort[4]) * 256) + Integer.parseInt(AdrrPort[5]);
        clientAddr = InetAddress.getByName(AdrrPort[0] + "." + AdrrPort[1] + "." + AdrrPort[2] + "." + AdrrPort[3]);
        this.socketData = new Socket(this.clientAddr, this.communicationPort);

        this.sendMessage("200 PORT command successful. Consider using PORT.\r\n");
    }

    public void processPASV() throws IOException {
        this.serverSocket = new ServerSocket(0);
        this.isPassive = true;

        byte[] Adress = this.socket.getInetAddress().getAddress();
        String addr = "";
        for (byte bit : Adress) {
            addr += bit + ",";
        }
        int port = serverSocket.getLocalPort();
        this.sendMessage("227 Entering Passive Mode (" + addr + (port / 256) + "," + (port % 256) + ")\r\n");
        this.socketData = this.serverSocket.accept();

    }

    public void processLIST() throws NotAuthentificationException, IOException {


        String FileName = "";
        String permission = "";
        String resultat = "";
        Date date = null;
        String userName = "";

        if (isConnected()) {

            sendMessage("150 Opening data channel for directory list.\r\n");


            OutputStream out = socketData.getOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);

            File[] files = new File(parServ.getCurrentDirectory()).listFiles();
            for (File file : files) {

                FileName = file.getName();

                date = new Date(file.lastModified());

                userName = Files.getOwner(file.toPath()).toString();

                if (file.isFile()) {
                    permission = "-rw-rw-rw-";
                } else {
                    permission = "drw-rw-rw-";
                }

                resultat += permission + "\t" + userName + "\t" + file.length() + "\t" + date + "\t" + FileName + "\n";
            }

            dataOut.writeBytes(resultat + "\n");
            dataOut.close();
            this.socketData.close();
            out.close();
            if (this.serverSocket != null) {
                this.serverSocket.close();
                this.serverSocket = null;
            }
            sendMessage("226 Directory send OK.\r\n");
        } else {
            this.sendMessage("550 errour non ethentification.\r\n");
            throw new NotAuthentificationException();
        }
    }

    public void processStor(String fichier) throws NotAuthentificationException, IOException {

        if (this.isConnected() != true) {
            this.sendMessage("550 errour non ethentification.\r\n");
            throw new NotAuthentificationException();
        } else {
            if (this.socketData == null) {
                this.sendMessage("425 la connexion data n'est pas realisée\r\n");
            } else {

                InputStream in = this.socketData.getInputStream();
                File f = new File(parServ.getCurrentDirectory());
                String chemin = f.toPath().toAbsolutePath().toString();
                Path tar = Paths.get(chemin + "/" + fichier);
                System.out.println("etape 1");
                this.sendMessage("125 Starting transfer.\r\n");
                Files.copy(in, tar, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("etape 2");
                in.close();
                this.socketData.close();

                //this.serverSocket.close();
                this.sendMessage("226 \r\n");
            }

            System.out.println("on a sortie de la fonctin stor");
        }
    }

    public void processOPTS() throws IOException {

        this.sendMessage("200 Welcome on the FTP Server\r\n");
    }

    public void processRetr(String fichier) throws NotAuthentificationException, IOException {

        if (this.isConnected() != true) {
            this.sendMessage("550 errour non ethentification.\r\n");
            throw new NotAuthentificationException();
        } else if (this.socketData == null) {
            this.sendMessage("425 la connexion data n'est pas realisée\r\n");
        } else {

            //this.envoyerMessage("125 Starting transfer.\r\n");

            File f = new File(parServ.getCurrentDirectory());

            String chemin = f.toPath().toAbsolutePath().toString();
            Path tar = Paths.get(chemin + "/" + fichier);

            File ftest = new File(parServ.getCurrentDirectory() + File.separator + fichier);

            if(ftest.exists()){
                this.sendMessage("125 Starting transfer.\r\n");

                OutputStream os = this.socketData.getOutputStream();
                Files.copy(tar, os);
                os.flush();
                System.out.println("etape 1");
                this.sendMessage("226 RETR Transfer completed.\r\n");

                this.socketData.close();
                this.serverSocket = null;
            }else{

                this.sendMessage("550 " + fichier + " Is Not Exist.\r\n");
            }
        }
    }

    public void processPWD() throws NotAuthentificationException, IOException {

        if (!this.isConnected()) {
            this.sendMessage("550 errour non ethentification.\r\n");
            throw new NotAuthentificationException();
        } else {

            String tmp = new File(parServ.getCurrentDirectory()).getCanonicalPath();
            String chemin = tmp.substring(parServ.getDirectoryServer().length()-2);
            chemin = chemin.replace(File.separator, new String("/"));


            if (chemin.length() == 0) {
                chemin = chemin + "/";
            }

            this.sendMessage("257 " + chemin + " \r\n");

        }
    }


}


