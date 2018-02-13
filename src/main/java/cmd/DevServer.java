package cmd;

public class DevServer {

        public static int port;
        public static String serverRepertoire;
        public static String courrantRepertoire;

        public DevServer(){
            this.serverRepertoire = "serveur";
            this.courrantRepertoire = "directory";
            this.port = 2018;
        }

        public String getDirectoryServer()
        {
            return serverRepertoire;
        }

        public int getPort(){
            return port;
        }

        public  String getCurrentDirectory()
        {
            return courrantRepertoire;
        }
        public void AddCurrentDirectory(String chemin){
            this.courrantRepertoire = chemin;
        }



    }
