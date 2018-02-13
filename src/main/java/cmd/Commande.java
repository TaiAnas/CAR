package cmd;

public class Commande {

    //Commandes possibles
    public static final String CMD_USER = "USER";
    public static final String CMD_PASS = "PASS";
    public static final String CMD_STOR = "STOR";
    public static final String CMD_RETR = "RETR";
    public static final String CMD_LIST = "LIST";
    public static final String CMD_QUIT = "QUIT";
    public static final String CMD_PWD = "XPWD";
    public static final String CMD_CWD = "CWD";
    public static final String CMD_CDUP = "CDUP";




    public final static String WELCOME = "220 Welcome to the FTP Server.\r\n";

    /* USER */
    public final static String USER_OK = "331 Username is okay, now you need to tap the password\r\n";
    public final static String USER_KO = "530 Bad UserName.\r\n";

    /* PASS */
    public final static String PASS_OK = "230 authentification réussie.\r\n";
    public final static String PASS_KO = "430 echec d'authentication .\r\n";
    public final static String ERR_CHANGE_USER ="530 Impossible de changer d'utilisateur\r\n";

    /* LIST */
    public final static String LIST_MSG_BAD_REP = "504 Paramètre invalid.\r\n";
    public final static String LIST_OK = "150 listing.\r\n";
    public final static String LIST_FIN = "226 Répertoire renvoie OK..\r\n";

    /* SYST */
    public final static String SYST_OK = "215 MAC OS.\r\n";

    /* CDUP */
    public final static String CDUP_OK = "250 Vous êtes maintenant dans .\r\n";

    /* CWD */
    public final static String CWD_OK = "250 Vous êtes dans le répertoire .\r\n";
    public final static String CWD_MSG_NOT_REP = "550 Le répertoire %s n'existe pas :(.\r\n";

    /* PORT */
    public final static String PORT_MSG_OK = "200 Ouverture du port .\r\n";

    /* PWD */
    public final static String PWD_MSG_OK = "257 .\r\n";

    /* QUIT */
    public final static String QUIT_OK = "221 QUIT Au revoir.\r\n";

    /* RETR */
    public final static String RETR_IS_DIRECTORY = " c'est un répertoire :(.\r\n";
    public final static String RETR_START_TRANSFERT = "150 Début du transfert en mode ASCII.\r\n";
    public final static String RETR_NOT_EXIST = "550 le fichier n'existe pas.\r\n";
    public final static String RETR_END_TRANSFER = "226 Transfert terminé!.\r\n";

    /* STOR */
    public final static String STOR_START_TRANSFERT = "125 Début du transfert.\r\n";
    public final static String STOR_END_TRANSFERT = "226 Transfert fini.\r\n";
    public final static String STOR_ERR_TRANSFERT = "451 Une erreur est survenue .\r\n";

    /* MKDIR */
    public final static String MKD_OK = "257 Répertoire créé.\r\n";
    public final static String MKD_KO = "550 Impossible de créer le répertoire %s.\r\n";

    /* DELETE */
    public final static String DELE_OK = "200 Répertoire supprimé.\r\n";
    public final static String DELE_KO = "550 Impossible de supprimer le fichier/répertoire.\r\n";
    public final static String DELE_ERR = "550 Le fichier/répertoire n'existe pas.\r\n";

    /* Autres */
    public final static String STRANGE = "421 Une erreur est survenue.\r\n";

    /* Error  */
    public final static String ERR_ABORTED = "451 Une erreur est survenue :(. On annule..\r\n";
    public final static String ERR_BAD_CMD = "502 Command Invalid.\r\n";
    public final static String ERR_CANNOT_OPEN_DATA = "425 Argh, j'arrive pas à ouvrir le canal de data.\r\n";

    //retour à la ligne
    public static final String END_LINE = "\n";
}
