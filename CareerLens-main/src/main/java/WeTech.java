import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeTech {
    public static void main(String[] args) {
        String baseUrl = "https://www.wetech.ma";
        int totalPages = 6; // Définir le nombre total de pages à scraper

        for (int page = 1; page <= totalPages; page++) {
            String pageUrl = baseUrl + "/offres.php/" + page + "/";
            System.out.println("Chargement de la page : " + pageUrl);

            try {
                // Charger la page principale des offres pour chaque page
                Document document = Jsoup.connect(pageUrl).get();
                Elements offres = document.select(".offres");

                // Parcourir chaque offre
                for (Element offre : offres) {
                    // Extraire l'URL relative pour les détails
                    String detailsUrl = offre.select("a.offreUrl").attr("href");
                    if (detailsUrl.isEmpty()) {
                        System.out.println("Aucune URL trouvée pour cette offre.");
                        continue;
                    }

                    // Construire l'URL absolue
                    String fullDetailsUrl = "https://www.wetech.ma" + detailsUrl;
                    System.out.println("Chargement des détails depuis : " + fullDetailsUrl);

                    try {
                        Document details = Jsoup.connect(fullDetailsUrl).get();

                        String companyUrl= "https://www.wetech.ma/"+ details.select("h2 > a").attr("href");
                        Document company = Jsoup.connect(companyUrl).get();

                        String DescriptionSociete = company.selectFirst("div.row").text();
                        String companyAdresse = company.select("div.item > div.col-md-12 > span").text();
                        String SecteurActivite = company.select("div.col-md-10.col-sm-9.headcen > span").text();
                        String Avantages = company.select("div.row:nth-of-type(4)").text();

                        // Extraction des détails
                        String jobTitle = details.select("div.offredetails h1").text();

                        String jobDescription = details.select("div.blocpost:nth-of-type(2)").text();
                        String companyName = details.selectFirst("h2 > a").text();
                        String datePostedRaw = details.selectFirst("div.col-md-10.col-xs-12 > span:first-of-type").text();
                        String datePosted = formatToSqlDate(datePostedRaw); // Convertir la date au format SQL
                        String regionRaw = details.selectFirst("div.col-md-10.col-xs-12 > span:nth-of-type(2)").text();
                        String region = extractAfterDash(regionRaw);
                        String contractType = details.selectFirst("div.col-md-10.col-xs-12 > span:nth-of-type(3)").text();
                        String profileText = details.select("div.blocpost:nth-of-type(3)").text();
                        String Salaire = details.select("div.blocpost:nth-of-type(4) > div#statuts > div.offredetails > h3:nth-of-type(2) + p").text();
                                // Extraire les informations à partir du texte brut
                        String experienceLevel = extractExperienceLevel(profileText);
                        String educationLevel = extractEducationLevel(profileText);

                        String langue =extractLanguages(profileText);

                        // Afficher les informations

                        System.out.println("langue : "+ langue);
                        System.out.println("Secteur d activiter: "+SecteurActivite);
                        System.out.println("salaire : "+Salaire);
                        System.out.println("avantages : "+Avantages);
                        System.out.println(jobDescription);
                        System.out.println("adresse : "+companyAdresse);
                        System.out.println(DescriptionSociete);
                        System.out.println("Titre du poste : " + jobTitle);
                        System.out.println("Entreprise : " + companyName);
                        System.out.println("Date de publication : " + datePosted);
                        System.out.println("Région : " + region);
                        System.out.println("Type de contrat : " + contractType);
                        System.out.println("Niveau d'expérience : " + experienceLevel);
                        System.out.println("Niveau d'étude : " + educationLevel);
                        List<String> skills = extractSkills(details);
                        String skillsString = String.join(", ", skills);
                        List<String> diplomas = extractDiplomas(details);

                        System.out.println("skilss:");

                        if (!skills.isEmpty()) {
                            for (String skill : skills) {
                                System.out.println("  - " + skill); // Affiche chaque compétence avec un tiret
                            }
                        } else {
                            System.out.println("  Aucune compétence identifiée.");
                        }
                        System.out.println("Diplômes :");
                        if (!diplomas.isEmpty()) {
                            for (String diploma : diplomas) {
                                System.out.println("  - " + diploma); // Affiche chaque diplôme avec un tiret
                            }
                        } else {
                            System.out.println("  Aucun diplôme identifié.");
                        }


                        List<String> softSkills = extractSoftSkills(details);
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

                        List<String> HardSkills = extractHardSkills(details);
                        String HardSkillsString = String.join(", ", HardSkills);

                        if (!HardSkills.isEmpty()) {
                            for (String HardSkill : HardSkills) {
                                System.out.println("  - : " + HardSkill); // Affiche chaque soft skill avec un tiret
                            }
                        } else {
                            System.out.println("  Aucune compétence identifiée.");
                        }
                        System.out.println("==========================================");

                        // Insérer dans la base de données
                        try {
                            DatabaseConnection bd = new DatabaseConnection();

                            bd.insertJob(jobTitle, fullDetailsUrl, "https://www.wetech.ma/", datePosted, datePosted, companyAdresse, null, companyName, DescriptionSociete, jobDescription, region, region, SecteurActivite, null, contractType, educationLevel, null,
                                    experienceLevel, profileText, null,HardSkillsString , skillsString2, skillsString, langue,
                                    null, Salaire, Avantages, null);
                        } catch (Exception e) {
                            System.err.println("Erreur lors de l'insertion en base de données : " + e.getMessage());
                        }

                    } catch (IOException e) {
                        System.err.println("Erreur lors du chargement des détails pour l'URL : " + fullDetailsUrl);
                    }
                }

            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de la page " + page + " : " + e.getMessage());
            }
        }
    }

    private static String formatToSqlDate(String rawDate) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(rawDate, inputFormatter);
            return date.format(outputFormatter);
        } catch (DateTimeParseException e) {
            System.err.println("Erreur de formatage de la date : " + rawDate + " - " + e.getMessage());
            return null;
        }
    }

    private static String extractAfterDash(String rawText) {
        if (rawText.contains("-")) {
            return rawText.split("-")[1].trim();
        }
        return rawText.trim();
    }

    private static String extractExperienceLevel(String text) {
        Pattern pattern = Pattern.compile(
                "(une\\s*expérience\\s*(significative|pertinente|avérée)\\s*de\\s*\\d+\\s*(ans|years?)\\s*(minimum|et\\s*plus|ou\\s*plus|or\\s*more)|" + // "une expérience significative de X ans minimum"
                        "\\d+\\s*(ans|years?)\\s*(et\\s*plus|ou\\s*plus|or\\s*more)|" +                              // "3 ans et plus" ou "3 years or more"
                        "plus\\s*de\\s*\\d+\\s*(ans|years?)|" +                                                     // "plus de 3 ans"
                        "expérience\\s*avérée\\s*de\\s*\\d+\\s*(ans|years?)|" +                                     // "expérience avérée de 3 ans"
                        "une\\s*expérience\\s*de\\s*\\d+\\s*(ans|years?)|" +                                        // "une expérience de 5 ans"
                        "\\d+\\s*to\\s*\\d+\\s*years\\s*of\\s*experience|" +                                        // "5 to 8 years of experience"
                        "minimum\\s*de\\s*\\d+\\s*(ans|years?)|" +                                                  // "minimum de 3 ans"
                        "minimum\\s*of\\s*\\d+\\s*years|" +                                                         // "minimum of 3 years"
                        "\\u00e0\\s*\\d+\\s*ans|" +                                                                 // "à 3 ans"
                        "Au\\s*moins\\s*\\d+\\s*ans|" +                                                             // "Au moins 3 ans"
                        "entre\\s*\\d+\\s*et\\s*\\d+\\s*(ans|years?)|" +                                            // "entre 3 et 5 ans"
                        "\\d+/\\d+\\s*ans|" +                                                                       // "3/5 ans"
                        "\\d+\\s*ans\\s*d'expérience|" +                                                            // "3 ans d'expérience"
                        "débutant|confirmé|senior|junior)",                                                         // mots-clés fixes
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : "Non spécifié";
    }


    private static String extractEducationLevel(String text) {
        Pattern pattern = Pattern.compile(
                "(dipl[oô]me\\s*(de\\s*)?Bac\\s*\\+\\s*\\d+|" +                  // Capture "diplôme de Bac+2"
                        "De\\s*formation\\s*Bac\\s*\\+\\s*\\d+|" +                       // Capture "De formation Bac+X"
                        "Bac\\s*\\+\\s*\\d+|" +                                         // Capture "Bac+X" ou "Bac + X"
                        "Master|" +                                                     // Capture "Master"
                        "Licence|" +                                                    // Capture "Licence"
                        "Doctorat|" +                                                   // Capture "Doctorat"
                        "PhD|Doctorat|" +                                               // Capture "PhD" ou "Doctorat"
                        "Bachelor|Licence|" +                                           // Capture "Bachelor" ou "Licence"
                        "Dipl[oô]me|" +                                                 // Capture "Diplôme"
                        "Engineering|Ingénierie|" +                                     // Capture "Engineering" ou "Ingénierie"
                        "College\\s*Degree|University\\s*Degree|" +                     // Capture "College Degree" ou "University Degree"
                        "University\\s*or\\s*college\\s*degree\\s*in\\s*[^,]+|" +       // Capture "University or college degree in X"
                        "relevant\\s*experience\\s*in\\s*equivalent\\s*domain)",        // Capture "relevant experience in equivalent domain"
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : "Non spécifié";
    }






    private static String extractLanguages(String text) {
        // Regex pour détecter uniquement les langues
        Pattern languagePattern = Pattern.compile(
                "(anglais|français|espagnol|allemand|arabe|italien|japonais|chinois)",
                Pattern.CASE_INSENSITIVE
        );

        // Ensemble pour stocker les langues détectées sans doublons
        Set<String> languages = new LinkedHashSet<>();

        // Appliquer la regex sur le texte
        Matcher matcher = languagePattern.matcher(text);

        while (matcher.find()) {
            languages.add(matcher.group().trim());
        }

        // Retourner les langues sous forme de chaîne séparées par des virgules
        return languages.isEmpty() ? "Francais" : String.join(", ", languages);
    }

    private static List<String> extractSkills(Document jobPage) {
        List<String> skills = new ArrayList<>();

        // Pattern regex pour les mots-clés des compétences
        Pattern pattern = Pattern.compile(
                "(?i)(maîtrise|compétences|connaissances|connaissance de|capacité à|expérience en|expérience avec|experience in|experience with|culture|force|skills|knowledge|knowledge of|ability to|expertise|proficiency|" +
                        "curiosité|pragmatisme|agilité|safe|analyse|analyser|comprendre|alerter|communication|autonomie|rigueur|proactivité|organisation|synthèse|écoute|polyvalence|aisance relationnelle|" +
                        "convaincre|argumenter|présenter|adhérer|traduire|collaborer|collaboration|synthétiser|résoudre|proposer)\s*(à|en|des|de la|de l'|in|of|to|de)?\s*([^;:.•\n]+)" +
                        "|(Java|Java/J2EE|Python|C\\+\\+|C#|JavaScript|TypeScript|PHP|Rust|Swift|Kotlin|" +
                        "Spring Boot|Hibernate|Angular|React|Vue\\.js|Node\\.js|Django|Flask|Laravel|" +
                        "Docker|Kubernetes|CI/CD|Jenkins|GitLab|Ansible|Terraform|Maven|" +
                        "MySQL|PostgreSQL|MongoDB|Oracle|Redis|SQLite|" +
                        "Selenium|JUnit|Postman|Cucumber|JMeter|Gatling|Robot Framework|" +
                        "API REST|SOAP|Mockito|JIRA|Confluence|Agile|SAFe|Scrum|Leadership|Communication|Soft skills|Teamwork|Problem-solving|" +
                        "gestion agiles|outils collaboratifs|architecture logicielle|développement logiciel|analyse fonctionnelle|test unitaire|déploiement continu)"
        );




        // Sélectionner les paragraphes pertinents
        Elements paragraphs = jobPage.select("div.blocpost:nth-of-type(3)");

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
        Elements paragraphs = jobPage.select("div.blocpost:nth-of-type(3)");

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
        Elements paragraphs = jobPage.select("div.blocpost:nth-of-type(3)");

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
        Elements paragraphs = jobPage.select("div.blocpost:nth-of-type(3)");

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
