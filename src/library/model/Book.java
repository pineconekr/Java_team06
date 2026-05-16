package library.model;
public class Book {
    private String isbn, title, author, category;
    private BookStatus status = BookStatus.AVAILABLE;
    public Book(String isbn, String title, String author, String category) {
        this.isbn = isbn; this.title = title; this.author = author; this.category = category;
    }
    public String getIsbn()       { return isbn; }
    public String getTitle()      { return title; }
    public String getAuthor()     { return author; }
    public String getCategory()   { return category; }
    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus s) { this.status = s; }
}
