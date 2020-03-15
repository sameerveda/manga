package sam.manga;

import sam.config.EnvHelper;
import sam.config.Properties2;

//VERSION = 1.2
//REQUIRED = 1509527940769-my-config.properties

public final class Env {
	

	static {
		Properties2 c = EnvHelper.read(Env.class, "5fe118d3-6e29-40fe-bd03-f16097cc2bef.properties");

		COMMONS_DIR = c.get("COMMONS_DIR");
		MANGA_DIR = c.get("MANGA_DIR");
		MANGA_DATA_DIR = c.get("MANGA_DATA_DIR");
		MANGAROCK_INPUT_DB = c.get("MANGAROCK_INPUT_DB");
		MANGAROCK_INPUT_DIR = c.get("MANGAROCK_INPUT_DIR");
		MANGAROCK_DB_BACKUP = c.get("MANGAROCK_DB_BACKUP"); 
		SAMROCK_DB_OLD = c.get("SAMROCK_DB_OLD");
		SAMROCK_DB = c.get("SAMROCK_DB");
		SAMROCK_THUMBS_DIR = c.get("SAMROCK_THUMBS_DIR");
		NEW_MANGAS_TSV_FILE = c.get("NEW_MANGAS_TSV_FILE");
		UPDATED_MANGAS_TSV_FILE = c.get("UPDATED_MANGAS_TSV_FILE");
		MISSING_CHAPTERS_FILE = c.get("MISSING_CHAPTERS_FILE");
		
		EnvHelper.printMissing(Env.class);
	}

	public static final String 

	COMMONS_DIR,

	MANGA_DIR,
	MANGA_DATA_DIR,
	MANGAROCK_INPUT_DB,
	MANGAROCK_INPUT_DIR,

	SAMROCK_DB_OLD,
	SAMROCK_DB,
	SAMROCK_THUMBS_DIR,

	NEW_MANGAS_TSV_FILE,
	UPDATED_MANGAS_TSV_FILE,
	MISSING_CHAPTERS_FILE,
	MANGAROCK_DB_BACKUP;
	
	public static String get(String key) {
		switch (key) {
			case "COMMONS_DIR": return COMMONS_DIR;
			case "MANGA_DIR": return MANGA_DIR;
			case "MANGA_DATA_DIR": return MANGA_DATA_DIR;
			case "MANGAROCK_INPUT_DB": return MANGAROCK_INPUT_DB;
			case "MANGAROCK_INPUT_DIR": return MANGAROCK_INPUT_DIR;
			case "MANGAROCK_DB_BACKUP": return MANGAROCK_DB_BACKUP;
			case "SAMROCK_DB_OLD": return SAMROCK_DB_OLD;
			case "SAMROCK_DB": return SAMROCK_DB;
			case "SAMROCK_THUMBS_DIR": return SAMROCK_THUMBS_DIR;
			case "NEW_MANGAS_TSV_FILE": return NEW_MANGAS_TSV_FILE;
			case "UPDATED_MANGAS_TSV_FILE": return UPDATED_MANGAS_TSV_FILE;
			case "MISSING_CHAPTERS_FILE": return MISSING_CHAPTERS_FILE;
			default: throw new IllegalArgumentException("unnknown key: \""+key+"\"");
		}
	}
}
