package fr.mohamed.test.sqlinjection;

import org.h2.jdbc.JdbcSQLSyntaxErrorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

// Déclaration du package et des imports nécessaires.

@ExtendWith(SpringExtension.class)
@SpringBootTest
class TestSqlInjectionJavaApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(TestSqlInjectionJavaApplicationTests.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Test pour vérifier que le contexte Spring se charge correctement.
    @Test
    void contextLoads() {
    }

    // Test pour vérifier l'ouverture d'une connexion JDBC.
    @Test
    void openJavaSqlConnection() {
        try (Connection cnx = dataSource.getConnection()) {
            assertThat(cnx).isNotNull(); // Vérifie que la connexion n'est pas nulle.
            assertThat(cnx.isClosed()).isFalse(); // Vérifie que la connexion est ouverte.
        } catch (SQLException e) {
            log.error(e.getMessage(), e); // Log en cas d'exception SQL.
        }
    }

    // Test pour exécuter une requête SQL simple avec Spring JdbcTemplate.
    @Test
    void selectQuerySpring() {
        String sql = "select id, name, password from \"user\"";
        List<String> results =
                jdbcTemplate.query(sql,
                        createSingleStringRowMapper());
        assertThat(results).isNotEmpty(); // Vérifie que les résultats ne sont pas vides.

        results.forEach(log::debug); // Log chaque résultat.
    }

    // Test pour exécuter une requête SQL avec une restriction sur le nom.
    @Test
    void selectQuerySpringWithRestriction() {
        String param = "Donald";
        String sql = "select id, name, password from \"user\" where name = '" + param + "'";
        List<String> results =
                jdbcTemplate.query(sql,
                        createSingleStringRowMapper());
        assertThat(results).hasSize(1); // Vérifie qu'un seul résultat est retourné.

        results.forEach(log::debug); // Log chaque résultat.
    }

    // Test pour vérifier que la requête échoue à cause d'une mauvaise gestion des caractères spéciaux.
    @Test
    void selectQuerySpringWithRestrictionFailingBecauseNotSanitized() {
        String param = "Dona'ld";
        String sql = "select id, name, password from \"user\" where name = '" + param + "'";
        Throwable thrown = catchThrowable(() -> {
            List<String> results =
                    jdbcTemplate.query(sql,
                            createSingleStringRowMapper());
        });
        assertThat(thrown)
                .isInstanceOf(BadSqlGrammarException.class); // Vérifie que l'exception levée est une BadSqlGrammarException.
        Throwable cause = thrown.getCause();
        assertThat(cause)
                .isInstanceOf(JdbcSQLSyntaxErrorException.class); // Vérifie que la cause de l'exception est une JdbcSQLSyntaxErrorException.
    }

    // Test pour montrer une injection SQL réussie.
    @Test
    void selectQuerySpringWithRestrictionHackedBySqlInjection() {
        String param = "likju' or '1'='1";
        String sql = "select id, name, password from \"user\" where name = '" + param + "'";
        List<String> results =
                jdbcTemplate.query(sql,
                        createSingleStringRowMapper());
        assertThat(results.size()).isGreaterThanOrEqualTo(4); // Vérifie que plusieurs résultats sont retournés (4 ou plus).

        results.forEach(log::debug); // Log chaque résultat.
    }

    // Test pour exécuter une requête SQL avec un PreparedStatement pour éviter les injections SQL.
    @Test
    void selectQuerySpringWithRestrictionUsingPreparedStatement() {
        String param = "Donald";
        String sql = "select id, name, password from \"user\" where name = ?";
        List<String> results =
                jdbcTemplate.query(sql, new Object[]{param},
                        createSingleStringRowMapper());
        assertThat(results).hasSize(1); // Vérifie qu'un seul résultat est retourné.

        results.forEach(log::debug); // Log chaque résultat.
    }

    // Test pour exécuter une requête SQL avec un PreparedStatement et vérifier l'échappement des caractères spéciaux.
    @Test
    void selectQuerySpringWithRestrictionUsingPreparedStatementSanitized() {
        String param = "Dona'ld"; // Transformed to "Dona''ld"
        String sql = "select id, name, password from \"user\" where name = ?";
        List<String> results =
                jdbcTemplate.query(sql, new Object[]{param},
                        createSingleStringRowMapper());
        assertThat(results).hasSize(1); // Vérifie qu'un seul résultat est retourné.
        assertThat(results.get(0)).startsWith("5 - Dona'ld -"); // Vérifie que le résultat contient le nom correctement échappé.

        results.forEach(log::debug); // Log chaque résultat.
    }

    // Test pour vérifier qu'une tentative d'injection SQL échoue avec un PreparedStatement.
    @Test
    void selectQuerySpringWithRestrictionUsingPreparedStatementTryingSqlInjection() {
        String param = "xxx' or '1'='1";
        String sql = "select id, name, password from \"user\" where name = ?";
        List<String> results =
                jdbcTemplate.query(sql, new Object[]{param},
                        createSingleStringRowMapper());
        assertThat(results).isEmpty(); // Vérifie qu'aucun résultat n'est retourné.

        results.forEach(log::debug); // Log chaque résultat.
    }

    // Méthode utilitaire pour créer un RowMapper qui convertit une ligne de résultat en une chaîne formatée.
    private static RowMapper<String> createSingleStringRowMapper() {
        return (rs, rowNum) -> String.format("%d - %s - %s",
                rs.getInt(1),
                rs.getString(2),
                rs.getString(3));
    }

}
