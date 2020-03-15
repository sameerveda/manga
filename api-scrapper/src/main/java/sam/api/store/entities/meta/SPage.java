package sam.api.store.entities.meta;

public interface SPage {
	int getId();
    int getOrder();
    String getPageUrl();
    String getImgUrl();
    void setImgUrl(String url);
}