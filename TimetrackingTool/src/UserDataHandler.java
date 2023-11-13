import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserDataHandler {
    private Map<String, UserData> users;

    public UserDataHandler() {
        loadUserFile();
    }

    public UserData getUserData(String username) {
        loadUserFile();
        return users.get(username);
    }

    public Map<String, UserData> getAllUsers() {
        loadUserFile();
        return users;
    }

    public void saveUser(UserData userData) {
        loadUserFile();
        users.put(userData.getUsername(), userData);
        saveUserFile();
    }

    public void deleteUser(String username) {
        loadUserFile();
        users.remove(username);
        saveUserFile();
    }

    public boolean checkUsernameExists(String username) {
        loadUserFile();
        return users.containsKey(username);
    }

    public boolean checkPassword(String username, String password) {
        loadUserFile();
        return users.get(username).checkPassword(password);
    }

    public void loadUserFile() {
        try {
            FileWriter writer = new FileWriter("users.csv", true);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error creating user file");
        }

        this.users = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("users.csv"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(",");
                UserData user = new UserData(userData[2]);
                user.setPasswordHash(userData[1]);
                user.setUsername(userData[0]);
                users.put(userData[0], user);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading user file");
        }
    }

    public void saveUserFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("users.csv"));
            for (UserData user : users.values()) {
                writer.write(user.getUsername() + "," + user.getPasswordHash() + "," + user.getRole() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing user file");
        }
    }

}
