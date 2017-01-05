-- MySQL dump 10.13  Distrib 5.1.73, for redhat-linux-gnu (x86_64)
--
-- Host: localhost    Database: biominer
-- ------------------------------------------------------
-- Server version	5.1.73

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `Institute`
--

LOCK TABLES `Institute` WRITE;
/*!40000 ALTER TABLE `Institute` DISABLE KEYS */;
INSERT INTO `Institute` VALUES (1,'University of Utah');
/*!40000 ALTER TABLE `Institute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `Lab`
--

LOCK TABLES `Lab` WRITE;
/*!40000 ALTER TABLE `Lab` DISABLE KEYS */;
INSERT INTO `Lab` VALUES (5,'Brett.Milash@hci.utah.edu','CvDC Admin','CvDC Admin',8012135602);
/*!40000 ALTER TABLE `Lab` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `Permission`
--

LOCK TABLES `Permission` WRITE;
/*!40000 ALTER TABLE `Permission` DISABLE KEYS */;
INSERT INTO `Permission` VALUES (1,'user:modify'),(2,'user:delete'),(3,'user:create'),(4,'lab:create'),(5,'lab:modify'),(6,'lab:delete'),(7,'lab:view'),(8,'user:view');
/*!40000 ALTER TABLE `Permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `Role`
--

LOCK TABLES `Role` WRITE;
/*!40000 ALTER TABLE `Role` DISABLE KEYS */;
INSERT INTO `Role` VALUES (1,NULL,'admin'),(2,NULL,'manager');
/*!40000 ALTER TABLE `Role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `RolePermission`
--

LOCK TABLES `RolePermission` WRITE;
/*!40000 ALTER TABLE `RolePermission` DISABLE KEYS */;
INSERT INTO `RolePermission` VALUES (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),(1,8),(2,1),(2,2),(2,3),(2,8);
/*!40000 ALTER TABLE `RolePermission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `User`
--

LOCK TABLES `User` WRITE;
/*!40000 ALTER TABLE `User` DISABLE KEYS */;
INSERT INTO `User` VALUES (45,'admin@admin.edu','admin',NULL,NULL,'Y','admin','-2405e61aa3f9f3ec442121134ccb3f8e09497416e011b36c',1111111111,'-79c625a23d2b9a6db87d0e526878a48aad4cab79c9e6434d7c95fe82a46ff8626a37d5f23d4ce8129a02562de4c4539cba75b367785f0ca7ac576e38c987c317','admin');
/*!40000 ALTER TABLE `User` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `UserInstitute`
--

LOCK TABLES `UserInstitute` WRITE;
/*!40000 ALTER TABLE `UserInstitute` DISABLE KEYS */;
INSERT INTO `UserInstitute` VALUES (45,1);
/*!40000 ALTER TABLE `UserInstitute` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `UserLab`
--

LOCK TABLES `UserLab` WRITE;
/*!40000 ALTER TABLE `UserLab` DISABLE KEYS */;
INSERT INTO `UserLab` VALUES (45,5);
/*!40000 ALTER TABLE `UserLab` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `UserRole`
--

LOCK TABLES `UserRole` WRITE;
/*!40000 ALTER TABLE `UserRole` DISABLE KEYS */;
INSERT INTO `UserRole` VALUES (45,1);
/*!40000 ALTER TABLE `UserRole` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

--
-- Dumping data for table `SampleType`
--

LOCK TABLES `SampleType` WRITE;
/*!40000 ALTER TABLE `SampleType` DISABLE KEYS */;
INSERT INTO `SampleType` VALUES (1,'DNA'),(2,'RNA'),(3,'Methylated DNA');
/*!40000 ALTER TABLE `SampleType` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `AnalysisType`
--

LOCK TABLES `AnalysisType` WRITE;
/*!40000 ALTER TABLE `AnalysisType` DISABLE KEYS */;
INSERT INTO `AnalysisType` VALUES (1,'REGION','ChIPSeq'),(2,'GENE,REGION','RNASeq'),(3,'REGION','Methylation'),(4,'VARIANT,REGION','Variant');
/*!40000 ALTER TABLE `AnalysisType` ENABLE KEYS */;
UNLOCK TABLES;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-12-22  9:40:10
