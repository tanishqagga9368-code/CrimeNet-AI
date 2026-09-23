package criminal_network_intelligence.config;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:}")
    private String configuredUrl;

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:postgres}")
    private String password;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = resolveJdbcUrl(configuredUrl);

        log.info("Configuring primary DataSource with URL: {}", sanitizeUrl(url));

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);

        if (url.startsWith("jdbc:h2:")) {
            ds.setDriverClassName("org.h2.Driver");
            ds.setUsername("sa");
            ds.setPassword("");
            ds.setConnectionTimeout(5000);
            ds.setMaximumPoolSize(5);
        } else {
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUsername(username);
            ds.setPassword(password);
            ds.setConnectionTimeout(10000);
            ds.setMaximumPoolSize(10);
            ds.setMinimumIdle(2);
        }

        return ds;
    }

    private String resolveJdbcUrl(String inputUrl) {
        String envUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (envUrl == null || envUrl.isBlank()) {
            envUrl = System.getenv("DATABASE_URL");
        }
        if (envUrl != null && !envUrl.isBlank()) {
            inputUrl = envUrl.trim();
        }

        if (inputUrl != null && !inputUrl.isBlank()) {
            if (inputUrl.startsWith("postgres://") || inputUrl.startsWith("postgresql://")) {
                inputUrl = convertStandardUriToJdbc(inputUrl);
            }
            if (inputUrl.startsWith("jdbc:postgresql://localhost") || inputUrl.startsWith("jdbc:postgresql://127.0.0.1")) {
                if (isLocalPostgresAvailable()) {
                    return inputUrl;
                } else {
                    log.warn("PostgreSQL not detected on localhost:5432 and no external database URL provided. Activating embedded database fallback.");
                    return "jdbc:h2:mem:crimenet;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH";
                }
            }
            return inputUrl;
        }

        if (isLocalPostgresAvailable()) {
            return "jdbc:postgresql://localhost:5432/criminal_network_intelligence";
        }

        log.warn("No database URL provided and localhost:5432 unreachable. Activating embedded database fallback.");
        return "jdbc:h2:mem:crimenet;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH";
    }

    private boolean isLocalPostgresAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 5432), 400);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String convertStandardUriToJdbc(String uriStr) {
        try {
            URI uri = new URI(uriStr);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath();
            return "jdbc:postgresql://" + host + ":" + port + path + "?sslmode=require";
        } catch (Exception e) {
            return uriStr.replaceFirst("^(postgres|postgresql)://", "jdbc:postgresql://");
        }
    }

    private String sanitizeUrl(String url) {
        if (url == null) return "null";
        return url.replaceAll(":[^/@:]+@", ":***@");
    }
}
