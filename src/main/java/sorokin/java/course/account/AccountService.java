package sorokin.java.course.account;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;
import sorokin.java.course.helper.TransactionHelper;
import sorokin.java.course.user.User;

import java.util.*;

@Component
public class AccountService {

    private final AccountProperties accountProperties;
    private final SessionFactory sessionFactory;
    private final TransactionHelper transactionHelper;

    public AccountService(
            AccountProperties accountProperties,
            SessionFactory sessionFactory,
            TransactionHelper transactionHelper
    ) {
        this.accountProperties = accountProperties;
        this.sessionFactory = sessionFactory;
        this.transactionHelper = transactionHelper;
    }

    public Account createAccount(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }

        Account newAccount = new Account(user, accountProperties.getDefaultAmount());

        return transactionHelper.executeInTransaction(session -> {
            session.persist(newAccount);
            return newAccount;
        });
    }

    public void withdraw(Long fromAccountId, Integer amount) {
        validatePositiveId(fromAccountId, "account id");
        validatePositiveAmount(amount);

        transactionHelper.executeInTransaction(session -> {
            Account account = findAccountById(session, fromAccountId);

            if (amount > account.getMoneyAmount()) {
                throw new IllegalArgumentException(
                        "insufficient funds on account id=%s, moneyAmount=%s, attempted withdraw=%s"
                                .formatted(account.getId(), account.getMoneyAmount(), amount)
                );
            }
            account.setMoneyAmount(account.getMoneyAmount() - amount);
            session.merge(account);
        });
    }

    public void deposit(Long toAccountId, Integer amount) {
        validatePositiveId(toAccountId, "account id");
        validatePositiveAmount(amount);

        transactionHelper.executeInTransaction(session -> {
            Account account = findAccountById(session, toAccountId);

            account.setMoneyAmount(account.getMoneyAmount() + amount);
            session.merge(account);
        });
    }

    public void closeAccount(Long accountId) {
        validatePositiveId(accountId, "account id");

        transactionHelper.executeInTransaction(session -> {
            Account accountToClose = findAccountById(session, accountId);

            var userId = accountToClose.getUser().getId();
            var userAccounts = getUserAccounts(session, userId);
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
        });
    }

    public void transfer(Long fromAccountId, Long toAccountId, int amount) {
        validatePositiveId(fromAccountId, "source account id");
        validatePositiveId(toAccountId, "target account id");
        validatePositiveAmount(amount);
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("source and target account id must be different");
        }

        transactionHelper.executeInTransaction(session -> {
            Account accountFrom = findAccountById(session, fromAccountId);
            Account accountTo = findAccountById(session, toAccountId);

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
        });
    }

    private Account findAccountById(Session session, Long id) {
        validatePositiveId(id, "account id");

        Account account = session.get(Account.class, id);
        if (account == null) {
            throw new IllegalArgumentException("No such account: id=%s".formatted(id));
        }
        return account;
    }

    private List<Account> getUserAccounts(Session session, Long userId) {
        User user = session.get(User.class, userId);
        return user.getAccountList();
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
