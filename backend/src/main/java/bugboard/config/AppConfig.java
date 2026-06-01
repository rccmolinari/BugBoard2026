package bugboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/*
 * DIP — il BCryptPasswordEncoder è dichiarato come @Bean e iniettato
 * tramite Spring, invece di essere istanziato con `new` nelle singole classi.
 * AuthService e UserService non devono più dipendere da un dettaglio di
 * costruzione; dipendono entrambi dall'astrazione iniettata.
 */
@Configuration
public class AppConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
