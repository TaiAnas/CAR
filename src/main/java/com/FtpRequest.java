package com;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class FtpRequest implements Runnable{

    private Socket socket;
    private boolean StillConnect;
    private HashMap<String, String> userAndPassWord;
    private String user = null;
    private boolean log = false;
    private BufferedReader in;
    private DataOutputStream out;
    private Socket downloadSocket;
    private boolean passiv = false;
    private Socket comSocket= null;
    private ServerSocket s;
    private int comPort;
    private static String path = "serveur" ;
    private File ancien_nom = null;

    public FtpRequest(Socket socket, HashMap<String, String> userAndPassWord) throws IOException{
        this.socket = socket;
        this.StillConnect = true;
        this.userAndPassWord = userAndPassWord;
        this.out = new DataOutputStream(this.socket.getOutputStream()); // ecrire
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // lire
        s = new ServerSocket(0);

    }
    public FtpRequest() {
        // TODO Auto-generated constructor stub
    }
    @Override
    public void run() {
        try {

            System.out.println("Connexion avec le client : " + socket.getInetAddress());
            out.writeBytes("220 ready\n");

            while (this.StillConnect) {
                try {

                    String message = in.readLine();
                    if (message != null) {
                        System.out.println("client : " +message);
                        this.processRequest(message);
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processRequest(String message) throws IOException {
        if(message != null) {
            String res = null;
            message = message.trim();
            String[] requet = message.split("\\s+",2);


            if (requet[0].equalsIgnoreCase("USER")) {
                res = this.processUSER(requet[1]);
            } else if (requet[0].equalsIgnoreCase("PASS")) {
                res = this.processPASS(requet[1]);
            } else if (requet[0].equalsIgnoreCase("LIST")) {
                res = this.processLIST();
            } else if (requet[0].equalsIgnoreCase("RETR")) {
                res = this.processRETR(requet[1]);
            } else if (requet[0].equalsIgnoreCase("PASV")) {
                res = this.processPASV();
            } else if (requet[0].equalsIgnoreCase("ACTV")) {
                res = this.processACTV();
            } else if (requet[0].equalsIgnoreCase("EPRT")) {
                res = this.processEPRT(requet[1]);
            } else if (requet[0].equalsIgnoreCase("STOR")) {
                res = this.processSTOR(requet[1]);
            } else if (requet[0].equalsIgnoreCase("CWD")) {
                res = this.processCD(requet[1]);
            }	else if (requet[0].equalsIgnoreCase("PWD")) {
                res = this.processPWD(path);
            }	else if (requet[0].equalsIgnoreCase("XMKD")) {
                res = this.processMKDIR(requet[1]);
            }	else if (requet[0].equalsIgnoreCase("RNFR")) {
                res = this.processRNFR(requet[1]);
            }	else if (requet[0].equalsIgnoreCase("XRMD")) {
                res = this.processXRMD(requet[1]);
            }	else if (requet[0].equalsIgnoreCase("QUIT")) {
                res = this.processQUIT();
            }	else if (requet[0].equalsIgnoreCase("RNTO")) {
                res = this.processRNTO(requet[1]);
            }
            else{
                this.sendMessage("ERROR YAZID");
            }
            this.sendMessage(res);
        }
    }

    public String processQUIT() {
        this.StillConnect= false;
        return "221 QUIT";
    }

    public String processXRMD(String message) {
        File rep = new File(path+"/"+message);
        String[]files = rep.list();
        if (files != null) {
            for(String f: files){
                File currentFile = new File(rep.getPath(),f);
                currentFile.delete();
            }
        }
        if(rep.delete())
            return "Delete saccess";
        return "530 Delete failed";
    }

    public String processRNFR(String message) throws IOException {

        ancien_nom = new File(path+"/"+message.trim());
        if(ancien_nom.exists())
            return "350 File exists";
        return "404 File not found !!";
    }

    public String processRNTO(String message) throws IOException{

        File nouveau_nom = new File(path+"/"+message.trim());

        if (ancien_nom.renameTo(nouveau_nom))
            return "250 Rename success";
        return null;
    }

    public String processMKDIR(String message) throws IOException {

        File dir = new File(path+"/"+message);
        if(dir.mkdirs()) {
            return message.trim()+" created";
        }
        return "KO";
    }

    public String processPWD(String path) throws IOException{

        String cleanPathFile = new File(path).getCanonicalPath();
        String[] s = cleanPathFile.split("FTP", 2);
        return s[0];

    }

    public String processCD(String message) throws IOException{
        //((/?\\w*/?)?(/?[.]{1,2})(/[.]{1,2})?)
        if(message.matches("((/?\\w*([.])?\\w+/?)|(/?[.]{1,2})(/[.]{1,2})?)|((/?\\w*([.])?\\w+/?)(/?[.]{1,2})(/[.]{1,2})?)")) {
            File f = new File(path+"/"+message);
            String s = processPWD(f.getPath());

            if(s.indexOf("serveur") != -1) {
                System.out.println("OK");
                if(f.isDirectory()) {
                    path += "/"+message;
                    return "OK";
                }else
                    return "No such directory or is not a directory !!";

            }
            else
                return "You can't go down !!";
        }

        return "Path name not correct !!";

    }



    public String processSTOR(String message) throws IOException{

        File f = new File(path+"/"+message);

        downloadSocket = new Socket();
        downloadSocket.connect(new InetSocketAddress(this.socket.getInetAddress(), this.comPort));
        this.sendMessage("125 download");

        DataInputStream dIn = new DataInputStream(downloadSocket.getInputStream());
        FileOutputStream dOut = new FileOutputStream(f);

        byte[] buffer = new byte[downloadSocket.getReceiveBufferSize()];
        int bytesRead = -1 ;
        while ((bytesRead = dIn.read(buffer)) != -1) {
            dOut.write(buffer, 0, bytesRead) ;
        }

        dIn.close();
        dOut.flush();
        sendMessage("226 File downloaded");
        dOut.close();

        return "OK";
    }

    public String processEPRT(String message) {

        String[] cmd = message.split("[|]");
        this.comPort = Integer.parseInt(cmd[3]);
        System.out.println("communication port : " + this.comPort);

        return "communication port : "+ this.comPort;

    }



    public String processPASV() throws IOException {
        if(log) {

            int port = s.getLocalPort();
            int p1 = port / 256;
            int p2 = port % 256;
            this.passiv = true;

            String[] adresse = InetAddress.getLocalHost().toString().split("[/]",2);
            String[] h1_4 =adresse[1].split("[.]");

            //sendMessage("227 passive mode ("+h1_4[0]+','+h1_4[1]+','+h1_4[2]+','+h1_4[3]+','+p1+","+p2+")");
            sendMessage("227 passive mode (0,0,0,0,"+p1+","+p2+")");
            this.downloadSocket = this.s.accept();

        }
        return null;
    }

    public String processACTV() throws IOException {
        if(log) {
            downloadSocket = new Socket();
            downloadSocket.connect(new InetSocketAddress(this.socket.getInetAddress(), this.comPort));
            return "ok";
        }
        return null;
    }

    public String processRETR(String message) throws IOException {
        File file = new File(path+"/"+message);

        if(file.exists()) {
            downloadSocket = new Socket();
            downloadSocket.connect(new InetSocketAddress(this.socket.getInetAddress(), this.comPort));
            this.sendMessage("125 download");
            DataOutputStream dOut = new DataOutputStream(downloadSocket.getOutputStream());

            if(file.isFile()) {
                FileInputStream dIn = new FileInputStream(file);

                byte[] buffer = new byte[downloadSocket.getSendBufferSize()];
                int bytesRead = -1 ;
                while ((bytesRead = dIn.read(buffer)) != -1) {
                    dOut.write(buffer, 0, bytesRead) ;
                }

                dIn.close();
                dOut.flush();
                sendMessage("226 File downloaded");
                dOut.close();
                return "OK";
            }else {
                File source = new File(path+"../../../../"+file.getName());
                //if(!source.exists())
                //	source.mkdirs();

                //file.renameTo(source);
                File[] files = file.listFiles();
                for(int i = 0; i < files.length; i++){
                    if(files[i].isDirectory())
                        processRETR(file.getName()+"/"+files[i].getName());
                    else {
                        //System.out.println(file.getName()+"/"+f.getName());
                        //processRETR(file.getName()+"/"+files[i].getName());
                        this.sendMessage("125 download");
                        dOut = new DataOutputStream(downloadSocket.getOutputStream());

                        if(file.isFile()) {
                            FileInputStream dIn = new FileInputStream(file);

                            byte[] buffer = new byte[downloadSocket.getSendBufferSize()];
                            int bytesRead = -1 ;
                            while ((bytesRead = dIn.read(buffer)) != -1) {
                                dOut.write(buffer, 0, bytesRead) ;
                            }

                            dIn.close();
                            dOut.flush();
                            sendMessage("226 File downloaded");
                            dOut.close();
                            return "OK";
                        }
						/*this.sendMessage("125 download");
						FileInputStream dIn = new FileInputStream(f);
						dOut = new DataOutputStream(downloadSocket.getOutputStream());

						byte[] buffer = new byte[downloadSocket.getSendBufferSize()];

						int bytesRead = -1 ;
						while ((bytesRead = dIn.read(buffer)) != -1) {
							dOut.write(buffer, 0, bytesRead) ;
						}

						dIn.close();
						dOut.flush();
						sendMessage("226 File downloaded");*/
                        //processRETR(file.getName()+"/"+f.getName());
                    }
                }
                //dOut.close();
					/*public static void copyDirectory(final File from, final File to) throws IOException {
						 if (! to.exists()) {
						  to.mkdir();
						 }
						 final File[] inDir = from.listFiles();
						 for (int i = 0; i < inDir.length; i++) {
						  final File file = inDir[i];
						  copy(file, new File(to, file.getName()));
						 }
					}*/

            }
            //dOut.flush();
            //sendMessage("226 File downloaded");
            //dOut.close();
            return "OK";
        }



        return "404 file not found";
    }


    public String processLIST() {
        if(log) {
            File repertoir = new File(path);
            StringBuilder builder = new StringBuilder();

            for(File f : repertoir.listFiles()){
                if(f.isDirectory())
                    builder.append(f.getName()+" -d \t");
                else
                    builder.append(f.getName()+" -f \t");
            }
            return builder.toString();
        }

        return "you mast log first !!";
    }

    public String processUSER(String message) {
        if (this.userAndPassWord.containsKey(message)) {
            user = message;
            return "331 name OK";
        }
        return "530 name KO";
    }

    public String processPASS(String message) {
        if(user != null && this.userAndPassWord.get(user).equals(message)) {
            log = true;
            return "230 pass word OK";
        }
        return "530 pass word KO";
    }


    public void sendMessage(String message) throws IOException {
        message += "\n";
        this.socket.getOutputStream().write(message.getBytes());
        this.socket.getOutputStream().flush();

    }
}