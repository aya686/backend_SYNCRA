package com.example.projetpi.service;
import com.example.projetpi.entity.ArticleSante;
import com.example.projetpi.repository.ArticleSanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
@Service
public class ArticleSanteService {
    @Autowired
    private ArticleSanteRepository articleSanteRepository;
    public ArticleSante create(ArticleSante articleSante) {
        return articleSanteRepository.save(articleSante);
    }
    public List<ArticleSante> findAll() {
        return articleSanteRepository.findAll();
    }
    public Optional<ArticleSante> findById(Long id) {
        return articleSanteRepository.findById(id);
    }
    public ArticleSante update(Long id, ArticleSante articleSanteDetails) {
        Optional<ArticleSante> optionalArticle = articleSanteRepository.findById(id);
        if (optionalArticle.isPresent()) {
            ArticleSante article = optionalArticle.get();
            article.setAuteur(articleSanteDetails.getAuteur());
            article.setContenu(articleSanteDetails.getContenu());
            article.setDatePublication(articleSanteDetails.getDatePublication());
            article.setTags(articleSanteDetails.getTags());
            return articleSanteRepository.save(article);
        }
        return null;
    }
    public void delete(Long id) {
        articleSanteRepository.deleteById(id);
    }
}
