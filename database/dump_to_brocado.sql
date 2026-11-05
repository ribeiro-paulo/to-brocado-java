-- MySQL dump 10.13  Distrib 8.0.36, for Linux (x86_64)
--
-- Host: localhost    Database: to_brocado
-- ------------------------------------------------------
-- Server version	26.10.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '1b13763f-d610-11f1-807a-047c1655ed60:1-666';

--
-- Table structure for table `bill`
--

DROP TABLE IF EXISTS `bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `table_id` bigint unsigned NOT NULL,
  `table_number` smallint unsigned NOT NULL,
  `waiter_id` bigint unsigned DEFAULT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `service_included` tinyint(1) NOT NULL,
  `service_rate` decimal(5,4) NOT NULL DEFAULT '0.1000',
  `service_fee` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `people` tinyint unsigned NOT NULL,
  `per_person` decimal(12,2) NOT NULL,
  `closed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_bill_closed_at` (`closed_at`),
  KEY `idx_bill_table` (`table_id`),
  KEY `idx_bill_waiter` (`waiter_id`),
  CONSTRAINT `fk_bill_table` FOREIGN KEY (`table_id`) REFERENCES `restaurant_table` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_bill_waiter` FOREIGN KEY (`waiter_id`) REFERENCES `waiter` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_bill_people` CHECK ((`people` between 1 and 99)),
  CONSTRAINT `chk_bill_per_person` CHECK ((`per_person` = round((`total` / `people`),2))),
  CONSTRAINT `chk_bill_service_fee` CHECK ((((`service_included` = true) and (`service_fee` = round((`subtotal` * `service_rate`),2))) or ((`service_included` = false) and (`service_fee` = 0)))),
  CONSTRAINT `chk_bill_service_rate` CHECK ((`service_rate` between 0 and 1)),
  CONSTRAINT `chk_bill_total` CHECK ((`total` = (`subtotal` + `service_fee`))),
  CONSTRAINT `chk_bill_values` CHECK (((`subtotal` >= 0) and (`service_fee` >= 0) and (`total` >= 0) and (`per_person` >= 0)))
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bill`
--

LOCK TABLES `bill` WRITE;
/*!40000 ALTER TABLE `bill` DISABLE KEYS */;
INSERT INTO `bill` VALUES (1,1,1,1,264.90,1,0.1000,26.49,291.39,2,145.70,'2026-11-02 13:30:00'),(2,2,2,2,283.30,1,0.1000,28.33,311.63,3,103.88,'2026-11-01 14:30:00'),(3,3,3,3,336.20,1,0.1000,33.62,369.82,4,92.46,'2026-11-02 15:30:00'),(4,4,4,4,381.00,1,0.1000,38.10,419.10,1,419.10,'2026-11-01 16:30:00'),(5,5,5,5,230.70,1,0.1000,23.07,253.77,2,126.89,'2026-11-02 17:30:00'),(6,6,6,6,295.80,1,0.1000,29.58,325.38,3,108.46,'2026-11-01 12:30:00'),(7,7,7,7,337.60,1,0.1000,33.76,371.36,4,92.84,'2026-11-02 13:30:00'),(8,8,8,8,287.00,1,0.1000,28.70,315.70,1,315.70,'2026-11-01 14:30:00'),(9,9,9,1,299.20,1,0.1000,29.92,329.12,2,164.56,'2026-11-02 15:30:00'),(10,10,10,2,341.90,1,0.1000,34.19,376.09,3,125.36,'2026-11-01 16:30:00'),(11,11,11,1,62.00,1,0.1000,6.20,68.20,1,68.20,'2026-11-02 18:01:58'),(12,16,16,9,0.00,1,0.1000,0.00,0.00,1,0.00,'2026-11-02 19:30:14'),(13,16,16,10,0.00,1,0.1000,0.00,0.00,1,0.00,'2026-11-02 20:11:59');
/*!40000 ALTER TABLE `bill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_order`
--

DROP TABLE IF EXISTS `customer_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_order` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `table_id` bigint unsigned NOT NULL,
  `waiter_id` bigint unsigned DEFAULT NULL,
  `status` enum('PENDING','PREPARING','READY','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `cancelled_at` datetime DEFAULT NULL,
  `bill_id` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_customer_order_table_status` (`table_id`,`status`),
  KEY `idx_customer_order_created_at` (`created_at`),
  KEY `idx_customer_order_bill` (`bill_id`),
  KEY `idx_customer_order_waiter` (`waiter_id`),
  CONSTRAINT `fk_customer_order_bill` FOREIGN KEY (`bill_id`) REFERENCES `bill` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_customer_order_table` FOREIGN KEY (`table_id`) REFERENCES `restaurant_table` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_customer_order_waiter` FOREIGN KEY (`waiter_id`) REFERENCES `waiter` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_customer_order_cancelled` CHECK ((((`status` = _utf8mb4'CANCELLED') and (`cancelled_at` is not null)) or ((`status` <> _utf8mb4'CANCELLED') and (`cancelled_at` is null))))
) ENGINE=InnoDB AUTO_INCREMENT=41 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_order`
--

LOCK TABLES `customer_order` WRITE;
/*!40000 ALTER TABLE `customer_order` DISABLE KEYS */;
INSERT INTO `customer_order` VALUES (1,1,1,'DELIVERED','2026-10-01 12:07:00',NULL,1),(2,1,1,'DELIVERED','2026-10-01 13:14:00',NULL,1),(3,1,1,'DELIVERED','2026-10-01 14:21:00',NULL,1),(4,2,2,'DELIVERED','2026-10-02 15:28:00',NULL,2),(5,2,2,'DELIVERED','2026-10-02 16:35:00',NULL,2),(6,2,2,'DELIVERED','2026-10-02 17:42:00',NULL,2),(7,3,3,'DELIVERED','2026-10-03 18:49:00',NULL,3),(8,3,3,'DELIVERED','2026-10-03 11:56:00',NULL,3),(9,3,3,'DELIVERED','2026-10-03 12:03:00',NULL,3),(10,4,4,'DELIVERED','2026-10-04 13:10:00',NULL,4),(11,4,4,'DELIVERED','2026-10-04 14:17:00',NULL,4),(12,4,4,'DELIVERED','2026-10-04 15:24:00',NULL,4),(13,5,5,'DELIVERED','2026-10-05 16:31:00',NULL,5),(14,5,5,'DELIVERED','2026-10-05 17:38:00',NULL,5),(15,5,5,'DELIVERED','2026-10-05 18:45:00',NULL,5),(16,6,6,'DELIVERED','2026-10-06 11:52:00',NULL,6),(17,6,6,'DELIVERED','2026-10-06 12:59:00',NULL,6),(18,6,6,'DELIVERED','2026-10-06 13:06:00',NULL,6),(19,7,7,'DELIVERED','2026-10-07 14:13:00',NULL,7),(20,7,7,'DELIVERED','2026-10-07 15:20:00',NULL,7),(21,7,7,'DELIVERED','2026-10-07 16:27:00',NULL,7),(22,8,8,'DELIVERED','2026-10-08 17:34:00',NULL,8),(23,8,8,'DELIVERED','2026-10-08 18:41:00',NULL,8),(24,8,8,'DELIVERED','2026-10-08 11:48:00',NULL,8),(25,9,1,'DELIVERED','2026-10-09 12:55:00',NULL,9),(26,9,1,'DELIVERED','2026-10-09 13:02:00',NULL,9),(27,9,1,'DELIVERED','2026-10-09 14:09:00',NULL,9),(28,10,2,'DELIVERED','2026-10-10 15:16:00',NULL,10),(29,10,2,'DELIVERED','2026-10-10 16:23:00',NULL,10),(30,10,2,'DELIVERED','2026-10-10 17:30:00',NULL,10),(31,11,1,'DELIVERED','2026-11-02 16:20:00',NULL,11),(32,12,2,'PREPARING','2026-11-02 16:35:00',NULL,NULL),(33,13,3,'READY','2026-11-02 17:00:00',NULL,NULL),(34,14,4,'DELIVERED','2026-11-02 16:50:00',NULL,NULL),(35,15,5,'CANCELLED','2026-11-02 16:40:00','2026-11-02 16:58:00',NULL),(36,11,1,'DELIVERED','2026-11-02 16:55:00',NULL,11),(37,12,2,'PREPARING','2026-11-02 17:05:00',NULL,NULL),(38,13,3,'READY','2026-11-02 16:45:00',NULL,NULL),(39,14,4,'PENDING','2026-11-02 17:10:00',NULL,NULL),(40,15,5,'DELIVERED','2026-11-02 16:30:00',NULL,NULL);
/*!40000 ALTER TABLE `customer_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menu_item`
--

DROP TABLE IF EXISTS `menu_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `category` enum('SNACKS','SIDES','DRINKS','DESSERTS') NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `available` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `idx_menu_item_category_available` (`category`,`available`),
  CONSTRAINT `chk_menu_item_name` CHECK ((char_length(trim(`name`)) > 0)),
  CONSTRAINT `chk_menu_item_price` CHECK ((`price` > 0))
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu_item`
--

LOCK TABLES `menu_item` WRITE;
/*!40000 ALTER TABLE `menu_item` DISABLE KEYS */;
INSERT INTO `menu_item` VALUES (1,'X-Burger','SNACKS',24.90,1),(2,'X-Salada','SNACKS',27.90,1),(3,'X-Bacon','SNACKS',31.90,1),(4,'X-Egg','SNACKS',28.50,1),(5,'Hambúrguer artesanal','SNACKS',34.90,1),(6,'Hot dog tradicional','SNACKS',18.00,1),(7,'Hot dog completo','SNACKS',23.00,1),(8,'Misto quente','SNACKS',14.00,1),(9,'Sanduíche de frango','SNACKS',26.50,1),(10,'Cheeseburger duplo','SNACKS',32.90,1),(11,'Batata frita P','SIDES',12.00,1),(12,'Batata frita G','SIDES',19.00,1),(13,'Onion rings','SIDES',17.50,1),(14,'Nuggets 6 unidades','SIDES',16.00,1),(15,'Anéis de queijo','SIDES',18.00,1),(16,'Água mineral','DRINKS',4.00,1),(17,'Água com gás','DRINKS',5.00,1),(18,'Refrigerante lata','DRINKS',7.00,1),(19,'Refrigerante 600ml','DRINKS',10.00,1),(20,'Suco natural','DRINKS',9.00,1),(21,'Suco de laranja','DRINKS',11.00,1),(22,'Milk-shake chocolate','DRINKS',16.00,1),(23,'Milk-shake morango','DRINKS',16.00,1),(24,'Café','DRINKS',5.00,1),(25,'Brownie','DESSERTS',12.00,1),(26,'Petit gâteau','DESSERTS',19.00,1),(27,'Sorvete 2 bolas','DESSERTS',10.00,1),(28,'Pudim','DESSERTS',8.00,1),(29,'Açaí 300ml','DESSERTS',17.00,1),(30,'Torta de limão','DESSERTS',13.00,0);
/*!40000 ALTER TABLE `menu_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `order_id` bigint unsigned NOT NULL,
  `menu_item_id` bigint unsigned NOT NULL,
  `item_name` varchar(150) NOT NULL,
  `quantity` smallint unsigned NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order_item_order` (`order_id`),
  KEY `idx_order_item_menu_item` (`menu_item_id`),
  CONSTRAINT `fk_order_item_menu_item` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_item` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `customer_order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_order_item_name` CHECK ((char_length(trim(`item_name`)) > 0)),
  CONSTRAINT `chk_order_item_quantity` CHECK ((`quantity` between 1 and 99)),
  CONSTRAINT `chk_order_item_unit_price` CHECK ((`unit_price` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,1,4,'X-Egg',2,28.50),(2,1,11,'Batata frita P',3,12.00),(3,1,18,'Refrigerante lata',1,7.00),(4,2,7,'Hot dog completo',3,23.00),(5,2,14,'Nuggets 6 unidades',1,16.00),(6,2,21,'Suco de laranja',2,11.00),(7,3,10,'Cheeseburger duplo',1,32.90),(8,3,17,'Água com gás',2,5.00),(9,3,24,'Café',3,5.00),(10,4,13,'Onion rings',2,17.50),(11,4,20,'Suco natural',3,9.00),(12,4,27,'Sorvete 2 bolas',1,10.00),(13,5,16,'Água mineral',3,4.00),(14,5,23,'Milk-shake morango',1,16.00),(15,5,1,'X-Burger',2,24.90),(16,6,19,'Refrigerante 600ml',1,10.00),(17,6,26,'Petit gâteau',2,19.00),(18,6,4,'X-Egg',3,28.50),(19,7,22,'Milk-shake chocolate',2,16.00),(20,7,29,'Açaí 300ml',3,17.00),(21,7,7,'Hot dog completo',1,23.00),(22,8,25,'Brownie',3,12.00),(23,8,3,'X-Bacon',1,31.90),(24,8,10,'Cheeseburger duplo',2,32.90),(25,9,28,'Pudim',1,8.00),(26,9,6,'Hot dog tradicional',2,18.00),(27,9,13,'Onion rings',3,17.50),(28,10,2,'X-Salada',2,27.90),(29,10,9,'Sanduíche de frango',3,26.50),(30,10,16,'Água mineral',1,4.00),(31,11,5,'Hambúrguer artesanal',3,34.90),(32,11,12,'Batata frita G',1,19.00),(33,11,19,'Refrigerante 600ml',2,10.00),(34,12,8,'Misto quente',1,14.00),(35,12,15,'Anéis de queijo',2,18.00),(36,12,22,'Milk-shake chocolate',3,16.00),(37,13,11,'Batata frita P',2,12.00),(38,13,18,'Refrigerante lata',3,7.00),(39,13,25,'Brownie',1,12.00),(40,14,14,'Nuggets 6 unidades',3,16.00),(41,14,21,'Suco de laranja',1,11.00),(42,14,28,'Pudim',2,8.00),(43,15,17,'Água com gás',1,5.00),(44,15,24,'Café',2,5.00),(45,15,2,'X-Salada',3,27.90),(46,16,20,'Suco natural',2,9.00),(47,16,27,'Sorvete 2 bolas',3,10.00),(48,16,5,'Hambúrguer artesanal',1,34.90),(49,17,23,'Milk-shake morango',3,16.00),(50,17,1,'X-Burger',1,24.90),(51,17,8,'Misto quente',2,14.00),(52,18,26,'Petit gâteau',1,19.00),(53,18,4,'X-Egg',2,28.50),(54,18,11,'Batata frita P',3,12.00),(55,19,29,'Açaí 300ml',2,17.00),(56,19,7,'Hot dog completo',3,23.00),(57,19,14,'Nuggets 6 unidades',1,16.00),(58,20,3,'X-Bacon',3,31.90),(59,20,10,'Cheeseburger duplo',1,32.90),(60,20,17,'Água com gás',2,5.00),(61,21,6,'Hot dog tradicional',1,18.00),(62,21,13,'Onion rings',2,17.50),(63,21,20,'Suco natural',3,9.00),(64,22,9,'Sanduíche de frango',2,26.50),(65,22,16,'Água mineral',3,4.00),(66,22,23,'Milk-shake morango',1,16.00),(67,23,12,'Batata frita G',3,19.00),(68,23,19,'Refrigerante 600ml',1,10.00),(69,23,26,'Petit gâteau',2,19.00),(70,24,15,'Anéis de queijo',1,18.00),(71,24,22,'Milk-shake chocolate',2,16.00),(72,24,29,'Açaí 300ml',3,17.00),(73,25,18,'Refrigerante lata',2,7.00),(74,25,25,'Brownie',3,12.00),(75,25,3,'X-Bacon',1,31.90),(76,26,21,'Suco de laranja',3,11.00),(77,26,28,'Pudim',1,8.00),(78,26,6,'Hot dog tradicional',2,18.00),(79,27,24,'Café',1,5.00),(80,27,2,'X-Salada',2,27.90),(81,27,9,'Sanduíche de frango',3,26.50),(82,28,27,'Sorvete 2 bolas',2,10.00),(83,28,5,'Hambúrguer artesanal',3,34.90),(84,28,12,'Batata frita G',1,19.00),(85,29,1,'X-Burger',3,24.90),(86,29,8,'Misto quente',1,14.00),(87,29,15,'Anéis de queijo',2,18.00),(88,30,4,'X-Egg',1,28.50),(89,30,11,'Batata frita P',2,12.00),(90,30,18,'Refrigerante lata',3,7.00),(91,31,7,'Hot dog completo',2,23.00),(92,32,10,'Cheeseburger duplo',3,32.90),(93,33,13,'Onion rings',1,17.50),(94,34,16,'Água mineral',2,4.00),(95,35,19,'Refrigerante 600ml',3,10.00),(96,36,22,'Milk-shake chocolate',1,16.00),(97,37,25,'Brownie',2,12.00),(98,38,28,'Pudim',3,8.00),(99,39,2,'X-Salada',1,27.90),(100,40,5,'Hambúrguer artesanal',2,34.90);
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `restaurant_table`
--

DROP TABLE IF EXISTS `restaurant_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `restaurant_table` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `number` smallint unsigned NOT NULL,
  `seats` tinyint unsigned NOT NULL,
  `status` enum('FREE','OCCUPIED') NOT NULL DEFAULT 'FREE',
  `waiter_id` bigint unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_restaurant_table_number` (`number`),
  KEY `idx_restaurant_table_waiter` (`waiter_id`),
  CONSTRAINT `fk_restaurant_table_waiter` FOREIGN KEY (`waiter_id`) REFERENCES `waiter` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_restaurant_table_number` CHECK ((`number` between 1 and 999)),
  CONSTRAINT `chk_restaurant_table_seats` CHECK ((`seats` between 1 and 99))
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `restaurant_table`
--

LOCK TABLES `restaurant_table` WRITE;
/*!40000 ALTER TABLE `restaurant_table` DISABLE KEYS */;
INSERT INTO `restaurant_table` VALUES (1,1,2,'FREE',NULL),(2,2,2,'FREE',NULL),(3,3,4,'FREE',NULL),(4,4,4,'FREE',NULL),(5,5,4,'FREE',NULL),(6,6,6,'FREE',NULL),(7,7,2,'FREE',NULL),(8,8,4,'FREE',NULL),(9,9,6,'FREE',NULL),(10,10,2,'FREE',NULL),(11,11,4,'FREE',NULL),(12,12,4,'OCCUPIED',2),(13,13,2,'OCCUPIED',3),(14,14,6,'OCCUPIED',4),(15,15,4,'OCCUPIED',5),(16,16,2,'FREE',NULL),(17,19,3,'FREE',NULL);
/*!40000 ALTER TABLE `restaurant_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `vw_bill_summary`
--

DROP TABLE IF EXISTS `vw_bill_summary`;
/*!50001 DROP VIEW IF EXISTS `vw_bill_summary`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_bill_summary` AS SELECT 
 1 AS `bill_id`,
 1 AS `table_number`,
 1 AS `waiter_name`,
 1 AS `subtotal`,
 1 AS `service_included`,
 1 AS `service_rate`,
 1 AS `service_fee`,
 1 AS `total`,
 1 AS `people`,
 1 AS `per_person`,
 1 AS `closed_at`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_late_orders`
--

DROP TABLE IF EXISTS `vw_late_orders`;
/*!50001 DROP VIEW IF EXISTS `vw_late_orders`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_late_orders` AS SELECT 
 1 AS `order_id`,
 1 AS `table_id`,
 1 AS `table_number`,
 1 AS `waiter_id`,
 1 AS `waiter_name`,
 1 AS `status`,
 1 AS `created_at`,
 1 AS `elapsed_minutes`,
 1 AS `order_total`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_order_summary`
--

DROP TABLE IF EXISTS `vw_order_summary`;
/*!50001 DROP VIEW IF EXISTS `vw_order_summary`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_order_summary` AS SELECT 
 1 AS `order_id`,
 1 AS `table_id`,
 1 AS `table_number`,
 1 AS `waiter_id`,
 1 AS `waiter_name`,
 1 AS `status`,
 1 AS `created_at`,
 1 AS `elapsed_minutes`,
 1 AS `order_total`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `vw_pending_orders`
--

DROP TABLE IF EXISTS `vw_pending_orders`;
/*!50001 DROP VIEW IF EXISTS `vw_pending_orders`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `vw_pending_orders` AS SELECT 
 1 AS `order_id`,
 1 AS `table_id`,
 1 AS `table_number`,
 1 AS `waiter_id`,
 1 AS `waiter_name`,
 1 AS `status`,
 1 AS `created_at`,
 1 AS `elapsed_minutes`,
 1 AS `order_total`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `waiter`
--

DROP TABLE IF EXISTS `waiter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `waiter` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(120) NOT NULL,
  `phone` varchar(11) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  CONSTRAINT `chk_waiter_name` CHECK ((char_length(trim(`name`)) > 0)),
  CONSTRAINT `chk_waiter_phone` CHECK (regexp_like(`phone`,_utf8mb4'^[0-9]{10,11}$'))
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `waiter`
--

LOCK TABLES `waiter` WRITE;
/*!40000 ALTER TABLE `waiter` DISABLE KEYS */;
INSERT INTO `waiter` VALUES (1,'Ana Beatriz Lima','92981001001',1),(2,'Bruno Carvalho','92981001002',1),(3,'Camila Souza','92981001003',1),(4,'Daniel Oliveira','92981001004',1),(5,'Eduarda Martins','92981001005',1),(6,'Felipe Costa','92981001006',1),(7,'Gabriela Rocha','92981001007',1),(8,'Henrique Alves','92981001008',1),(9,'Paulo Victor','92994281996',0),(10,'paulinho','4002892299',0);
/*!40000 ALTER TABLE `waiter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Final view structure for view `vw_bill_summary`
--

/*!50001 DROP VIEW IF EXISTS `vw_bill_summary`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_bill_summary` AS select `b`.`id` AS `bill_id`,`b`.`table_number` AS `table_number`,`w`.`name` AS `waiter_name`,`b`.`subtotal` AS `subtotal`,`b`.`service_included` AS `service_included`,`b`.`service_rate` AS `service_rate`,`b`.`service_fee` AS `service_fee`,`b`.`total` AS `total`,`b`.`people` AS `people`,`b`.`per_person` AS `per_person`,`b`.`closed_at` AS `closed_at` from (`bill` `b` left join `waiter` `w` on((`w`.`id` = `b`.`waiter_id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_late_orders`
--

/*!50001 DROP VIEW IF EXISTS `vw_late_orders`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_late_orders` AS select `vw_pending_orders`.`order_id` AS `order_id`,`vw_pending_orders`.`table_id` AS `table_id`,`vw_pending_orders`.`table_number` AS `table_number`,`vw_pending_orders`.`waiter_id` AS `waiter_id`,`vw_pending_orders`.`waiter_name` AS `waiter_name`,`vw_pending_orders`.`status` AS `status`,`vw_pending_orders`.`created_at` AS `created_at`,`vw_pending_orders`.`elapsed_minutes` AS `elapsed_minutes`,`vw_pending_orders`.`order_total` AS `order_total` from `vw_pending_orders` where (`vw_pending_orders`.`elapsed_minutes` >= 15) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_order_summary`
--

/*!50001 DROP VIEW IF EXISTS `vw_order_summary`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_order_summary` AS select `co`.`id` AS `order_id`,`co`.`table_id` AS `table_id`,`rt`.`number` AS `table_number`,`co`.`waiter_id` AS `waiter_id`,`w`.`name` AS `waiter_name`,`co`.`status` AS `status`,`co`.`created_at` AS `created_at`,timestampdiff(MINUTE,`co`.`created_at`,now()) AS `elapsed_minutes`,coalesce(sum((`oi`.`quantity` * `oi`.`unit_price`)),0.00) AS `order_total` from (((`customer_order` `co` join `restaurant_table` `rt` on((`rt`.`id` = `co`.`table_id`))) left join `waiter` `w` on((`w`.`id` = `co`.`waiter_id`))) left join `order_item` `oi` on((`oi`.`order_id` = `co`.`id`))) group by `co`.`id`,`co`.`table_id`,`rt`.`number`,`co`.`waiter_id`,`w`.`name`,`co`.`status`,`co`.`created_at` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vw_pending_orders`
--

/*!50001 DROP VIEW IF EXISTS `vw_pending_orders`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vw_pending_orders` AS select `vw_order_summary`.`order_id` AS `order_id`,`vw_order_summary`.`table_id` AS `table_id`,`vw_order_summary`.`table_number` AS `table_number`,`vw_order_summary`.`waiter_id` AS `waiter_id`,`vw_order_summary`.`waiter_name` AS `waiter_name`,`vw_order_summary`.`status` AS `status`,`vw_order_summary`.`created_at` AS `created_at`,`vw_order_summary`.`elapsed_minutes` AS `elapsed_minutes`,`vw_order_summary`.`order_total` AS `order_total` from `vw_order_summary` where (`vw_order_summary`.`status` in ('PENDING','PREPARING','READY')) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-02 21:09:34
