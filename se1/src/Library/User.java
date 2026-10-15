package Library;

import java.io.Serializable;

abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String username;
    private String password;
    private String fullName;

    protected User(int id, String username, String password, String fullName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName == null ? "" : fullName.trim();
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName == null ? "" : fullName.trim();
    }

    public abstract String getRoleName();

    public boolean checkPassword(String raw) {
        return raw != null && raw.equals(password);
    }

    String getPasswordForStorage() {
        return password;
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + username
                + (fullName.isEmpty() ? "" : " (" + fullName + ")")
                + " — " + getRoleName();
    }
}
