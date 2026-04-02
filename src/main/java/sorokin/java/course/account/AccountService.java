package sorokin.java.course.account;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.user.User;

import java.util.*;

@Component
public class AccountService {

    private final AccountProperties accountProperties;
    private final SessionFactory sessionFactory;

    public AccountService(AccountProperties accountProperties, SessionFactory sessionFactory) {
        this.accountProperties = accountProperties;
        this.sessionFactory = sessionFactory;
    }

    public Account createAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }

        Account newAccount = new Account(user, accountProperties.getDefaultAmount());

        try (Session session = sessionFactory.openSession()) {
            try {
                session.beginTransaction();
                session.persist(newAccount);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw e;
            }
        }

        return newAccount;
    }

    public Account findAccountById(Long id) {
        validatePositiveId(id, "account id");

        try (Session session = sessionFactory.openSession()) {
            Account account = session.get(Account.class, id);
            if (account == null) {
                throw new IllegalArgumentException("No such account: id=%s".formatted(id));
            }
            return account;
        }
    }

    public List<Account> getUserAccounts(Long userId) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.get(User.class, userId);
            return user.getAccountList();
        }
    }

    public void withdraw(Long fromAccountId, Integer amount) {
        validatePositiveId(fromAccountId, "account id");
        validatePositiveAmount(amount);

        try (Session session = sessionFactory.openSession()) {

            try {
                session.beginTransaction();
                Account account = findAccountById(fromAccountId);

                if (amount > account.getMoneyAmount()) {
                    throw new IllegalArgumentException(
                            "insufficient funds on account id=%s, moneyAmount=%s, attempted withdraw=%s"
                                    .formatted(account.getId(), account.getMoneyAmount(), amount)
                    );
                }
                account.setMoneyAmount(account.getMoneyAmount() - amount);
                session.merge(account);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void deposit(Long toAccountId, Integer amount) {
        validatePositiveId(toAccountId, "account id");
        validatePositiveAmount(amount);

        try (Session session = sessionFactory.openSession()) {

            try {
                session.beginTransaction();
                Account account = findAccountById(toAccountId);

                account.setMoneyAmount(account.getMoneyAmount() + amount);
                session.merge(account);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void closeAccount(Long accountId) {
        validatePositiveId(accountId, "account id");

        try (Session session = sessionFactory.openSession()) {

            try {
                session.beginTransaction();
                Account accountToClose = findAccountById(accountId);

                var userId = accountToClose.getUser().getId();
                var userAccounts = getUserAccounts(userId);
                if (userAccounts.size() == 1) {
                    throw new IllegalStateException("Can't close the only one account");
                }

                var accountToTransferMoney = userAccounts.stream()
                        .filter(it -> !Objects.equals(it.getId(), accountId))
                        .findFirst()
                        .orElseThrow();

                var newAmount = accountToTransferMoney.getMoneyAmount() + accountToClose.getMoneyAmount();
                accountToTransferMoney.setMoneyAmount(newAmount);

                session.merge(accountToTransferMoney);
                session.remove(accountToClose);
                accountToClose.getUser().getAccountList().remove(accountToClose);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void transfer(Long fromAccountId, Long toAccountId, int amount) {
        validatePositiveId(fromAccountId, "source account id");
        validatePositiveId(toAccountId, "target account id");
        validatePositiveAmount(amount);
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("source and target account id must be different");
        }

        try (Session session = sessionFactory.openSession()) {

            try {
                session.beginTransaction();
                Account accountFrom = findAccountById(fromAccountId);
                Account accountTo = findAccountById(toAccountId);

                if (amount > accountFrom.getMoneyAmount()) {
                    throw new IllegalArgumentException(
                            "insufficient funds on account id=%s, moneyAmount=%s, attempted transfer=%s"
                                    .formatted(accountFrom.getId(), accountFrom.getMoneyAmount(), amount)
                    );
                }
                accountFrom.setMoneyAmount(accountFrom.getMoneyAmount() - amount);

                int amountToTransfer = accountTo.getUser().getId().equals(accountFrom.getUser().getId())
                        ? amount
                        : (int) Math.round(amount * (1 - accountProperties.getTransferCommission()));
                accountTo.setMoneyAmount(accountTo.getMoneyAmount() + amountToTransfer);
                session.merge(accountFrom);
                session.merge(accountTo);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
                throw e;
            }
        }
    }

    private void validatePositiveId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must be > 0");
        }
    }

    private void validatePositiveAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
    }
}
