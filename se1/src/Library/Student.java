package Library;

class Student extends User {

    private static final long serialVersionUID = 1L;

    private boolean active = true;
    private String studentNumber = "";

    public Student(int id, String username, String password, String fullName) {
        super(id, username, password, fullName);
    }

    @Override
    public String getRoleName() {
        return "دانشجو";
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber == null ? "" : studentNumber.trim();
    }

    public String statusFa() {
        return active ? "فعال" : "غیرفعال";
    }
}
