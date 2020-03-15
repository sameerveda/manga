package sam.api;

import java.io.IOException;
import java.nio.file.Path;

public interface FileUtils {
	void deleteIfExists(Path path) throws IOException;
	void createDirectories(Path dir) throws IOException;
}