import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

class UserData {
    private String username;
    private String passwordHash;
    private String role;

    public UserData(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.passwordHash = hashPassword(password);
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean checkPassword(String password) {
        return hashPassword(password).equals(passwordHash);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 32) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String validateRegistration(String username, String password) {
        if (username.isEmpty()) {
            return "Username cannot be empty";
        }
        if (password.isEmpty()) {
            return "Password cannot be empty";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one digit";
        }
        if (!password.matches(".*[!@#$%^&*].*")) {
            return "Password must contain at least one special character";
        }
        if (username.contains("'") || username.contains("\"") || username.contains(";")) {
            return "Username contains illegal characters (', \", ;)";
        }
        if (username.length() > 20) {
            return "Username must be shorter than 20 characters";
        }
        return null;
    }

    public void setRole(String string) {
    }

}
