import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public class Rekrute {
    public static void main(String[] args) {
        // URL principale
        String baseUrl = "https://www.rekrute.com/offres.html?s=1&p=";
        int totalPages = 134; // Nombre total de pages

        for (int page = 1; page <= totalPages; page++) {
            try {
                // Construction de l'URL de chaque page
                String url = "https://www.rekrute.com/offres.html?s=1&p=" + page + "&o=1&positionId%5B0%5D=13&positionId%5B1%5D=19&positionId%5B2%5D=23";
                // sans filtre &o=1
                Document document = Jsoup.connect(url).get();

                // Sélection des annonces
                Elements jobs = document.select(".post-id");

                for (Element job : jobs) {
                    String JobUrl = "https://www.rekrute.com" + job.select("a.titreJob").attr("href");

                    String jobTitle = job.selectFirst("a.titreJob") != null ? job.selectFirst("a.titreJob").text() : "Non spécifié";
                    String Ville = extractCity(jobTitle);
                    String company = job.selectFirst("a > img") != null ? job.selectFirst("a > img").attr("title") : "Non spécifié";
                    String experience = job.select("li:nth-of-type(3) > a").text();

                    String contrat = job.select("li:nth-of-type(5) > a").text();


                    // Null check for the date
                    String date = "";
                    Element dateElement = job.selectFirst("em.date > span");
                    if (dateElement != null) {
                        date = dateElement.text();
                    } else {
                        System.err.println("Date non trouvée pour le poste : " + jobTitle);
                    }

                    String jobDescription = job.selectFirst("div.info:nth-of-type(2)").text();



                    // Charger la page détaillée
                    Document jobPage = Jsoup.connect(JobUrl).get();

                    String niveau = jobPage.select("ul.featureInfo > li:nth-of-type(3)").text();



                    // Extraire les compétences contenant des mots-clés
                    List<String> skills = extractSkills(jobPage);
                    String skillsString = String.join(", ", skills);

                    String region = jobPage.selectFirst("li[title=\"Région\"]\n") != null ? jobPage.selectFirst("li[title=\"Région\"]\n").text() : "Non spécifié";
                    String adresse = jobPage.select("span#address").text();
                    String DescriptionSociete = jobPage.selectFirst("div#recruiterDescription > p") != null ? jobPage.selectFirst("div#recruiterDescription > p").text() : "Non spécifié";

                    String Profile = jobPage.select("div.contentbloc > div.col-md-12.blc:nth-of-type(5)").text();
                    Elements traits = jobPage.select("div.col-md-12.blc > p > span.tagSkills");
                    Set<String> uniqueTraits = new LinkedHashSet<>(traits.eachText());
                    String TraitPerso = String.join(", ", uniqueTraits);
                    Boolean teleTravail =isTeletravail(jobPage.select("ul.featureInfo:nth-of-type(2) > li:nth-of-type(2)").text()) ;
                    String SecteurActivite = job.select("div.info:nth-of-type(3) > ul > li:nth-of-type(1)").text();
                    String specialite=diplome(Profile) ;
                    // Formater la date
                    String formattedDate = formatDate(date);

                    // Affichage des résultats
                    System.out.println("URL du poste : " + JobUrl);
                    System.out.println("Titre du poste : " + jobTitle);
                    System.out.println("teletravail : "+ teleTravail);

                    System.out.println( SecteurActivite);
                    System.out.println("Description du poste : " + jobDescription);
                    System.out.println("Entreprise : " + company);
                    System.out.println("Expérience : " + experience);
                    System.out.println("Niveau requis : " + niveau);
                    System.out.println("Contrat Proposé : " + contrat);
                    System.out.println( Profile);
                    System.out.println("Traits de personnalité : " + TraitPerso);
                    System.out.println("Région : " + region);
                    System.out.println("Ville : " + Ville);
                    System.out.println("Date de publication : " + formattedDate);
                    System.out.println("Adresse du siège : " + adresse);
                    System.out.println("Description de l'entreprise : " + DescriptionSociete);
                    System.out.println("Compétences :");

                    if (!skills.isEmpty()) {
                        for (String skill : skills) {
                            System.out.println("  - " + skill); // Affiche chaque compétence avec un tiret
                        }
                    } else {
                        System.out.println("  Aucune compétence identifiée.");
                    }
                    Map<String, String> languages = extractLanguages(jobPage);

                    // Si aucune langue trouvée, ajouter Français par défaut avec niveau courant
                    if (languages.isEmpty()) {
                        languages.put("Français", "Courant");
                    }

                    System.out.println("Langues et niveaux :");

                    for (Map.Entry<String, String> entry : languages.entrySet()) {
                        System.out.println("  - Langue : " + entry.getKey() + ", Niveau : " + entry.getValue());
                    }
                    List<String> diplomas = extractDiplomas(jobPage);

                    System.out.println("Diplômes :");
                    if (!diplomas.isEmpty()) {
                        for (String diploma : diplomas) {
                            System.out.println("  - " + diploma); // Affiche chaque diplôme avec un tiret
                        }
                    } else {
                        System.out.println("  Aucun diplôme identifié.");
                    }

                    List<String> softSkills = extractSoftSkills(jobPage);
                    String skillsString2 = String.join(", ", softSkills);

                    System.out.println("sofskills :");

                    if (!softSkills.isEmpty()) {
                        for (String softSkill : softSkills) {
                            System.out.println("  - : " + softSkill); // Affiche chaque soft skill avec un tiret
                        }
                    } else {
                        System.out.println("  Aucune compétence identifiée.");
                    }

                    System.out.println("HardSkills :");

                    List<String> HardSkills = extractHardSkills(jobPage);
                    String HardSkillsString = String.join(", ", HardSkills);

                    if (!HardSkills.isEmpty()) {
                        for (String HardSkill : HardSkills) {
                            System.out.println("  - : " + HardSkill); // Affiche chaque soft skill avec un tiret
                        }
                    } else {
                        System.out.println("  Aucune compétence identifiée.");
                    }
                    System.out.println("----------------------------");

                    // Insérer les données dans la base
                    // Formater les langues et leurs niveaux en une chaîne
                    StringBuilder languageStringBuilder = new StringBuilder();
                    for (Map.Entry<String, String> entry : languages.entrySet()) {
                        languageStringBuilder.append(entry.getKey())
                                .append(" (")
                                .append(entry.getValue())
                                .append("), ");
                    }

                    String languagesString = languageStringBuilder.toString().replaceAll(", $", "");
                    String StringDiplome = String.join(", ", diplomas);


                    if (formattedDate != null) {
                        try {
                            DatabaseConnection bd = new DatabaseConnection();
                            bd.insertJob(jobTitle, JobUrl, "https://www.rekrute.com/", formattedDate, formattedDate, adresse, null,
                                    company, DescriptionSociete, jobDescription, region, Ville, SecteurActivite, null, contrat, niveau, StringDiplome,
                                    experience, Profile, TraitPerso, HardSkillsString, skillsString2, skillsString, languagesString,
                                    null, null, null, teleTravail);
                        } catch (Exception e) {
                            System.err.println("Erreur lors de l'insertion en base de données : " + e.getMessage());
                        }
                    } else {
                        System.err.println("Date invalide pour le poste : " + jobTitle);
                    }

                }
            } catch (IOException e) {
                System.err.println("Erreur de connexion ou de récupération des données sur la page " + page + " : " + e.getMessage());
            }
        }
    }

    // Méthode pour extraire les compétences contenant des mots-clés spécifiques
    private static List<String> extractSkills(Document jobPage) {
        List<String> skills = new ArrayList<>();

        // Pattern regex pour les mots-clés des compétences
        Pattern pattern = Pattern.compile(
                "(?i)(maîtrise|compétences|connaissances|connaissance de|capacité à|expérience en|expérience avec|experience in|experience with|culture|force|skills|knowledge|knowledge of|ability to|expertise|proficiency|" +
                        "curiosité|pragmatisme|agilité|safe|analyse|analyser|comprendre|alerter|communication|autonomie|rigueur|proactivité|organisation|synthèse|écoute|polyvalence|aisance relationnelle|" +
                        "convaincre|argumenter|présenter|adhérer|traduire|collaborer|collaboration|synthétiser|résoudre|proposer)" +
                        "(\\s*(à|en|des|de la|de l'|in|of|to|de)?\\s*[^.\\n]+[.;]?)|" + // Capture le texte jusqu'à un point ou saut de ligne
                        "(Java|Java/J2EE|Python|C\\+\\+|C#|JavaScript|TypeScript|PHP|Rust|Swift|Kotlin|" +
                        "Spring Boot|Hibernate|Angular|React|Vue\\.js|Node\\.js|Django|Flask|Laravel|" +
                        "Docker|Kubernetes|CI/CD|Jenkins|GitLab|Ansible|Terraform|Maven|" +
                        "MySQL|PostgreSQL|MongoDB|Oracle|Redis|SQLite|" +
                        "Selenium|JUnit|Postman|Cucumber|JMeter|Gatling|Robot Framework|" +
                        "API REST|SOAP|Mockito|JIRA|Confluence|Agile|SAFe|Scrum|Leadership|Communication|Soft skills|Teamwork|Problem-solving|" +
                        "gestion agiles|outils collaboratifs|architecture logicielle|développement logiciel|analyse fonctionnelle|test unitaire|déploiement continu)[.]?"
        );




        // Sélectionner les paragraphes pertinents
        Elements paragraphs = jobPage.select("div.col-md-12.blc:nth-of-type(5), div.col-md-12.blc:nth-of-type(6)");

        for (Element paragraph : paragraphs) {
            String[] lines = paragraph.text().split("\\.\\s*|\\n"); // Diviser en lignes ou phrases
            for (String line : lines) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) { // Trouver toutes les correspondances dans chaque ligne
                    String skill = matcher.group().trim();
                    if (!skills.contains(skill)) { // Éviter les doublons
                        skills.add(skill);
                    }
                }
            }
        }

        return skills;
    }


    private static String formatDate(String inputDate) {
        try {
            DateTimeFormatter sourceFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter targetFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate date = LocalDate.parse(inputDate, sourceFormat);
            return date.format(targetFormat);
        } catch (Exception e) {
            System.err.println("Erreur de formatage de la date : " + inputDate);
            return null;
        }
    }

    public static String extractCity(String input) {
        Pattern pattern = Pattern.compile("\\|\\s*([^(]+)\\s*\\(");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Non spécifié";
    }

    public static boolean isTeletravail(String input) {
        // Vérifie si l'entrée contient "oui" ou "hybride" (insensible à la casse)
        if (input != null && (input.equalsIgnoreCase("Télétravail : oui") || input.equalsIgnoreCase("Télétravail : hybride"))) {
            return true;
        }
        // Vérifie si l'entrée contient "non" (insensible à la casse)
        else if (input != null && input.equalsIgnoreCase("Télétravail : non")) {
            return false;
        }
        // En cas d'entrée invalide, retourne "false" par défaut
        System.err.println("Valeur inattendue pour le télétravail : " + input);
        return false; // Choisissez une valeur par défaut adaptée
    }


    public static String diplome(String text) {
        if (text == null) {
            return "Non spécifié";
        }

        // Regex pour capturer un diplôme ou une spécialisation spécifique
        String regex = "(?i)(Dipl[oô]me\\s+d'Ing[ée]nieur.*?grande école)|" +
                "(Dipl[oô]me\\s*:\\s*(.+))|" +
                "(Sp[ée]cialit[ée]\\s*:\\s*(.+))|"+
                "(formation \\s*(.+)) ";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            // Vérifie quel groupe correspond
            if (matcher.group(1) != null) {
                return matcher.group(1).trim(); // Cas : "Diplôme d'Ingénieur d'une grande école"
            } else if (matcher.group(3) != null) {
                return matcher.group(3).trim(); // Cas : "Diplôme : ..."
            } else if (matcher.group(5) != null) {
                return matcher.group(5).trim(); // Cas : "Spécialité : ..."
            }
        }

        return "Non spécifié"; // Si aucune correspondance
    }

    private static Map<String, String> extractLanguages(Document jobPage) {
        Map<String, String> languages = new HashMap<>();

        // Pattern regex pour les langues et niveaux
        Pattern languagePattern = Pattern.compile(
                "(?i)(anglais|english|français|french|espagnol|spanish|allemand|german|arabe|arabic|mandarin|chinese|italien|italian|japonais|japanese)" +
                        "(\s*(courant|fluide|intermédiaire|bilingue|notions de base|niveau avancé|niveau intermédiaire|niveau basique|couramment|fluent|intermediate|basic|advanced|bilingual))?"
        );

        // Sélectionner les paragraphes pertinents
        Elements paragraphs = jobPage.select("div.col-md-12.blc:nth-of-type(5), div.col-md-12.blc:nth-of-type(6)");

        for (Element paragraph : paragraphs) {
            String[] lines = paragraph.text().split("\\.\\s*|\\n"); // Diviser en lignes ou phrases
            for (String line : lines) {
                Matcher matcher = languagePattern.matcher(line);
                while (matcher.find()) {
                    String language = matcher.group(1).trim();
                    String level = matcher.group(2) != null ? matcher.group(2).trim() : "Courant";
                    if (!languages.containsKey(language)) {
                        languages.put(language, level);
                    }
                }
            }
        }

        return languages;
    }


    private static List<String> extractDiplomas(Document jobPage) {
        List<String> diplomas = new ArrayList<>();

        // Regex pour capturer les types de diplômes
        Pattern diplomaPattern = Pattern.compile(
                "(?i)(BAC\\s*\\+\\d+|Licence|Master|Doctorat|PhD|Dipl[oô]me\\s+d'Ing[ée]nieur|Formation\\s+[a-z]+|" +
                        "Master\\s+en\\s+[a-zàâäéèêëîïôöùûüç\\s]+|Dipl[oô]me\\s+en\\s+[a-zàâäéèêëîïôöùûüç\\s]+|" +
                        "Certificat\\s+en\\s+[a-zàâäéèêëîïôöùûüç\\s]+|Bac\\s*\\+\\d+\\s+d'une\\s+[a-zàâäéèêëîïôöùûüç\\s]+)" +
                        "(.*?)(?=\\.|\\n|$)" // Capture jusqu'au point, nouvelle ligne ou fin de chaîne
        );



        // Sélectionner les paragraphes pertinents
        Elements paragraphs = jobPage.select("div.col-md-12.blc:nth-of-type(5), div.col-md-12.blc:nth-of-type(6)");

        for (Element paragraph : paragraphs) {
            String[] lines = paragraph.text().split("\\.\\s*|\\n"); // Diviser en lignes ou phrases
            for (String line : lines) {
                Matcher matcher = diplomaPattern.matcher(line);
                while (matcher.find()) {
                    String diploma = matcher.group().trim();
                    if (!diplomas.contains(diploma)) { // Éviter les doublons
                        diplomas.add(diploma);
                    }
                }
            }
        }

        return diplomas;
    }


    private static List<String> extractSoftSkills(Document jobPage) {
        List<String> softSkills = new ArrayList<>();

        // Pattern regex spécifique pour les soft skills
        Pattern pattern = Pattern.compile(
                "(?i)(communication|autonomie|rigueur|proactivité|organisation|synthèse|écoute|polyvalence|aisance relationnelle|" +
                        "collaboration|résolution de problèmes|gestion du temps|créativité|leadership|adaptabilité|flexibilité|empathie|" +
                        "capacité d'analyse|esprit critique|prise d'initiative|curiosité|gestion des conflits|écoute active|" +
                        "esprit d'équipe|diplomatie|capacité à motiver|résilience|gestion des priorités|négociation|intégrité|éthique|" +
                        "travail en équipe|relationnel|goût du challenge|volonté|esprit d'analyse|veille technologique|" +
                        "ouverture d'esprit|qualités relationnelles|passion pour l'innovation|travail collaboratif|" +
                        "communication écrite et orale|adaptabilité aux déplacements|responsabilité|esprit entrepreneurial|" +
                        "gestion du stress|résolution de conflits|motivation personnelle|capacité à déléguer|prise de décision|" +
                        "vision stratégique|gestion du changement|compétences interculturelles|fiabilité|persévérance|patience|" +
                        "optimisme|écoute active|initiative|sens des responsabilités|orientation client|innovation|empathie organisationnelle)"
        );



        // Sélectionner les paragraphes pertinents
        Elements paragraphs = jobPage.select("div.col-md-12.blc:nth-of-type(5), div.col-md-12.blc:nth-of-type(6)");

        for (Element paragraph : paragraphs) {
            String[] lines = paragraph.text().split("\\.\\s*|\\n"); // Diviser en lignes ou phrases
            for (String line : lines) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) { // Trouver toutes les correspondances dans chaque ligne
                    String softSkill = matcher.group().trim();
                    if (!softSkills.contains(softSkill)) { // Éviter les doublons
                        softSkills.add(softSkill);
                    }
                }
            }
        }

        return softSkills;
    }

    private static List<String> extractHardSkills(Document jobPage) {
        List<String> skills = new ArrayList<>();

        // Pattern regex pour les mots-clés des compétences
        Pattern pattern = Pattern.compile(
                "(?i)\\b(" +  // Ignore la casse and use word boundaries

                        // Single-letter keywords (e.g., R)




                        // Informatique
                        "Java|Java/J2EE|Python|C\\+\\+|C#|JavaScript|TypeScript|PHP|Rust|Swift|Kotlin|" +
                        "Spring Boot|Hibernate|Angular|React|Vue\\.js|Node\\.js|Django|Flask|Laravel|" +
                        "Docker|Kubernetes|CI/CD|Jenkins|GitLab|Ansible|Terraform|Maven|" +
                        "MySQL|PostgreSQL|MongoDB|Oracle|Redis|SQLite|NoSQL|Elasticsearch|" +
                        "Big Data|Hadoop|Spark|Kafka|Tableau|Power BI|ETL|Talend|Informatica|" +
                        "Machine Learning|Deep Learning|Robot Framework|TensorFlow|Keras|PyTorch|scikit-learn|OpenCV|" +
                        "NLP|Traitement du langage naturel|RPA|Power Automate|UiPath|Automation Anywhere|" +
                        "Cloud|AWS|Azure|Google Cloud|Virtualisation|VMware|Hyper-V|Linux|Unix|Shell Scripting|" +

                        // Protocoles réseau et outils
                        "TCP/IP|UDP|FTP|HTTP|HTTPS|DNS|SMTP|IMAP|POP3|SSH|SNMP|LDAP|NFS|" +
                        "DHCP|Telnet|ICMP|VPN|Firewall|Proxy|GitHub|Bitbucket|CI/CD|SSL|TLS|" +

                        // Développement Web - Frontend
                        "HTML5|CSS3|Sass|LESS|Bootstrap|Tailwind CSS|Material UI|Foundation|" +
                        "JavaScript|TypeScript|React|Angular|Vue\\.js|Next\\.js|Nuxt\\.js|jQuery|Three\\.js|" +

                        // Développement Web - Backend
                        "Node\\.js|Express\\.js|NestJS|Spring Boot|Django|Flask|Laravel|" +
                        "Ruby on Rails|ASP\\.NET|FastAPI|Ktor|PHP|Perl|Scala|GraphQL|REST API|SOAP|gRPC|" +

                        // Fullstack
                        "MEAN|MERN|LAMP|JAMstack|" +

                        // Marketing et communication
                        "SEO|Google Analytics|Google Ads|Content marketing|Marketing digital|Community management|Branding|" +
                        "Publicité|Campagnes publicitaires|Rédaction de contenu|Stratégie marketing|E-commerce|Email marketing|" +
                        "Adobe Photoshop|Adobe Illustrator|Adobe InDesign|Canva|CRM|HubSpot|Mailchimp|Marketing automation|" +
                        "Facebook Ads|Instagram Ads|TikTok Ads|WordPress|Shopify|Growth Hacking|Copywriting|Storytelling|" +

                        // Sciences et ingénierie
                        "AutoCAD|SolidWorks|MATLAB|Simulink|CATIA|ANSYS|Fusion 360|Robotique|IoT|Systèmes embarqués|PLC|" +
                        "CFAO|Fabrication additive|Mécanique des fluides|Thermodynamique|Electrotechnique|Domotique|" +
                        "Énergie renouvelable|BIM|Modélisation 3D|ArcGIS|QGIS|Calcul de structures|Géotechnique|" +
                        "Chimie analytique|Biochimie|Biotechnologies|Génie civil|Hydraulique|Thermique|Pneumatique|" +

                        // Langues et outils bureautiques
                        "Microsoft Excel|Word|PowerPoint|Outlook|Access|Tableaux croisés dynamiques|Macros VBA|Google Sheets|" +
                        "Saisie de données|Rédaction professionnelle|Archivage|Prise de notes|Bases de données|CRM|" +

                        // Logiciels spécifiques
                        "ETAP|Canneco|MATLAB|Simulink|AutoCAD|SolidWorks|ANSYS|Fusion 360|" +

                        // ERP
                        "SAP|QuickBooks|Sage|" +

                        // Additional Keywords
                        "Spring|Hibernate|Struts|ASP\\.NET|Django|Flask|Ruby on Rails|Laravel|CodeIgniter|" +
                        "Angular|React|Vue\\.js|Svelte|Ember\\.js|Backbone\\.js|Express\\.js|Node\\.js|Bootstrap|" +
                        "Tailwind CSS|jQuery|Next\\.js|Nuxt\\.js|Electron|TensorFlow|PyTorch|Keras|Scikit-learn|" +
                        "OpenCV|Pandas|NumPy|Matplotlib|Seaborn|Plotly|D3\\.js|Chart\\.js|GraphQL|Redux|MobX|" +
                        "Ant Design|Material-UI|Foundation|Semantic UI|Vuetify|PrimeFaces|PrimeNG|Ionic|" +
                        "Xamarin|Flutter|Quasar|React Native|NativeScript|Capacitor|" +
                        "MySQL|PostgreSQL|SQLite|Oracle|Microsoft SQL Server|MongoDB|Cassandra|Redis|MariaDB|" +
                        "DynamoDB|CouchDB|Firebase|Neo4j|HBase|BigQuery|Snowflake|Redshift|CockroachDB|" +
                        "ElasticSearch|Solr|Amazon RDS|Memcached|Realm|ArangoDB|Derby|VoltDB|InfluxDB|Vertica|" +
                        "TimescaleDB|KeyDB|SAP HANA|GraphQL|FaunaDB|OrientDB|" +
                        "Docker|Kubernetes|Jenkins|Ansible|Puppet|Chef|Terraform|Vagrant|Git|GitLab|GitHub|" +
                        "Bitbucket|Travis CI|CircleCI|Bamboo|TeamCity|Maven|Gradle|Ant|Yarn|npm|NuGet|Helm|" +
                        "Rancher|HashiCorp Vault|Prometheus|Grafana|Splunk|ELK Stack|Zabbix|Nagios|SonarQube|" +
                        "Artifactory|Nexus|AWS CloudFormation|" +
                        "AWS|Azure|Google Cloud Platform|IBM Cloud|Oracle Cloud|Heroku|DigitalOcean|Firebase|" +
                        "Linode|Netlify|Vercel|OpenStack|Alibaba Cloud|Cloudflare|SAP Cloud|Vultr|Rackspace|" +
                        "Cloud Foundry|Kubernetes Engine|Lambda|S3|EC2|RDS|Cloud Functions|Elastic Beanstalk|" +
                        "JUnit|TestNG|Selenium|Cypress|Puppeteer|Postman|SoapUI|Cucumber|Jasmine|Mocha|Chai|" +
                        "Jest|Karma|Protractor|Appium|Espresso|XCUITest|Robot Framework|LoadRunner|JMeter|" +
                        "Gatling|Tricentis Tosca|QTP|Sikuli|Pytest|NUnit|SpecFlow|RSpec|" +
                        "TensorFlow|PyTorch|Keras|Scikit-learn|OpenCV|Pandas|NumPy|Matplotlib|Seaborn|Plotly|" +
                        "D3\\.js|Hadoop|Apache Spark|Mahout|H2O\\.ai|MLlib|Weka|Theano|XGBoost|LightGBM|CatBoost|" +
                        "FastAI|Statsmodels|RAPIDS|NLTK|SpaCy|Gensim|Tesseract|BERT|GPT|HuggingFace Transformers|" +
                        "OpenAI|TensorBoard|BigML|KNIME|DataRobot|" +
                        "Jenkins|GitLab CI|Travis CI|CircleCI|Bamboo|TeamCity|CodePipeline|ArgoCD|Drone|Buddy|" +
                        "Bitrise|Semaphore CI|Octopus Deploy|Spinnaker|" +
                        "Apache|Nginx|Tomcat|Jetty|IIS|Lighttpd|Caddy|Flask|FastAPI|Express\\.js|Koa\\.js|Hapi\\.js|" +
                        "JSON|REST|SOAP|GraphQL|gRPC|Swagger|OpenAPI|Postman|WSO2|MuleSoft|Apigee|" +
                        "Docker|Kubernetes|OpenShift|Mesos|Marathon|Nomad|Helm|Docker Swarm|Portainer|Rancher|" +
                        "HashiCorp Consul|" +
                        "Wireshark|Burp Suite|Metasploit|Nessus|OpenVAS|Snort|OWASP ZAP|Nmap|HashiCorp Vault|" +
                        "AWS IAM|Azure Active Directory|Fail2Ban|AppArmor|SELinux|BitLocker|GnuPG|VeraCrypt|" +
                        "Linux|Windows|macOS|Unix|Ubuntu|Debian|Fedora|CentOS|Red Hat|Arch Linux|Kali Linux|" +
                        "Alpine|Solaris|FreeBSD|OpenBSD|" +
                        "Agile|Scrum|Kanban|Waterfall|Lean|Extreme Programming \\(XP\\)|SAFe|DevOps|Design Thinking|" +
                        "Java|Python|JavaScript|C|C\\+\\+|C#|Ruby|PHP|Swift|Kotlin|Rust|TypeScript|Scala|Perl|" +
                        "Bash|Shell|SQL|HTML|CSS|VBA|Objective-C|Dart|Lua|MATLAB|Groovy|Elixir|F#|COBOL|Haskell|" +
                        "Fortran|Julia|Ada|Erlang|OCaml|VB\\.NET|Delphi|Lisp|Prolog|Assembly|SAS|Scratch|Apex|Smalltalk" +

                        ")\\b"
        );


        // Sélectionner les paragraphes pertinents
        Elements paragraphs = jobPage.select("div.col-md-12.blc:nth-of-type(5), div.col-md-12.blc:nth-of-type(6)");

        for (Element paragraph : paragraphs) {
            String[] lines = paragraph.text().split("\\.\\s*|\\n"); // Diviser en lignes ou phrases
            for (String line : lines) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) { // Trouver toutes les correspondances dans chaque ligne
                    String skill = matcher.group().trim();
                    if (!skills.contains(skill)) { // Éviter les doublons
                        skills.add(skill);
                    }
                }
            }
        }

        return skills;
    }

}
