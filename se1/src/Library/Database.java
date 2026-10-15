package Library;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

class Database implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String DATA_DIR = "data";

    private static final Database INSTANCE = new Database();

    private List<User> users = new ArrayList<>();
    private List<Book> books = new ArrayList<>();
    private List<LoanRequest> requests = new ArrayList<>();
    private List<Loan> loans = new ArrayList<>();

    private transient boolean firstRun = false;
    private transient boolean loaded = false;

    private Database() {
    }

    public static Database getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public void load() {
        if (loaded) {
            return;
        }
        File dir = new File(DATA_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("خطا در ایجاد پوشه داده: " + dir.getAbsolutePath());
        }

        List<User> loadedUsers = (List<User>) readObject("users.dat");
        List<Book> loadedBooks = (List<Book>) readObject("books.dat");
        List<LoanRequest> loadedRequests = (List<LoanRequest>) readObject("requests.dat");
        List<Loan> loadedLoans = (List<Loan>) readObject("loans.dat");

        users = loadedUsers != null ? loadedUsers : new ArrayList<>();
        books = loadedBooks != null ? loadedBooks : new ArrayList<>();
        requests = loadedRequests != null ? loadedRequests : new ArrayList<>();
        loans = loadedLoans != null ? loadedLoans : new ArrayList<>();

        if (loadedUsers == null) {
            users.add(new Manager(nextUserId(), "admin", "admin123", "مدیر سیستم"));
            firstRun = true;
            save();
        }
        loaded = true;
    }

    public boolean isFirstRun() {
        return firstRun;
    }

    public void save() {
        writeObject("users.dat", users);
        writeObject("books.dat", books);
        writeObject("requests.dat", requests);
        writeObject("loans.dat", loans);
    }

    private Object readObject(String fileName) {
        File file = new File(DATA_DIR, fileName);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return in.readObject();
        } catch (Exception e) {
            System.err.println("هشدار: خواندن فایل " + fileName + " ناموفق بود (" + e.getMessage() + ")");
            return null;
        }
    }

    private void writeObject(String fileName, Object object) {
        File file = new File(DATA_DIR, fileName);
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeObject(object);
        } catch (Exception e) {
            System.err.println("خطا در ذخیره فایل " + fileName + ": " + e.getMessage());
        }
    }

    public int nextUserId() {
        return users.stream().mapToInt(User::getId).max().orElse(0) + 1;
    }

    public int nextBookId() {
        return books.stream().mapToInt(Book::getId).max().orElse(0) + 1;
    }

    public int nextRequestId() {
        return requests.stream().mapToInt(LoanRequest::getId).max().orElse(0) + 1;
    }

    public int nextLoanId() {
        return loans.stream().mapToInt(Loan::getId).max().orElse(0) + 1;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<LoanRequest> getRequests() {
        return requests;
    }

    public List<Loan> getLoans() {
        return loans;
    }
}
