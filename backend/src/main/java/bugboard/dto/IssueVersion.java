package bugboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Riga ultra-leggera per il polling admin: solo id e version. Il client la
// confronta con quello che ha già e si va a riprendere (via /righe) solo le
// issue nuove o cambiate, invece di riscaricare tutta la lista a ogni giro.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueVersion {
    private Integer id;
    private Long version;
}
