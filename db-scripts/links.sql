/* Create the links table
*/

CREATE TABLE `links` (
  `srcid`  int(11) NOT NULL default '0',
  `destid` int(11) NOT NULL default '0',
  `type`   int(11) default '0',
  PRIMARY KEY  (`srcid`,`destid`),
  KEY `destid` (`destid`),
  CONSTRAINT `links_ibfk_2` FOREIGN KEY (`destid`)
    REFERENCES `blogs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `links_ibfk_1` FOREIGN KEY (`srcid`)
    REFERENCES `blogs` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
