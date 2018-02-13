package cmd;

import java.util.HashMap;
import java.util.Map;

public class User {

    public HashMap<String, String> users = new HashMap<>();
    private String user,password;

    public HashMap<String, String> getUsers() {
        return users;
    }

    public User(){
        users.put("Mohamed","Kaddou");
        users.put("Anas","Taibi");

    }

    public boolean CorrectUser(String user) {
        this.user = user;
        return this.users.containsKey(user);
    }

    public boolean CorrectPass(String pass) {
        this.password = pass;
        return this.users.get(this.user).equals(pass);
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
