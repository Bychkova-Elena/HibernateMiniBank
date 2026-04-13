package sorokin.java.course.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.account.Account;
import sorokin.java.course.account.AccountProperties;
import sorokin.java.course.helper.TransactionHelper;

import java.util.*;

@Component
public class UserService {

    private final AccountProperties accountProperties;
    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;
    private final Set<String> takenLogins;

    public UserService(AccountProperties accountProperties,
                       SessionFactory sessionFactory,
                       TransactionHelper transactionHelper) {
        this.accountProperties = accountProperties;
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
        this.takenLogins = new HashSet<>(getUsersLogins());
    }

    public User createUser(String login) {
        String normalizedLogin = validateLogin(login);
        if (takenLogins.contains(normalizedLogin)) {
            throw new IllegalArgumentException("User already exists with login=%s".formatted(normalizedLogin));
        }
        User user = new User(normalizedLogin);
        Account account = new Account(user, accountProperties.getDefaultAmount());

        return transactionHelper.executeInTransaction(session -> {
            session.persist(user);
            session.persist(account);
            user.getAccountList().add(account);
            takenLogins.add(normalizedLogin);
            return user;
        });
    }

    public User findUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("user id must be > 0");
        }

        try (Session session = sessionFactory.openSession()) {
            var user = session.get(User.class, id);
            if (user == null) {
                throw new IllegalArgumentException("No such user with id=%s".formatted(id));
            }
            return user;
        }
    }


    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.accountList", User.class)
                    .getResultList();
        }
    }

    private String validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("login must not be blank");
        }
        return login.trim();
    }

    private List<String> getUsersLogins() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("SELECT u.login FROM User u", String.class)
                    .list();
        }
    }
}
