package sam.manga.samrock.chapters;



import static sam.manga.samrock.chapters.ChaptersMeta.CHAPTER_ID;
import static sam.manga.samrock.chapters.ChaptersMeta.NAME;
import static sam.manga.samrock.chapters.ChaptersMeta.NUMBER;
import static sam.manga.samrock.chapters.ChaptersMeta.READ;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class Chapter implements Comparable<Chapter>, MinimalChapter {
    private final int id;
    private String filename; //file name
    private double number; // number
    private boolean read; // isRead ?

    private boolean readModified, nameModified, delete, numberModified;

    public Chapter(ResultSet rs) throws SQLException {
        this.id = rs.getInt(CHAPTER_ID);
        this.filename = rs.getString(NAME);
        this.number = rs.getDouble(NUMBER);
        this.read = rs.getBoolean(READ);
    }

    /**
     * isRead will be set to false 
     * @param fileName
     */
    public Chapter(double number, String fileName, boolean isRead) {
        this.id = -1;
        this.number = number;
        this.filename = fileName;
        this.read = isRead;
    }
    public Chapter(double number, String fileName) {
        this(number, fileName, false);
    }
    @Override
    public String getFileName() {
        return filename;
    }
    /**
     * chapter id
     * @return
     */
    public int getChapterId() {
        return id;
    }
    public void setNumber(double number) {
        if(this.number == number)
            return;
        this.number = number;
        numberModified = true;
    }
    public void setFileName(String name) {
        if(Objects.equals(name, this.filename))
            return;
        Objects.requireNonNull(name);

        nameModified = true;
        this.filename = name;
    }
    public double getNumber() {
        return number;
    }
    public boolean isRead() {
        return read;
    }
    public void setRead(boolean isRead) {
        if(read == isRead)
            return;

        readModified = true;
        this.read = isRead;
    }
    public void setDeleted(boolean deleted) {
        this.delete = deleted;
    }
    public boolean isDeleted() {
        return delete;
    }
    public boolean isNumberModified() {
        return numberModified;
    }
    public boolean isReadModified() {
        return readModified;
    }
    public boolean isNameModified() {
        return nameModified;
    }
    public boolean isChapterNotInDb() {
        return id == -1;
    }
    public boolean isModified() {
        return id == -1 || nameModified || readModified || delete ;
    }
    @Override
    public int compareTo(Chapter c) {
        if(this.number == c.number)
            return this.filename.compareTo(c.filename);
        else
            return this.number < c.number ? -1 : 1;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return equals((Chapter) obj);
    }

    public boolean equals(Chapter c) {
        if(c == null)
            return false;
        if(c == this)
            return true;

        return this.id == c.id && this.id == -1 ? (this.number == c.number && this.filename.equals(c.filename)) : true;
    }
    @Override
    public String toString() {
        return "Chapter [id=" + id + ", number=" + number + ", read=" + read +", name=" + filename + "]";
    }
    
    @Override
    public final int hashCode() {
    	return id;
    }
    
    private String _filename, title;

	@Override
	public String getTitle() {
		if(_filename == filename)
			return title;
		_filename = filename;
		return title = MinimalChapter.getTitleFromFileName(filename);
	}
}
