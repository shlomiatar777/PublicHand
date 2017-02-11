package Logic;

/**
 * Created by Shlomi on 29/01/2017.
 */

public class User {
    private String firstName;
    private String lasttName;
    private String phone;
    private String location;
    private String userName;
    private String password;


    public User(String firstName, String lasttName, String location, String phone, String userName, String password) {
        this.firstName = firstName;
        this.lasttName = lasttName;
        this.location = location;
        this.phone = phone;
        this.userName = userName;
        this.password = password;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLasttName() {
        return lasttName;
    }

    public void setLasttName(String lasttName) {
        this.lasttName = lasttName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
