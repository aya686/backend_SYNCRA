package tn.esprit.ms2.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnalyseIAResponse {
    private String messagePersonnalise;
    private int scoreRisque;
    private java.util.List<String> actionsConcretes;
    private boolean bloquante;
    private String typeAlerte;
}