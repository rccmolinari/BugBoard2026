package bugboard;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import bugboard.service.IssueService;

@SpringBootApplication
public class BugBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(BugBoardApplication.class, args);
    }

    @Bean
    public CommandLineRunner demo(IssueService issueService) {
        return (args) -> {
            boolean assigned = issueService.assignIssueToUser(1, "mimmo@gmail.com", "admin@admin.com");
            if (assigned) {
                System.out.println("Assegnazione completata!");
            } else {
                System.out.println("Assegnazione fallita: issue o utenti non trovati.");
            }
        };
    }
}
