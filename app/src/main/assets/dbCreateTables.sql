DROP TABLE IF EXISTS `revlog`;
CREATE TABLE IF NOT EXISTS `revlog` (
	`id`	integer,
	`cid`	integer NOT NULL,
	`usn`	integer NOT NULL,
	`ease`	integer NOT NULL,
	`ivl`	integer NOT NULL,
	`lastIvl`	integer NOT NULL,
	`factor`	integer NOT NULL,
	`time`	integer NOT NULL,
	`type`	integer NOT NULL,
	PRIMARY KEY(`id`)
);
DROP TABLE IF EXISTS `notes`;
CREATE TABLE IF NOT EXISTS `notes` (
	`id`	integer,
	`guid`	text NOT NULL,
	`mid`	integer NOT NULL,
	`mod`	integer NOT NULL,
	`usn`	integer NOT NULL,
	`tags`	text NOT NULL,
	`flds`	text NOT NULL,
	`sfld`	integer NOT NULL,
	`csum`	integer NOT NULL,
	`flags`	integer NOT NULL,
	`data`	text NOT NULL,
	PRIMARY KEY(`id`)
);
DROP TABLE IF EXISTS `graves`;
CREATE TABLE IF NOT EXISTS `graves` (
	`usn`	integer NOT NULL,
	`oid`	integer NOT NULL,
	`type`	integer NOT NULL
);
DROP TABLE IF EXISTS `col`;
CREATE TABLE IF NOT EXISTS `col` (
	`id`	integer,
	`crt`	integer NOT NULL,
	`mod`	integer NOT NULL,
	`scm`	integer NOT NULL,
	`ver`	integer NOT NULL,
	`dty`	integer NOT NULL,
	`usn`	integer NOT NULL,
	`ls`	integer NOT NULL,
	`conf`	text NOT NULL,
	`models`	text NOT NULL,
	`decks`	text NOT NULL,
	`dconf`	text NOT NULL,
	`tags`	text NOT NULL,
	PRIMARY KEY(`id`)
);
DROP TABLE IF EXISTS `cards`;
CREATE TABLE IF NOT EXISTS `cards` (
	`id`	integer,
	`nid`	integer NOT NULL,
	`did`	integer NOT NULL,
	`ord`	integer NOT NULL,
	`mod`	integer NOT NULL,
	`usn`	integer NOT NULL,
	`type`	integer NOT NULL,
	`queue`	integer NOT NULL,
	`due`	integer NOT NULL,
	`ivl`	integer NOT NULL,
	`factor`	integer NOT NULL,
	`reps`	integer NOT NULL,
	`lapses`	integer NOT NULL,
	`left`	integer NOT NULL,
	`odue`	integer NOT NULL,
	`odid`	integer NOT NULL,
	`flags`	integer NOT NULL,
	`data`	text NOT NULL,
	PRIMARY KEY(`id`)
);
DROP INDEX IF EXISTS `ix_revlog_usn`;
CREATE INDEX IF NOT EXISTS `ix_revlog_usn` ON `revlog` (
	`usn`
);
DROP INDEX IF EXISTS `ix_revlog_cid`;
CREATE INDEX IF NOT EXISTS `ix_revlog_cid` ON `revlog` (
	`cid`
);
DROP INDEX IF EXISTS `ix_notes_usn`;
CREATE INDEX IF NOT EXISTS `ix_notes_usn` ON `notes` (
	`usn`
);
DROP INDEX IF EXISTS `ix_notes_csum`;
CREATE INDEX IF NOT EXISTS `ix_notes_csum` ON `notes` (
	`csum`
);
DROP INDEX IF EXISTS `ix_cards_usn`;
CREATE INDEX IF NOT EXISTS `ix_cards_usn` ON `cards` (
	`usn`
);
DROP INDEX IF EXISTS `ix_cards_sched`;
CREATE INDEX IF NOT EXISTS `ix_cards_sched` ON `cards` (
	`did`,
	`queue`,
	`due`
);
DROP INDEX IF EXISTS `ix_cards_nid`;
CREATE INDEX IF NOT EXISTS `ix_cards_nid` ON `cards` (
	`nid`
);