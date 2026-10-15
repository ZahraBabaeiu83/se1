package Library;

class Employee extends User {

    private static final long serialVersionUID = 1L;

    public Employee(int id, String username, String password, String fullName) {
        super(id, username, password, fullName);
    }

    @Override
    public String getRoleName() {
        return "کارمند کتابخانه";
    }
}
