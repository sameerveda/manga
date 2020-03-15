package sam.manga.mangarock;



public interface MangarockMangaMeta {
    String MANGA_TABLE_NAME = "Manga";

    String AUTHOR = "author";    // author 	VARCHAR
    String CATEGORIES = "categories";    // categories 	VARCHAR
    String THUMBNAILURL = "thumbnailUrl";    // thumbnailUrl 	VARCHAR
    String DESCRIPTION = "description";    // description 	VARCHAR
    String SOURCE_ID = "source_id";    // source_id 	INTEGER
    String NAME = "name";    // name 	VARCHAR
    String TOTALCHAPTERS = "totalChapters";    // totalChapters 	INTEGER
    String LASTUPDATE = "lastUpdate";    // lastUpdate 	BIGINT
    String LAST_VIEW = "last_view";    // last_view 	BIGINT
    String ID = "_id";    // _id 	INTEGER
    String DIRECTION = "direction";    // direction 	INTEGER
    String REMOVED = "removed";    // removed 	SMALLINT
    String STATUS = "status";    // status 	SMALLINT
    String RANK = "rank";    // rank 	INTEGER
    String OID = "oid";    // oid 	TEXT
    String GENRES = "genres";    // genres 	TEXT
    String EXTRA = "extra";    // extra 	TEXT
    String COVER = "cover";    // cover 	TEXT
    String CHARACTERS = "characters";    // characters 	TEXT
    String AUTHORS = "authors";    // authors 	TEXT
    String ARTWORKS = "artworks";    // artworks 	TEXT
    String MRS_SERIES = "mrs_series";    // mrs_series 	TEXT
    String ALIAS = "alias";    // alias 	TEXT


String CREATE_TABLE_SQL = "CREATE TABLE `Manga` (\n"+
"	`author`	VARCHAR,\n"+
"	`categories`	VARCHAR,\n"+
"	`thumbnailUrl`	VARCHAR,\n"+
"	`description`	VARCHAR,\n"+
"	`source_id`	INTEGER,\n"+
"	`name`	VARCHAR,\n"+
"	`totalChapters`	INTEGER,\n"+
"	`lastUpdate`	BIGINT,\n"+
"	`last_view`	BIGINT,\n"+
"	`_id`	INTEGER,\n"+
"	`direction`	INTEGER,\n"+
"	`removed`	SMALLINT,\n"+
"	`status`	SMALLINT,\n"+
"	`rank`	INTEGER,\n"+
"	`oid`	TEXT,\n"+
"	`genres`	TEXT,\n"+
"	`extra`	TEXT,\n"+
"	`cover`	TEXT,\n"+
"	`characters`	TEXT,\n"+
"	`authors`	TEXT,\n"+
"	`artworks`	TEXT,\n"+
"	`mrs_series`	TEXT,\n"+
"	`alias`	TEXT,\n"+
"	PRIMARY KEY(`_id`)\n"+
");\n";

}