package Library;

class Manager extends User {

    private static final long serialVersionUID = 1L;

    public Manager(int id, String username, String password, String fullName) {
        super(id, username, password, fullName);
    }

    @Override
    public String getRoleName() {
        return "مدیر سیستم";
    }
}
