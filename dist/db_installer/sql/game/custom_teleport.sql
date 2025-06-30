CREATE TABLE IF NOT EXISTS `custom_teleport` (
  `Description` varchar(75) DEFAULT NULL,
  `id` mediumint(7) unsigned NOT NULL DEFAULT '0',
  `loc_x` mediumint(6) DEFAULT NULL,
  `loc_y` mediumint(6) DEFAULT NULL,
  `loc_z` mediumint(6) DEFAULT NULL,
  `price` int(10) unsigned DEFAULT NULL,
  `fornoble` tinyint(1) NOT NULL DEFAULT '0',
  `itemId` smallint(5) unsigned NOT NULL DEFAULT '57',
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO `custom_teleport` VALUES
('Rune -> Primeval Isle',60001,10468,-24569,-3650,10000000,0,57),
('Primeval Isle -> Rune',60002,43835,-47749,-792,10000000,0,57);