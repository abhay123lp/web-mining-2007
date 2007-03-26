/* Creates the blogs table
   containing mapping between id and url
*/

CREATE TABLE `extblogs` (
  `id` int(11) NOT NULL auto_increment,
  `url` varchar(80) default NULL,
  `internal_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `url` (`url`),
  KEY `internal_id` (`internal_id`),
  CONSTRAINT `extblogs_ibfk_1` FOREIGN KEY (`internal_id`)
    REFERENCES `blogs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
