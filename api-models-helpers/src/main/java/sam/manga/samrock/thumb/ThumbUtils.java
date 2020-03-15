package sam.manga.samrock.thumb;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.stream.IntStream;

import sam.manga.Env;

public interface ThumbUtils {

    public class ExtraAndMissing {
        private final List<String> extraThumbNames;
        private final int[] missingThumbMangaIds;

        ExtraAndMissing(List<String> extra, int[] missing) {
            this.extraThumbNames = extra;
            this.missingThumbMangaIds = missing;
        }
        public List<String> getExtraThumbNames() {
            return extraThumbNames;
        }
        public int[] getMissingThumbMangaIds() {
            return missingThumbMangaIds;
        }
    }

    /**
     * 
     * @param allMangaIds will sorted
     * @param thumbFolder
     * @return
     */
    public static ExtraAndMissing extraAndMissingThumbs(int[] allMangaIds, File thumbFolder) {
        String[] names = thumbFolder.list();
        int[] nameToIds = new int[names.length];
        int index = 0;

        Arrays.sort(allMangaIds);

        List<String> extra = new ArrayList<>();

        for (String nm : names) {
            int n = nm.indexOf('_');
            n = n < 0 ? nm.indexOf('.') : n;

            String name = n < 0 ? nm : nm.substring(0, n);
            try {
                n = Integer.parseInt(name);
            } catch (NumberFormatException e) {
                extra.add(name);
                continue;
            }
            if(Arrays.binarySearch(allMangaIds, n) < 0)
                extra.add(nm);
            else
                nameToIds[index++] = n;
        }

        int[] temp = Arrays.copyOf(nameToIds, index);
        Arrays.sort(temp);

        int[] missing = IntStream.of(allMangaIds)
                .filter(i -> Arrays.binarySearch(temp, i) < 0)
                .toArray();

        return new ExtraAndMissing(extra, missing);
    }

    static final Path THUMB_FOLDER = Paths.get(Env.SAMROCK_THUMBS_DIR);
    
    public static Path findThumb(int mangaId) {
        Path path = THUMB_FOLDER.resolve(mangaId+".jpg");

        if(Files.exists(path))
            return path;
        
        path = THUMB_FOLDER.resolve(String.valueOf(mangaId));
        
        if(Files.notExists(path))
            return null;
        
        String[] names = path.toFile().list();
        if(names == null || names.length == 0)
            return null;
        
        return path.resolve(names[0]);
    }
}
