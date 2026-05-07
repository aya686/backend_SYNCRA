package com.synchub.ms6.model;

/**
 * Énumération des intentions reconnues par le chatbot NLP
 * Chaque intention représente une action possible que l'utilisateur veut effectuer
 */
public enum ChatbotIntent {
    SEARCH_PRODUCT("Recherche de produits"),
    CHECK_PRICE("Vérification de prix"),
    TRACK_ORDER("Suivi de commande"),
    GET_RECOMMENDATION("Demande de recommandations"),
    CHECK_STOCK("Vérification de stock"),
    APPLY_PROMO("Demande de code promotionnel"),
    COMPLAINT("Réclamation ou problème"),
    GREETING("Salutation"),
    THANKS("Remerciement"),
    GOODBYE("Au revoir"),
    HELP("Demande d'aide"),
    UNKNOWN("Intention non reconnue");

    private final String description;

    ChatbotIntent(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
