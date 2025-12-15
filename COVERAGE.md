# Configuration de la Couverture de Code avec JaCoCo et SonarQube

## 📊 Vue d'ensemble

Ce projet est configuré pour générer automatiquement des rapports de couverture de code avec **JaCoCo** et les afficher sur le **dashboard SonarQube**.

## 🛠️ Configuration

### Plugins Maven

Le projet utilise les plugins suivants :

1. **JaCoCo Maven Plugin** (v0.8.11) - Génère les rapports de couverture
2. **Maven Surefire Plugin** (v3.2.5) - Exécute les tests unitaires

### Fichiers de rapport

Après l'exécution des tests, les rapports sont générés dans :

- **Rapport XML** : `target/site/jacoco/jacoco.xml` (utilisé par SonarQube)
- **Rapport HTML** : `target/site/jacoco/index.html` (consultation locale)
- **Rapport CSV** : `target/site/jacoco/jacoco.csv`
- **Fichier d'exécution** : `target/jacoco.exec`

## 🚀 Utilisation

### Exécuter les tests localement

```bash
# Exécuter les tests et générer le rapport de couverture
mvn clean test

# Consulter le rapport HTML dans votre navigateur
open target/site/jacoco/index.html
```

### Exécuter l'analyse complète avec SonarQube

```bash
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=quality-gate \
  -Dsonar.host.url=$SONAR_HOST_URL \
  -Dsonar.login=$SONAR_TOKEN
```

## 🔄 Pipeline CI/CD

Le workflow GitHub Actions (`.github/workflows/build-an-deploy.yml`) exécute automatiquement :

1. ✅ Compilation du code
2. ✅ Exécution des tests unitaires
3. ✅ Génération du rapport JaCoCo
4. ✅ Analyse SonarQube avec envoi de la couverture
5. ✅ Build et push de l'image Docker

### Étape d'analyse dans le pipeline

```yaml
- name: Build and analyze
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
  run: |
    mvn -B clean verify sonar:sonar \
      -Dsonar.projectKey=quality-gate \
      -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }} \
      -Dsonar.login=${{ secrets.SONAR_TOKEN }}
```

## 📈 Visualisation sur SonarQube

Une fois le pipeline exécuté, vous pouvez consulter :

- **Couverture globale** : Pourcentage de lignes couvertes par les tests
- **Couverture par package** : Détail par package/classe
- **Branches couvertes** : Couverture des conditions (if/else, switch, etc.)
- **Lignes non couvertes** : Lignes qui nécessitent plus de tests

### Accès au dashboard

1. Connectez-vous à votre instance SonarQube
2. Naviguez vers le projet **quality-gate**
3. Consultez l'onglet **Coverage**

## 📊 Résultats des tests actuels

```
Tests run: 11
Failures: 0
Errors: 0
Skipped: 1
Classes analyzed: 8
```

## 🎯 Seuils de couverture

Le plugin JaCoCo est configuré avec un seuil minimum de **0%** (pas de blocage). Vous pouvez ajuster ce seuil dans le `pom.xml` :

```xml
<configuration>
    <rules>
        <rule>
            <element>PACKAGE</element>
            <limits>
                <limit>
                    <counter>LINE</counter>
                    <value>COVEREDRATIO</value>
                    <minimum>0.80</minimum> <!-- 80% de couverture minimum -->
                </limit>
            </limits>
        </rule>
    </rules>
</configuration>
```

## 🔧 Dépannage

### Le rapport n'apparaît pas sur SonarQube

Vérifiez que :
- Le fichier `jacoco.xml` a été généré dans `target/site/jacoco/`
- La propriété `sonar.coverage.jacoco.xmlReportPaths` est correctement définie dans `sonar-project.properties`
- Votre token SonarQube est valide

### Les tests ne s'exécutent pas

```bash
# Vérifier les dépendances Maven
mvn dependency:tree

# Nettoyer et reconstruire
mvn clean install
```

## 📝 Notes

- Les tests s'exécutent automatiquement lors de chaque push sur la branche `main`
- La couverture de code est envoyée à SonarQube après chaque build réussi
- Les rapports locaux peuvent être consultés à tout moment après `mvn test`
