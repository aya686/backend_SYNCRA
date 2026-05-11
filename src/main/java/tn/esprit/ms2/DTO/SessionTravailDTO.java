package tn.esprit.ms2.DTO;

import lombok.Data;

@Data
public class SessionTravailDTO {
    private Long tacheId;
    private Long utilisateurId;
    private Integer niveauCharge;
}