package tn.esprit.pifirst.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, String> codes = new ConcurrentHashMap<>();

    // ── Couche 1 : domaines grand public interdits ──────────────────────────
    private static final List<String> DOMAINES_INTERDITS = List.of(
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com",
            "icloud.com", "live.com", "msn.com", "protonmail.com",
            "yopmail.com", "mail.com", "gmx.com", "aol.com"
    );

    // ── Couche 2 : base locale universités tunisiennes ──────────────────────
    private static final Map<String, String> UNIVERSITES_TUNISIE = Map.ofEntries(
            // ── Universités publiques ──
            Map.entry("utm.tn",           "Université de Tunis El Manar"),
            Map.entry("enit.utm.tn",      "École Nationale d'Ingénieurs de Tunis"),
            Map.entry("fst.utm.tn",       "Faculté des Sciences de Tunis"),
            Map.entry("fmed.utm.tn",      "Faculté de Médecine de Tunis"),
            Map.entry("ip.tn",            "Institut Préparatoire de Tunis"),
            Map.entry("rnu.tn",           "Réseau National Universitaire"),
            Map.entry("ensi.rnu.tn",      "École Nationale des Sciences de l'Informatique"),
            Map.entry("insat.rnu.tn",     "Institut National des Sciences Appliquées de Tunis"),
            Map.entry("enis.rnu.tn",      "École Nationale d'Ingénieurs de Sfax"),
            Map.entry("isitcom.rnu.tn",   "ISITCOM Hammam Sousse"),
            Map.entry("iset.rnu.tn",      "Institut Supérieur des Études Technologiques"),
            Map.entry("fsegs.rnu.tn",     "Faculté des Sciences Économiques de Sfax"),
            Map.entry("flsh.rnu.tn",      "Faculté des Lettres et Sciences Humaines"),
            Map.entry("ihec.rnu.tn",      "Institut des Hautes Études Commerciales"),
            Map.entry("uvt.rnu.tn",       "Université Virtuelle de Tunis"),
            Map.entry("uu.tn",            "Université de Tunis"),
            Map.entry("mu.tn",            "Université de la Manouba"),
            Map.entry("uc.rnu.tn",        "Université de Carthage"),
            Map.entry("us.rnu.tn",        "Université de Sousse"),
            Map.entry("usf.rnu.tn",       "Université de Sfax"),
            Map.entry("ub.rnu.tn",        "Université de Bizerte"),
            Map.entry("ug.rnu.tn",        "Université de Gabès"),
            Map.entry("ugaf.rnu.tn",      "Université de Gafsa"),
            Map.entry("uma.rnu.tn",       "Université de Monastir"),
            Map.entry("uz.rnu.tn",        "Université de Jendouba"),
            Map.entry("uk.rnu.tn",        "Université de Kairouan"),
            // ── Grandes écoles publiques ──
            Map.entry("supcom.tn",        "École Supérieure des Communications de Tunis"),
            Map.entry("ept.rnu.tn",       "École Polytechnique de Tunisie"),
            Map.entry("ema.rnu.tn",       "École des Mines et de la Métallurgie"),
            Map.entry("isg.rnu.tn",       "Institut Supérieur de Gestion"),
            Map.entry("esct.tn",          "École Supérieure de Commerce de Tunis"),
            Map.entry("ipeit.rnu.tn",     "Institut Préparatoire aux Études d'Ingénieurs"),
            // ── Universités privées reconnues ──
            Map.entry("esprit.tn",        "ESPRIT — École Supérieure Privée d'Ingénierie"),
            Map.entry("tek-up.tn",        "TEK-UP University"),
            Map.entry("sesame.tn",        "SESAME University"),
            Map.entry("tunis-business-school.tn", "Tunis Business School"),
            Map.entry("isimg.tn",         "ISIMG"),
            Map.entry("polytechsf.tn",    "Polytechnique Sfax"),
            Map.entry("atast.tn",         "ATAST"),
            Map.entry("isamm.tn",         "ISAMM"),
            Map.entry("fsb.tn",           "Faculté des Sciences de Bizerte (privée)"),
            Map.entry("uct.tn",           "Université Centrale de Tunis"),
            Map.entry("univers.tn",       "Université Univers"),
            Map.entry("ihe.tn",           "Institut des Hautes Études"),
            Map.entry("tunis-el-manar.tn","Université Tunis El Manar (alt)"),
            // ── Universités arabes / internationales en Tunisie ──
            Map.entry("zu.edu.tn",        "Université Zitouna"),
            Map.entry("acu.tn",           "Arab Community University")
    );

    // ── Point d'entrée principal ────────────────────────────────────────────
    public void envoyerCodeVerification(String emailUniversitaire) {

        String domaine = emailUniversitaire
                .substring(emailUniversitaire.indexOf("@") + 1)
                .toLowerCase();

        // Couche 1 : refus immédiat domaine grand public
        if (DOMAINES_INTERDITS.contains(domaine)) {
            throw new RuntimeException(
                    "L'email doit être un email universitaire, pas @" + domaine);
        }

        // Couche 2 : vérification base locale tunisienne
        boolean reconnuLocalement = estDansBaseTunisienne(domaine);

        // Couche 3 : si pas trouvé localement → Hipolabs en fallback
        boolean reconnuHipolabs = false;
        if (!reconnuLocalement) {
            reconnuHipolabs = verifierViaHipolabs(domaine);
        }

        if (!reconnuLocalement && !reconnuHipolabs) {
            throw new RuntimeException(
                    "Le domaine @" + domaine + " n'est pas reconnu comme universitaire. " +
                            "Utilisez votre email académique officiel.");
        }

        // Log pour debug
        String source = reconnuLocalement ? "base locale" : "Hipolabs";
        System.out.println("✅ Domaine @" + domaine + " reconnu via " + source +
                (reconnuLocalement ? " (" + UNIVERSITES_TUNISIE.get(domaine) + ")" : ""));

        // Génération + stockage du code
        String code = String.format("%06d", new Random().nextInt(999999));
        codes.put(emailUniversitaire, code);

        // Envoi de l'email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailUniversitaire);
        message.setSubject("Vérification de votre email universitaire");
        message.setText(
                "Bonjour,\n\n" +
                        "Votre code de vérification est : " + code + "\n\n" +
                        "Entrez ce code dans le formulaire d'inscription.\n\n" +
                        "Ce code est à usage unique.\n\n" +
                        "Si vous n'avez pas demandé ce code, ignorez cet email."
        );
        mailSender.send(message);
    }

    // ── Vérification code saisi ─────────────────────────────────────────────
    public boolean verifierCode(String emailUniversitaire, String code) {
        String codeStocke = codes.get(emailUniversitaire);
        if (codeStocke != null && codeStocke.equals(code)) {
            codes.remove(emailUniversitaire);
            return true;
        }
        return false;
    }

    // ── Couche 2 : recherche dans la base locale ────────────────────────────
    private boolean estDansBaseTunisienne(String domaine) {
        // Vérification exacte
        if (UNIVERSITES_TUNISIE.containsKey(domaine)) return true;

        // Vérification domaine parent (ex: nada@info.esprit.tn → esprit.tn)
        String[] parts = domaine.split("\\.");
        if (parts.length > 2) {
            for (int i = 1; i < parts.length - 1; i++) {
                String parent = String.join(".",
                        java.util.Arrays.copyOfRange(parts, i, parts.length));
                if (UNIVERSITES_TUNISIE.containsKey(parent)) return true;
            }
        }
        return false;
    }

    // ── Couche 3 : Hipolabs en fallback ────────────────────────────────────
    private boolean verifierViaHipolabs(String domaine) {
        try {
            if (appelHipolabs(domaine)) return true;

            // Essai avec domaine parent
            String[] parts = domaine.split("\\.");
            if (parts.length > 2) {
                String parent = parts[parts.length - 2] + "." + parts[parts.length - 1];
                if (appelHipolabs(parent)) return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("⚠️ Hipolabs indisponible: " + e.getMessage());
            // Fail open si Hipolabs est down
            return false;
        }
    }

    private boolean appelHipolabs(String domaine) throws Exception {
        String urlStr = "http://universities.hipolabs.com/search?domain=" + domaine;
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();

        return !response.toString().trim().equals("[]");
    }
}