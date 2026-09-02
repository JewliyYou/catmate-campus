package com.catmate;

import static org.assertj.core.api.Assertions.assertThat;
import com.catmate.auth.PasswordService;
import com.catmate.user.UserAccount;
import com.catmate.user.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:catmate-production;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
    "catmate.seed-demo-users=false",
    "catmate.initial-admin.username=deploy_admin",
    "catmate.initial-admin.password=test-only-password",
    "catmate.initial-admin.display-name=部署管理员"
})
class ProductionInitializationTests {
    @Autowired UserAccountRepository users;
    @Autowired PasswordService passwords;

    @Test void createsOnlyConfiguredAdminWhenDemoUsersAreDisabled(){
        assertThat(users.findByUsername("user")).isEmpty();
        assertThat(users.findByUsername("admin")).isEmpty();
        UserAccount admin=users.findByUsername("deploy_admin").orElseThrow();
        assertThat(admin.getRole()).isEqualTo(UserAccount.Role.ADMIN);
        assertThat(passwords.matches("test-only-password",admin.getPasswordHash())).isTrue();
    }
}
