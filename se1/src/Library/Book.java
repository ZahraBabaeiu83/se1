package Library;

import java.io.Serializable;

class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private String title;
    private String author;
    private int publishYear;
    private final String registeredBy;

    public Book(int id, String title, String author, int publishYear, String registeredBy) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.publishYear = publishYear;
        this.registeredBy = registeredBy;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(int publishYear) {
        this.publishYear = publishYear;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public String toDisplayString() {
        return "کد: " + id
                + " | عنوان: " + title
                + " | نویسنده: " + author
                + " | سال نشر: " + publishYear
                + " | ثبت‌کننده: " + registeredBy;
    }

    public boolean matches(String titlePart, String authorPart, Integer year) {
        if (titlePart != null && !titlePart.isBlank()
                && !containsIgnoreCase(title, titlePart.trim())) {
            return false;
        }
        if (authorPart != null && !authorPart.isBlank()
                && !containsIgnoreCase(author, authorPart.trim())) {
            return false;
        }
        if (year != null && publishYear != year) {
            return false;
        }
        return true;
    }

    public boolean matchesTitle(String titlePart) {
        return titlePart == null || titlePart.isBlank()
                || containsIgnoreCase(title, titlePart.trim());
    }

    private static boolean containsIgnoreCase(String text, String part) {
        return text != null && text.toLowerCase().contains(part.toLowerCase());
    }
}
