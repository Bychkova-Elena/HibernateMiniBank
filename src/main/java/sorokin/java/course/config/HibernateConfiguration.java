package sorokin.java.course.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import sorokin.java.course.account.Account;
import sorokin.java.course.user.User;

@org.springframework.context.annotation.Configuration
@PropertySource("classpath:application.properties")
public class HibernateConfiguration{
    @Bean
    public SessionFactory sessionFactory(
            @Value("${db.driver}") String driver,
            @Value("${db.url}") String url,
            @Value("${db.username}") String username,
            @Value("${db.password}") String password,
            @Value("${db.dialect}") String dialect,
            @Value("${hibernate.hbm2ddl.auto}") String hibernateAuto,
            @Value("${hibernate.current_session_context_class}") String contextClass,
            @Value("${hibernate.show_sql}") String showSql,
            @Value("${hibernate.format_sql}") String formatSql
    ) {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(Account.class);
        configuration.addAnnotatedClass(User.class);

        // Настройки подключения
        configuration.setProperty("hibernate.connection.driver_class", driver);
        configuration.setProperty("hibernate.connection.url", url);
        configuration.setProperty("hibernate.connection.username", username);
        configuration.setProperty("hibernate.connection.password", password);
        configuration.setProperty("hibernate.dialect", dialect);
        configuration.setProperty("hibernate.hbm2ddl.auto", hibernateAuto);
        configuration.setProperty("hibernate.current_session_context_class", contextClass);

        // Чтобы видеть SQL из Hibernate
        configuration.setProperty("hibernate.show_sql", showSql);
        configuration.setProperty("hibernate.format_sql", formatSql);

        return configuration.buildSessionFactory();
    }
}
