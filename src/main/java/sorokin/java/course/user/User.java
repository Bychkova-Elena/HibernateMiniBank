package sorokin.java.course.user;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import sorokin.java.course.account.Account;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", unique = true, nullable = false)
    private String login;

    @OneToMany(mappedBy = "user")
    private List<Account> accountList = new ArrayList<>();

    public User(String login, List<Account> accountList) {
        this.login = login;
        this.accountList = accountList;
    }

    public User() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<Account> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", accountList=" + accountList +
                '}';
    }
}
