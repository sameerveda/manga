package sam.api.config;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.json.JSONObject;
import org.json.JSONTokener;

import sam.console.ANSI;
import sam.myutils.System2;

public interface ConfigService {
	default String get(String key) {
		return get(key, null);
	}
	String get(String key, String defaultValue);
	
	default int getInt(String key, int defaultValue) {
		String s = get(key, null);
		return s == null ? defaultValue : Integer.parseInt(s.trim());
	}
	default boolean getBoolean(String key, boolean defaultValue) {
		String s = get(key, null);
		return s == null ? defaultValue : Boolean.valueOf(s.trim());
	}
	
	static Function<String, String> fromFile(Path path) throws IOException {
		String sf = path.getFileName().toString().toLowerCase();
		
		if(sf.endsWith(".properties")) {
			Properties props = new Properties();
			props.load(Files.newBufferedReader(path));
			return props::getProperty;
		} else if(sf.endsWith(".json")) {
			JSONObject json = new JSONObject(new JSONTokener(Files.newBufferedReader(path)));
			return json::optString;
		} else {
			throw new IllegalStateException("unknown file type of file: "+sf);
		}
	}
	static ConfigService defaultImpl(Path configFile, Map<String, String> argsProps, PrintStream printTo) throws IOException {
		Map<String, String> map = argsProps.isEmpty() ? Collections.emptyMap() : argsProps;
		
		Function<String, String> fromFile = Files.exists(configFile) ? fromFile(configFile) : null;
		return (key, defaultValue) -> {
			String value = map.get(key);
			if(value == null)
				value = System2.lookup(key);
			if(value == null && fromFile != null)
				value = fromFile.apply(key);

			if(value == null)
				value = defaultValue;
			
			if (printTo != null)
				printTo.println(ANSI.yellow(key) + ": " + value);
			
			return value;
		};
	}
	static Map<String, String> propertiesFromArgs(List<String> args) {
		if(args.isEmpty())
			return Collections.emptyMap();
		
		Map<String, String> props = new HashMap<>();
		args.removeIf(s -> {
			int n;
			if(s.startsWith("--") && (n = s.indexOf('=')) > 0) {
				String key = s.substring(0, n);
				if(key.indexOf(' ') < 0) {
					props.put(key, s.substring(n + 1));
					return true;	
				}
			}
			return false;
		});
		return props;
	}
}
