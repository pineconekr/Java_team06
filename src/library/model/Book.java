package library.model;
public class Book {
    private String isbn, title, author, category, coverUrl;
    private BookStatus status = BookStatus.AVAILABLE;
    public Book(String isbn, String title, String author, String category, String coverUrl) {
        this.isbn = isbn; this.title = title; this.author = author; this.category = category; this.coverUrl = coverUrl;
    }
    public String getIsbn()       { return isbn; }
    public String getTitle()      { return title; }
    public String getAuthor()     { return author; }
    public String getCategory()   { return category; }
    public String getCoverUrl()   { return coverUrl; }
    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus s) { this.status = s; }
    public void setCoverUrl(String coverUrl) {this.coverUrl = coverUrl;}
}
