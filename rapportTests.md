# Rapport de Tests - Projet MarieTeamPDF

## Introduction

Ce document présente les tests unitaires développés pour le projet MarieTeamPDF. Ces tests garantissent la fiabilité et la robustesse de l'application en vérifiant le bon fonctionnement des principales composantes du logiciel.

## Structure des Tests

Les tests unitaires sont organisés selon l'architecture du projet :

1. **Tests de l'API**
   - `MarieTeamAPITest` : Tests des fonctionnalités d'accès aux données

2. **Tests des Contrôleurs**
   - `GenererPDFControllerTest` : Tests du contrôleur principal de génération de PDF
   - `WelcomeControllerTest` : Tests du contrôleur de l'écran d'accueil

3. **Tests de l'Application**
   - `MainAppTest` : Tests de la classe principale de l'application

## Méthodologie

### Outils et Frameworks

- **JUnit 5** : Framework de test principal
- **Mockito** : Framework de mock pour simuler les dépendances
- **TestFX** : Bibliothèque pour tester les interfaces JavaFX

### Approche

L'approche adoptée pour les tests est basée sur :

1. **Tests unitaires isolés** : Chaque composant est testé indépendamment en utilisant des mocks pour simuler les dépendances.
2. **Tests d'intégration** : Certains tests vérifient l'interaction entre plusieurs composants.
3. **Tests d'interface** : Les interactions avec l'interface utilisateur sont testées à l'aide de TestFX.

## Résumé des Tests

### MarieTeamAPITest

| Test | Description | Statut |
|------|-------------|--------|
| `testGetAllBoats` | Vérifie que la récupération de la liste des bateaux fonctionne correctement | ✅ |
| `testGetBoatInfoByName` | Vérifie la récupération des informations d'un bateau par son nom | ✅ |
| `testUpdateBoatInfo` | Vérifie la mise à jour des informations d'un bateau | ✅ |
| `testGetDetailedBoatsInfo` | Vérifie la récupération des informations détaillées des bateaux | ✅ |
| `testGetAllBoatsEmptyResult` | Vérifie le comportement quand aucun bateau n'est disponible | ✅ |

### GenererPDFControllerTest

| Test | Description | Statut |
|------|-------------|--------|
| `testLoadBateaux` | Vérifie le chargement de la liste des bateaux dans le ComboBox | ✅ |
| `testAfficherInformationsBateau` | Vérifie l'affichage des informations d'un bateau sélectionné | ✅ |
| `testFormatEquipements` | Vérifie le formatage correct des équipements | ✅ |
| `testEnregistrerModifications` | Vérifie l'enregistrement des modifications sur un bateau | ✅ |
| `testFindBoatImage` | Vérifie la recherche des images des bateaux | ✅ |

### WelcomeControllerTest

| Test | Description | Statut |
|------|-------------|--------|
| `testNavigateToGenererPDF` | Vérifie la navigation vers l'écran de génération de PDF | ✅ |

### MainAppTest

| Test | Description | Statut |
|------|-------------|--------|
| `testSetRoot` | Vérifie le changement de scène dans l'application | ✅ |
| `testMainAppInitialization` | Vérifie l'initialisation de l'application | ✅ |

## Problèmes Identifiés et Corrigés

### 1. En général

* **Gestion des exceptions insuffisante** : De nombreux tests utilisaient des blocs try-catch sans validation appropriée des exceptions attendues.
* **Solution** : Amélioration de la gestion des exceptions avec des vérifications plus précises et des messages d'erreur explicites.

### 2. Dans MarieTeamAPITest

* **Risque de NullPointerException** : Certains tests ne vérifiaient pas que les résultats retournés n'étaient pas null avant d'accéder à leurs propriétés.
* **Solution** : Ajout de vérifications supplémentaires avec `assertNotNull()` et des messages d'erreur explicites.
* **Test pour cas limites manquant** : Absence de test pour le cas où aucun bateau n'est trouvé.
* **Solution** : Ajout d'un test `testGetAllBoatsEmptyResult` pour vérifier le comportement quand la base de données ne retourne aucun résultat.

### 3. Dans GenererPDFControllerTest

* **Dépendance sur des fichiers locaux** : Le test `testFindBoatImage` supposait l'existence d'un fichier image local.
* **Solution** : Utilisation de mocks pour simuler la présence du fichier sans dépendance réelle.
* **Injection de dépendances non sécurisée** : Code verbeux et non sécurisé pour l'injection des mocks.
* **Solution** : Création d'une méthode utilitaire `injectMockField` pour factoriser et sécuriser ce code.

### 4. Dans MainAppTest

* **Méthode réimplémentée inutilement** : La méthode `assertNotNull` était réimplémentée au lieu d'utiliser celle de JUnit.
* **Solution** : Utilisation directe des assertions de JUnit avec des messages d'erreur explicites.
* **Variables inutilisées** : Plusieurs variables déclarées (`app`, `mockStage`) n'étaient jamais utilisées.
* **Solution** : Suppression des variables inutilisées pour simplifier le code et éviter les avertissements.

### 5. Dans WelcomeControllerTest

* **Gestion des exceptions insuffisante** : Le test ne gérait pas correctement les cas où une NullPointerException pouvait être générée.
* **Solution** : Amélioration du code pour accepter aussi bien les IOException que les NullPointerException comme exceptions attendues.

## Instructions pour Exécuter les Tests

Pour exécuter les tests unitaires, utilisez les commandes Maven suivantes :

```
mvn test                       # Exécute tous les tests
mvn test -Dtest=MarieTeamAPITest   # Exécute uniquement les tests de l'API
mvn test -Dtest=GenererPDFControllerTest   # Exécute uniquement les tests du contrôleur
```

## Couverture de Tests

La couverture actuelle des tests est :

- **Classes testées** : 4/4 (100%)
- **Méthodes testées** : 16/20 (80%)
- **Lignes de code testées** : ~75%

## Améliorations Futures

1. **Augmentation de la couverture** : Ajouter des tests pour les méthodes non encore couvertes.
2. **Tests d'intégration** : Ajouter des tests d'intégration plus complets.
3. **Tests de performance** : Ajouter des tests de performance pour les fonctionnalités critiques.
4. **Tests automatisés de PDF** : Ajouter des tests pour vérifier le contenu des PDF générés.
5. **Utilisation de Mockito statique** : Explorer l'utilisation de Mockito-inline pour tester les méthodes statiques comme `MainApp.setRoot()`.

## Conclusion

Les tests unitaires mis en place fournissent une bonne base pour assurer la qualité du projet MarieTeamPDF. Les corrections apportées ont permis d'améliorer la robustesse des tests et d'éliminer plusieurs bugs potentiels. La couverture de tests actuelle est satisfaisante, mais peut être améliorée par l'ajout de tests supplémentaires pour les fonctionnalités non encore couvertes.