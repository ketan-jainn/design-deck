package dev.designdeck.api.repository;

import dev.designdeck.api.config.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PasswordResetTokenRepositoryTest extends PostgresTestContainer {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PasswordResetTokenRepository repository;

    @Test
    void injectedComponentsAreNotNull(){
        assertThat(entityManager).isNotNull();
        assertThat(repository).isNotNull();
    }
}
