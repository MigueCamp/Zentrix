package com.zentrix.it;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base de las pruebas de integración (docs/07 §1): arranca el contexto Spring completo
 * y ejecuta la API real (vía {@link RestTestClient}) contra un SQL Server real.
 *
 * <p>Fuente de datos:
 * <ul>
 *   <li><b>CI / por defecto:</b> un SQL Server efímero gestionado por Testcontainers.</li>
 *   <li><b>Local:</b> si se define {@code IT_DATASOURCE_URL} (env) o {@code -Dit.datasource.url},
 *       se usa ese SQL Server existente — útil en entornos con poca memoria.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    private static MSSQLServerContainer<?> container;

    /** Lee primero variable de entorno (heredada por el JVM forkeado de failsafe) y luego system property. */
    private static String override(String envKey, String propKey, String defaultValue) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return System.getProperty(propKey, defaultValue);
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        String localUrl = override("IT_DATASOURCE_URL", "it.datasource.url", null);
        final String url;
        final String username;
        final String password;

        if (localUrl != null && !localUrl.isBlank()) {
            url = localUrl;
            username = override("IT_DATASOURCE_USERNAME", "it.datasource.username", "sa");
            password = override("IT_DATASOURCE_PASSWORD", "it.datasource.password", "");
        } else {
            if (container == null) {
                container = new MSSQLServerContainer<>(
                        DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-latest"))
                        .acceptLicense();
                container.start();
            }
            url = container.getJdbcUrl();
            username = container.getUsername();
            password = container.getPassword();
        }

        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> username);
        registry.add("spring.datasource.password", () -> password);
    }

    @LocalServerPort
    protected int port;

    protected RestTestClient client;
    protected final JsonMapper mapper = JsonMapper.builder().build();

    protected static final String BOOTSTRAP_EMAIL = "it-admin@zentrix.local";
    protected static final String BOOTSTRAP_PASSWORD = "It_Admin_2026!";

    @BeforeEach
    void initClient() {
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    protected EntityExchangeResult<String> get(String path, String token) {
        return client.get().uri(path)
                .headers(h -> h.setBearerAuth(token))
                .exchange().expectBody(String.class).returnResult();
    }

    protected EntityExchangeResult<String> post(String path, String body, String token) {
        var spec = client.post().uri(path).contentType(MediaType.APPLICATION_JSON).body(body);
        if (token != null) {
            spec = spec.headers(h -> h.setBearerAuth(token));
        }
        return spec.exchange().expectBody(String.class).returnResult();
    }

    /** Inicia sesión y devuelve el accessToken (JWT). */
    protected String login(String email, String password) {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        String response = post("/auth/login", body, null).getResponseBody();
        return mapper.readTree(response).get("accessToken").asText();
    }
}
