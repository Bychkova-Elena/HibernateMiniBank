package sorokin.java.course.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.context.annotation.Bean;
import sorokin.java.course.account.Account;
import sorokin.java.course.user.User;

@org.springframework.context.annotation.Configuration
public class HibernateConfiguration {
    @Bean
    public SessionFactory sessionFactory() {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(Account.class);
        configuration.addAnnotatedClass(User.class);

        // Настройки подключения
        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/postgres");
        configuration.setProperty("hibernate.connection.username", "postgres");
        configuration.setProperty("hibernate.connection.password", "root");
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.current_session_context_class", "thread");

        // Чтобы видеть SQL из Hibernate
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");

        return configuration.buildSessionFactory();
    }
}
