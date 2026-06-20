package bugboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*
 * Dichiaro qui il BCryptPasswordEncoder come bean, così Spring me lo inietta
 * dove serve e non me lo ritrovo creato con `new` sparso per le varie classi
 * (e tutti quanti usano lo stesso identico encoder).
 */
@Configuration
public class AppConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
