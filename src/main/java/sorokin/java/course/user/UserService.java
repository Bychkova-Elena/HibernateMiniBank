package sorokin.java.course.user;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.account.Account;
import sorokin.java.course.account.AccountProperties;

import java.util.*;

@Component
public class UserService {

    private final AccountProperties accountProperties;
    private final SessionFactory sessionFactory;

    public UserService(AccountProperties accountProperties, SessionFactory sessionFactory) {
        this.accountProperties = accountProperties;
        this.sessionFactory = sessionFactory;
    }

    public User createUser(String login) {
        User user = new User(login.trim());
        Account account = new Account(user, accountProperties.getDefaultAmount());

        try (Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();
                session.persist(user);
                session.persist(account);
                user.getAccountList().add(account);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw e;
            }
        }

        return user;
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
}
