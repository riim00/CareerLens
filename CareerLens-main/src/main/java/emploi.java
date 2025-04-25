import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class emploi {

    private static final String BASE_URL = "https://www.emploi.ma/recherche-jobs-maroc?f%5B0%5D=im_field_offre_metiers%3A31&page=";
     // sans filtre https://www.emploi.ma/recherche-jobs-maroc?page=
    public static void main(String[] args) {
        int page = 0;
        int maxPages = 16;

        try {
            for (int currentPage = page; currentPage < maxPages; currentPage++) {
                String url = BASE_URL + currentPage;
                Document doc = fetchDocument(url);
                if (doc != null) {
                    extractAndDisplayJobDetails(doc);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de connexion ou de récupération de données : " + e.getMessage());
        }
    }

    /**
     * Fetches the HTML document from the provided URL.
     *
     * @param url the URL to fetch the document from.
     * @return the Document object representing the HTML page.
     * @throws IOException if there is an error while connecting or fetching the page.
     */
    private static Document fetchDocument(String url) throws IOException {
        System.out.println("Récupération des données depuis : " + url);
        return Jsoup.connect(url).timeout(100000).get();
    }

    /**
     * Extracts job details from the Document and prints them.
     *
     * @param doc the Document object containing the HTML content.
     */
    private static void extractAndDisplayJobDetails(Document doc) {
        Elements jobs = doc.select(".card-job-detail");
        System.out.println("Emploi.ma Scraping");
        System.out.println("=============================================");

        for (Element job : jobs) {
            String jobTitle = extractJobTitle(job);
            String companyName = extractCompanyName(job);
            String jobDatePublication = extractJobDate(job);
            String jobDescription = extractJobDescription(job);
            String urlOffre = "https://www.emploi.ma" + job.select("h3 > a").attr("href");

            try {

            Document jobPage = Jsoup.connect(urlOffre).get();


            String descriptionEntreprise = jobPage.select("li.company-description").text();
            String SiteWeb = jobPage.selectFirst("span > a").attr("href");
            String Ville = jobPage.selectFirst("ul.arrow-list > li:nth-of-type(5)").text();
            String SecteurActivite = jobPage.selectFirst("ul.arrow-list > li:nth-of-type(2)").text();
            String Metier = jobPage.selectFirst("ul.arrow-list > li:nth-of-type(1)").text();
            String Profile = jobPage.selectFirst("div.job-qualifications").text();
            String niveauLangue = jobPage.selectFirst("ul.arrow-list > li:nth-of-type(8)").text();

                List<String> diplomas = extractDiplomas(jobPage);






                // Variables supplémentaires
                String niveauEtudes = "";
                String niveauExperience = "";
                String contratPropose = "";
                String region = "";
                String competencesCles = "";

                // Extraction des détails supplémentaires
                Elements details = job.select("ul > li");
                for (Element li : details) {
                    String liText = li.text();

                    if (liText.contains("Niveau d´études requis :")) {
                        niveauEtudes = liText.replace("Niveau d´études requis :", "").trim();
                    } else if (liText.contains("Niveau d'expérience")) {
                        niveauExperience = liText.replace("Niveau d'expérience :", "").trim();
                    } else if (liText.contains("Contrat proposé")) {
                        contratPropose = liText.replace("Contrat proposé :", "").trim();
                    } else if (liText.contains("Région de")) {
                        region = liText.replace("Région de :", "").trim();
                    } else if (liText.contains("Compétences clés")) {
                        competencesCles = liText.replace("Compétences clés :", "").trim();
                    }
                }

                // Affichage des informations
                System.out.println("Nom du site: https://www.emploi.ma/");
                System.out.println("Poste: " + jobTitle);
                System.out.println("Société: " + companyName);
                System.out.println("Date de publication: " + jobDatePublication);
                System.out.println("Description: " + jobDescription);
                System.out.println("Niveau d'études : " + niveauEtudes);
                System.out.println("Niveau d'expérience : " + niveauExperience);
                System.out.println("Contrat proposé : " + contratPropose);
                System.out.println("Région : " + region);
                System.out.println("Compétences clés : " + competencesCles);
                System.out.println("url : " + urlOffre);
                System.out.println("Site Web : " + SiteWeb);
                System.out.println(descriptionEntreprise);
                System.out.println(Ville);
                System.out.println(SecteurActivite);
                System.out.println(Metier);
                System.out.println("Profile Recherche: " + Profile);
                System.out.println("niveau des "+ niveauLangue);
                System.out.println( extractLanguages(niveauLangue));
                System.out.println("Diplômes :");
                if (!diplomas.isEmpty()) {
                    for (String diploma : diplomas) {
                        System.out.println("  - " + diploma); // Affiche chaque diplôme avec un tiret
                    }
                } else {
                    System.out.println("  Aucun diplôme identifié.");
                }

                System.out.println("Compétences :");

                List<String> skills = extractSkills(jobPage);
                String skillsString = String.join(", ", skills);

                if (!skills.isEmpty()) {
                    for (String skill : skills) {
                        System.out.println("  - " + skill); // Affiche chaque compétence avec un tiret
                    }
                } else {
                    System.out.println("  Aucune compétence identifiée.");
                }

                List<String> softSkills = extractSoftSkills(jobPage);
                String skillsString2 = String.join(", ", softSkills);


                System.out.println("softskills :");

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
                System.out.println("=====================================");


                try {
                    String StringDiplome = String.join(", ", diplomas);

                    DatabaseConnection bd = new DatabaseConnection();
                    bd.connect();
                    bd.insertJob(jobTitle,urlOffre,"https://www.emploi.ma/",jobDatePublication,jobDatePublication,null,SiteWeb
                    ,companyName,descriptionEntreprise,jobDescription,region,Ville,SecteurActivite,Metier,contratPropose,niveauEtudes,StringDiplome,
                            niveauExperience,Profile,null,competencesCles+HardSkillsString,skillsString2,skillsString,extractLanguages(niveauLangue),
                            niveauLangue,null,null,null);

                } catch (Exception e) {
                    System.err.println("Erreur lors de l'insertion en base de données : " + e.getMessage());
                }





            } catch (IOException e) {
                System.err.println("Erreur de connexion ou de récupération des données sur la page  : " + e.getMessage());
            }


        }
    }

    /**
     * Extracts the job title from the job element.
     *
     * @param job the Element representing a single job listing.
     * @return the job title.
     */
    private static String extractJobTitle(Element job) {
        return job.select("h3 > a").text();
    }

    /**
     * Extracts the job date from the job element.
     *
     * @param job the Element representing a single job listing.
     * @return the job date, if exists, else empty string.
     */
    private static String extractJobDate(Element job) {
        Element dateElement = job.select("time").first();
        String rawDate = (dateElement != null) ? dateElement.text() : "Non spécifiée";
        return formatDate(rawDate); // Convertir le format ici
    }

    private static String formatDate(String rawDate) {
        try {
            // Exemple de format d'entrée : "17.11.2024"
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(rawDate, inputFormatter);
            return date.format(outputFormatter);
        } catch (DateTimeParseException e) {
            System.err.println("Erreur de formatage de la date : " + rawDate + " - " + e.getMessage());
            return null; // Retourne null si la conversion échoue
        }
    }

    /**
     * Extracts the job description from the job element.
     *
     * @param job the Element representing a single job listing.
     * @return the job description.
     */
    private static String extractJobDescription(Element job) {
        return job.select(".card-job-description").text();
    }

    /**
     * Extracts the company name from the job element.
     *
     * @param job the Element representing a single job listing.
     * @return the company name.
     */
    private static String extractCompanyName(Element job) {
        return job.select(".card-job-company.company-name").text();
    }

    public static String extractLanguages(String input) {
        // Expression régulière pour capturer les langues (mots avant ">")
        Pattern pattern = Pattern.compile("(\\w+)\\s*>");

        // Création du matcher
        Matcher matcher = pattern.matcher(input);

        // Utiliser un StringBuilder pour construire le résultat final
        StringBuilder result = new StringBuilder();

        // Boucle pour parcourir toutes les correspondances
        while (matcher.find()) {
            if (result.length() > 0) {
                result.append(", "); // Ajouter une virgule entre les langues
            }
            result.append(matcher.group(1)); // Ajouter la langue trouvée
        }

        return result.toString(); // Retourner le résultat sous forme de String
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
        Elements paragraphs = jobPage.select("div.job-qualifications");

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
        Elements paragraphs = jobPage.select("div.job-qualifications");

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
        Elements paragraphs = jobPage.select("div.job-qualifications");

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
        Elements paragraphs = jobPage.select("div.job-qualifications");

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


