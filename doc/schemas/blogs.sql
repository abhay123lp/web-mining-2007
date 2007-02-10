/* Creates the blogs table
   containing mapping between id and url
*/

CREATE TABLE `blogs` (
  `id` int(11) NOT NULL auto_increment,
  `url` varchar(80) default NULL,
  PRIMARY KEY  (`id`),
  KEY `url` (`url`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;