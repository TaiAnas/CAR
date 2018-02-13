package com;

import java.io.IOException;
import java.net.*;

public class ChannelData {

    public Socket getNewChannelData(final InetAddress clientAddr, final int port,final Boolean passive, final ServerSocket passiveSocket)throws ConnectException {

        Socket communicationSocket = null;
        try {
            if(passive){
                System.out.println("mode passive");
                communicationSocket= passiveSocket.accept();
            }else {
                System.out.println("mode active");
                communicationSocket = new Socket();
                communicationSocket.connect(new InetSocketAddress(clientAddr, port));
            }

        } catch (final ConnectException e) {
            throw new ConnectException() ;
        }catch (final IOException e) {
            e.printStackTrace();
        }
        return communicationSocket;
    }
}
