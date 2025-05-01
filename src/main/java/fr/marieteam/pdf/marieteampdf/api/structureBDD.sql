-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : jeu. 01 mai 2025 à 17:53
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `marieteam`
--
CREATE DATABASE IF NOT EXISTS `marieteam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `marieteam`;

-- --------------------------------------------------------

--
-- Structure de la table `admin`
--

CREATE TABLE `admin` (
  `idAdmin` int(11) NOT NULL,
  `nomUtilisateur` text DEFAULT NULL,
  `mdp` text DEFAULT NULL,
  `lastLogin` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `bateau`
--

CREATE TABLE `bateau` (
  `idBat` int(11) NOT NULL,
  `nomBat` varchar(255) DEFAULT NULL,
  `lienImage` varchar(255) DEFAULT NULL,
  `Equipements` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `categorie`
--

CREATE TABLE `categorie` (
  `lettre` char(1) NOT NULL,
  `libelleCat` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `contenir`
--

CREATE TABLE `contenir` (
  `lettre` char(1) NOT NULL,
  `idBat` int(11) NOT NULL,
  `capaciteMax` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `enregistrer`
--

CREATE TABLE `enregistrer` (
  `idType` int(11) NOT NULL,
  `numRes` int(11) NOT NULL,
  `quantite` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `liaison`
--

CREATE TABLE `liaison` (
  `code` int(11) NOT NULL,
  `distance` float DEFAULT NULL,
  `idSecteur` int(11) DEFAULT NULL,
  `idPort_Depart` int(11) DEFAULT NULL,
  `idPort_Arrivee` int(11) DEFAULT NULL,
  `tempsLiaison` time DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `periode`
--

CREATE TABLE `periode` (
  `dateDeb` date NOT NULL,
  `dateFin` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `port`
--

CREATE TABLE `port` (
  `idPort` int(11) NOT NULL,
  `nomPort` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `reservation`
--

CREATE TABLE `reservation` (
  `numRes` int(11) NOT NULL,
  `nomRes` varchar(255) DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `codePostal` varchar(5) DEFAULT NULL,
  `ville` varchar(255) DEFAULT NULL,
  `numTra` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déclencheurs `reservation`
--
DELIMITER $$
CREATE TRIGGER `PlusDePlace` BEFORE INSERT ON `reservation` FOR EACH ROW BEGIN
    DECLARE capaciteBateau INT;
    DECLARE nombreReservations INT;

    -- Récupérer la capacité maximale du bateau lié à la traversée
    SELECT MAX(contenir.capaciteMax)
    INTO capaciteBateau
    FROM contenir
    JOIN bateau ON contenir.idBat = bateau.idBat
    JOIN traversee ON traversee.idBat = bateau.idBat
    WHERE traversee.numTra = NEW.numTra;

    -- Compter le nombre de réservations existantes pour la traversée
    SELECT COUNT(*) 
    INTO nombreReservations
    FROM reservation
    WHERE reservation.numTra = NEW.numTra;

    -- Vérifier si la capacité est dépassée
    IF (nombreReservations >= capaciteBateau) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Il n''y a plus de places disponibles sur le bateau sélectionné.';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `secteur`
--

CREATE TABLE `secteur` (
  `idSecteur` int(11) NOT NULL,
  `nomSecteur` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `tarifer`
--

CREATE TABLE `tarifer` (
  `dateDeb` date NOT NULL,
  `idType` int(11) NOT NULL,
  `code` int(11) NOT NULL,
  `tarif` float DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `traversee`
--

CREATE TABLE `traversee` (
  `numTra` int(11) NOT NULL,
  `date` date DEFAULT NULL,
  `heure` time DEFAULT NULL,
  `idBat` int(11) DEFAULT NULL,
  `code` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déclencheurs `traversee`
--
DELIMITER $$
CREATE TRIGGER `DateMinimum` BEFORE INSERT ON `traversee` FOR EACH ROW BEGIN
IF (NEW.date < CURRENT_DATE) THEN
	SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'La date de départ ne peut pas être antérieure à aujourd''hui.';
END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `DateMinimumUpdate` BEFORE UPDATE ON `traversee` FOR EACH ROW BEGIN
IF (NEW.date < CURRENT_DATE) THEN
	SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'La date de départ ne peut pas être antérieure à aujourd''hui.';
END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `MemeJourCondition` BEFORE INSERT ON `traversee` FOR EACH ROW BEGIN
IF (NEW.date = CURRENT_DATE) THEN
	IF(NEW.heure < CURRENT_TIME) THEN
    	SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'L''heure de départ ne peut pas être inférieure à l''heure actuelle si le départ a lieu aujourd''hui';
    END IF;
END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `MemeJourConditionUpdate` BEFORE UPDATE ON `traversee` FOR EACH ROW BEGIN
IF (NEW.date = CURRENT_DATE) THEN
	IF(NEW.heure < CURRENT_TIME) THEN
    	SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'L''heure de départ ne peut pas être inférieure à l''heure actuelle si le départ a lieu aujourd''hui';
    END IF;
    IF (OLD.heure < CURRENT_TIME) THEN
    	SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'L''heure de départ ne peut pas être inférieure à l''heure actuelle si le départ a lieu aujourd''hui';
    END IF;
END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `type`
--

CREATE TABLE `type` (
  `idType` int(11) NOT NULL,
  `libelleType` varchar(255) DEFAULT NULL,
  `lettre` char(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `admin`
--
ALTER TABLE `admin`
  ADD PRIMARY KEY (`idAdmin`);

--
-- Index pour la table `bateau`
--
ALTER TABLE `bateau`
  ADD PRIMARY KEY (`idBat`);

--
-- Index pour la table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`lettre`);

--
-- Index pour la table `contenir`
--
ALTER TABLE `contenir`
  ADD PRIMARY KEY (`lettre`,`idBat`);

--
-- Index pour la table `enregistrer`
--
ALTER TABLE `enregistrer`
  ADD PRIMARY KEY (`idType`,`numRes`),
  ADD KEY `FK_Enregistrer_numRes` (`numRes`);

--
-- Index pour la table `liaison`
--
ALTER TABLE `liaison`
  ADD PRIMARY KEY (`code`),
  ADD KEY `FK_Liaison_idSecteur` (`idSecteur`),
  ADD KEY `FK_Liaison_idPort_Depart` (`idPort_Depart`),
  ADD KEY `FK_Liaison_idPort_Arrivee` (`idPort_Arrivee`);

--
-- Index pour la table `periode`
--
ALTER TABLE `periode`
  ADD PRIMARY KEY (`dateDeb`);

--
-- Index pour la table `port`
--
ALTER TABLE `port`
  ADD PRIMARY KEY (`idPort`);

--
-- Index pour la table `reservation`
--
ALTER TABLE `reservation`
  ADD PRIMARY KEY (`numRes`),
  ADD KEY `FK_Reservation_numTra` (`numTra`);

--
-- Index pour la table `secteur`
--
ALTER TABLE `secteur`
  ADD PRIMARY KEY (`idSecteur`);

--
-- Index pour la table `tarifer`
--
ALTER TABLE `tarifer`
  ADD PRIMARY KEY (`dateDeb`,`idType`,`code`),
  ADD KEY `FK_Tarifer_idType` (`idType`),
  ADD KEY `FK_Tarifer_code` (`code`);

--
-- Index pour la table `traversee`
--
ALTER TABLE `traversee`
  ADD PRIMARY KEY (`numTra`),
  ADD KEY `FK_Traversee_idBat` (`idBat`),
  ADD KEY `FK_Traversee_code` (`code`);

--
-- Index pour la table `type`
--
ALTER TABLE `type`
  ADD PRIMARY KEY (`idType`),
  ADD KEY `FK_Type_lettre` (`lettre`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `admin`
--
ALTER TABLE `admin`
  MODIFY `idAdmin` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `bateau`
--
ALTER TABLE `bateau`
  MODIFY `idBat` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `liaison`
--
ALTER TABLE `liaison`
  MODIFY `code` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `port`
--
ALTER TABLE `port`
  MODIFY `idPort` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `secteur`
--
ALTER TABLE `secteur`
  MODIFY `idSecteur` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `traversee`
--
ALTER TABLE `traversee`
  MODIFY `numTra` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `type`
--
ALTER TABLE `type`
  MODIFY `idType` int(11) NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `enregistrer`
--
ALTER TABLE `enregistrer`
  ADD CONSTRAINT `FK_Enregistrer_idType` FOREIGN KEY (`idType`) REFERENCES `type` (`idType`),
  ADD CONSTRAINT `FK_Enregistrer_numRes` FOREIGN KEY (`numRes`) REFERENCES `reservation` (`numRes`);

--
-- Contraintes pour la table `liaison`
--
ALTER TABLE `liaison`
  ADD CONSTRAINT `FK_Liaison_idPort_Arrivee` FOREIGN KEY (`idPort_Arrivee`) REFERENCES `port` (`idPort`),
  ADD CONSTRAINT `FK_Liaison_idPort_Depart` FOREIGN KEY (`idPort_Depart`) REFERENCES `port` (`idPort`),
  ADD CONSTRAINT `FK_Liaison_idSecteur` FOREIGN KEY (`idSecteur`) REFERENCES `secteur` (`idSecteur`);

--
-- Contraintes pour la table `reservation`
--
ALTER TABLE `reservation`
  ADD CONSTRAINT `FK_Reservation_numTra` FOREIGN KEY (`numTra`) REFERENCES `traversee` (`numTra`);

--
-- Contraintes pour la table `tarifer`
--
ALTER TABLE `tarifer`
  ADD CONSTRAINT `FK_Tarifer_code` FOREIGN KEY (`code`) REFERENCES `liaison` (`code`),
  ADD CONSTRAINT `FK_Tarifer_dateDeb` FOREIGN KEY (`dateDeb`) REFERENCES `periode` (`dateDeb`),
  ADD CONSTRAINT `FK_Tarifer_idType` FOREIGN KEY (`idType`) REFERENCES `type` (`idType`);

--
-- Contraintes pour la table `traversee`
--
ALTER TABLE `traversee`
  ADD CONSTRAINT `FK_Traversee_code` FOREIGN KEY (`code`) REFERENCES `liaison` (`code`),
  ADD CONSTRAINT `FK_Traversee_idBat` FOREIGN KEY (`idBat`) REFERENCES `bateau` (`idBat`);

--
-- Contraintes pour la table `type`
--
ALTER TABLE `type`
  ADD CONSTRAINT `FK_Type_lettre` FOREIGN KEY (`lettre`) REFERENCES `categorie` (`lettre`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
