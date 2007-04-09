CREATE TABLE  `web-mining`.`Frontier` (
  `Url` text NOT NULL,
  `StatusCode` varchar(100) NOT NULL default 'NOT_FETCHED',
  `FileName` varchar(100) NOT NULL default '',
  `Id` bigint(20) unsigned NOT NULL auto_increment,
  PRIMARY KEY  (`Id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1
