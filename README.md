# MarieTeamPDF

![Un bateau pris au pif](images/MS_Moby_Wonder.jpg)

## 📄 Description du projet

MarieTeamPDF est une application JavaFX conçue pour générer des rapports PDF détaillés sur les bateaux de la compagnie maritime MarieTeam. Cette application permet de consulter les informations des navires de la flotte, visualiser leurs caractéristiques techniques, et produire des documents PDF professionnels incluant :

- Informations générales du bateau (nom, catégorie, capacité)
- Liste des équipements disponibles à bord
- Planning des traversées programmées
- Images du navire

## 🚢 Fonctionnalités

- **Consultation de la flotte complète** : Affichage de tous les bateaux disponibles avec leurs catégories et capacités
- **Visualisation détaillée** : Informations complètes sur chaque navire
- **Génération de PDF** : Création de rapports PDF incluant images et tableaux de traversées
- **Interface intuitive** : Navigation simple et ergonomique
- **Modification locale** : Possibilité de modifier certaines informations pour la génération de PDF
- **Recherche intelligente d'images** : Système robuste de recherche d'images associées aux navires

## 🛠️ Technologies utilisées

- **Java 17** : Langage de programmation principal
- **JavaFX** : Framework pour l'interface graphique
- **iText 7** : Bibliothèque pour la génération de documents PDF
- **MySQL** : Base de données relationnelle pour le stockage des informations
- **Maven** : Outil de gestion et d'automatisation de production

## 📋 Prérequis

- JDK 17 ou supérieur
- MySQL 8.0 ou supérieur
- Maven 3.8 ou supérieur

## 🚀 Installation et démarrage

### Méthode 1 : Utilisation de l'exécutable Windows (recommandé)

1. Téléchargez la dernière version de MarieTeamPDF.exe
2. Double-cliquez sur l'exécutable pour lancer l'application

### Méthode 2 : Compilation depuis les sources

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/Inkflow59/MarieTeamPDF.git
   cd MarieTeamPDF
   ```

2. Compilez le projet avec Maven :
   ```bash
   mvn clean package
   ```

3. Exécutez l'application avec la commande :
   ```bash
   java -jar target/MarieTeamPDF.jar
   ```

## 📱 Guide d'utilisation rapide

1. **Sélection d'un bateau** : Choisissez un bateau dans la liste déroulante
2. **Consultation des informations** : Cliquez sur "Afficher les informations" pour visualiser les détails
3. **Génération d'un PDF** : Cliquez sur "Générer PDF" et choisissez l'emplacement de sauvegarde
4. **Modification (optionnel)** : Modifiez les informations affichées puis cliquez sur "Enregistrer" pour mettre à jour le rapport

## 🗄️ Structure de la base de données

L'application se connecte à une base de données MySQL nommée "marieteam" qui contient les tables suivantes :
- **bateau** : Informations sur les navires
- **categorie** : Types de bateaux
- **contenir** : Relation entre les bateaux et leurs capacités
- **traversee** : Programmation des voyages
- **liaison** : Informations sur les liaisons maritimes
- **port** : Points de départ et d'arrivée
- **secteur** : Zones maritimes

## 🤝 Contribution

Les contributions sont les bienvenues ! N'hésitez pas à :
1. Fork le projet
2. Créer une branche pour votre fonctionnalité (`git checkout -b nouvelle-fonctionnalite`)
3. Commit vos changements (`git commit -m 'Ajout d'une nouvelle fonctionnalité'`)
4. Push vers la branche (`git push origin nouvelle-fonctionnalite`)
5. Ouvrir une Pull Request

## 📝 Licence

Ce projet est sous licence propriétaire MarieTeam. Tous droits réservés.

## 📞 Contact

Pour toute question ou suggestion, veuillez contacter l'équipe de développement MarieTeam.

---

© 2025 MarieTeam - Générateur de rapports PDF