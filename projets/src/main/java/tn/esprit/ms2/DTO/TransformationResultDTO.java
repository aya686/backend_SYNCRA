package tn.esprit.ms2.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import tn.esprit.ms2.entities.Idee;
import tn.esprit.ms2.entities.Projet;

@Data
@AllArgsConstructor
public class TransformationResultDTO {
    private Idee idee;
    private Projet projet;
}