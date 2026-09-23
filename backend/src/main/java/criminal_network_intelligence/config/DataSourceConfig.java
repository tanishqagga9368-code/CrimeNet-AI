package criminal_network_intelligence.config;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
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
        ResolvedDataSourceConfig config = resolveConfiguration();

        log.info("Resolved DataSource configuration: URL={}, Username={}", sanitizeUrl(config.jdbcUrl), config.username);

        // If the configuration points to PostgreSQL (local or cloud), test connectivity before locking in Hikari
        if (config.jdbcUrl.startsWith("jdbc:postgresql:")) {
            boolean reachable = testPostgresConnectivity(config.jdbcUrl, config.username, config.password);
            if (!reachable) {
                log.warn("PostgreSQL at {} is not reachable with provided credentials. Falling back to embedded H2 database to ensure continuous operation.", sanitizeUrl(config.jdbcUrl));
                return createH2DataSource();
            }
            return createPostgreSqlDataSource(config);
        }

        if (config.jdbcUrl.startsWith("jdbc:h2:")) {
            return createH2DataSource();
        }

        // Default: try creating requested DataSource
        return createPostgreSqlDataSource(config);
    }

    private HikariDataSource createPostgreSqlDataSource(ResolvedDataSourceConfig config) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(config.jdbcUrl);
        ds.setUsername(config.username);
        ds.setPassword(config.password);
        ds.setConnectionTimeout(10000);
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(2);
        ds.setPoolName("CrimeNet-Postgres-Pool");
        return ds;
    }

    private HikariDataSource createH2DataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setJdbcUrl("jdbc:h2:mem:crimenet;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setConnectionTimeout(5000);
        ds.setMaximumPoolSize(5);
        ds.setPoolName("CrimeNet-H2-Fallback-Pool");
        log.info("Initialized embedded H2 PostgreSQL-compatible in-memory DataSource.");
        return ds;
    }

    private boolean testPostgresConnectivity(String jdbcUrl, String user, String pass) {
        try {
            // Short socket/login check so Spring Boot startup never hangs
            DriverManager.setLoginTimeout(3);
            try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass)) {
                return conn.isValid(2);
            }
        } catch (Exception e) {
            log.warn("PostgreSQL validation check failed for {}: {}", sanitizeUrl(jdbcUrl), e.getMessage());
            return false;
        }
    }

    private ResolvedDataSourceConfig resolveConfiguration() {
        // Priority for environment variables:
        // 1. SPRING_DATASOURCE_URL
        // 2. DATABASE_URL
        // 3. POSTGRES_URL
        // 4. POSTGRES_PRISMA_URL
        // 5. configuredUrl from application.properties
        String rawUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (rawUrl == null || rawUrl.isBlank()) rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) rawUrl = System.getenv("POSTGRES_URL");
        if (rawUrl == null || rawUrl.isBlank()) rawUrl = System.getenv("POSTGRES_PRISMA_URL");
        if (rawUrl == null || rawUrl.isBlank()) rawUrl = configuredUrl;

        String effUsername = this.username;
        String effPassword = this.password;

        String envUser = System.getenv("SPRING_DATASOURCE_USERNAME");
        if (envUser == null || envUser.isBlank()) envUser = System.getenv("POSTGRES_USER");
        if (envUser != null && !envUser.isBlank()) effUsername = envUser.trim();

        String envPass = System.getenv("SPRING_DATASOURCE_PASSWORD");
        if (envPass == null || envPass.isBlank()) envPass = System.getenv("POSTGRES_PASSWORD");
        if (envPass != null && !envPass.isBlank()) effPassword = envPass.trim();

        if (rawUrl != null && !rawUrl.isBlank()) {
            rawUrl = rawUrl.trim();

            // Extract credentials and convert standard postgres:// or postgresql:// URI
            if (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://")) {
                try {
                    URI uri = new URI(rawUrl);
                    String userInfo = uri.getUserInfo();
                    if (userInfo != null && !userInfo.isBlank()) {
                        String[] parts = userInfo.split(":", 2);
                        effUsername = parts[0];
                        if (parts.length > 1) {
                            effPassword = parts[1];
                        }
                    }

                    String host = uri.getHost();
                    int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                    String path = uri.getPath();
                    String query = uri.getQuery();

                    String jdbc = "jdbc:postgresql://" + host + ":" + port + path;
                    if (query != null && !query.isBlank()) {
                        jdbc += "?" + query;
                    } else if (!"localhost".equalsIgnoreCase(host) && !"127.0.0.1".equalsIgnoreCase(host)) {
                        jdbc += "?sslmode=require";
                    }
                    rawUrl = jdbc;
                } catch (Exception e) {
                    rawUrl = rawUrl.replaceFirst("^(postgres|postgresql)://", "jdbc:postgresql://");
                }
            }

            return new ResolvedDataSourceConfig(rawUrl, effUsername, effPassword);
        }

        // If no URL configured at all, check if local postgres exists; otherwise H2
        if (isLocalPostgresListening()) {
            return new ResolvedDataSourceConfig("jdbc:postgresql://localhost:5432/criminal_network_intelligence", effUsername, effPassword);
        }

        return new ResolvedDataSourceConfig("jdbc:h2:mem:crimenet;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH", "sa", "");
    }

    private boolean isLocalPostgresListening() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", 5432), 300);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String sanitizeUrl(String url) {
        if (url == null) return "null";
        return url.replaceAll(":[^/@:]+@", ":***@");
    }

    private static class ResolvedDataSourceConfig {
        final String jdbcUrl;
        final String username;
        final String password;

        ResolvedDataSourceConfig(String jdbcUrl, String username, String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }
    }
}
