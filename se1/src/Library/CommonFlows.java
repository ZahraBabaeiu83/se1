package Library;

import java.util.List;

class CommonFlows {

    private CommonFlows() {
    }

    static void changePassword(LibraryService service, User user) throws LibraryException {
        String current = InputUtil.readNonEmpty("رمز عبور فعلی: ");
        String newPassword = InputUtil.readNonEmpty("رمز عبور جدید (حداقل ۴ نویسه): ");
        String confirm = InputUtil.readNonEmpty("تکرار رمز عبور جدید: ");
        service.changePassword(user, current, newPassword, confirm);
        System.out.println("رمز عبور با موفقیت تغییر کرد.");
    }

    static List<Book> searchBooksFlow(LibraryService service, boolean withFullCriteria) {
        String title = InputUtil.readLine("بخشی از عنوان کتاب (خالی = همه): ");
        String author = null;
        Integer year = null;
        if (withFullCriteria) {
            author = InputUtil.readLine("بخشی از نام نویسنده (خالی = بی‌توجه): ");
            year = InputUtil.readOptionalInt("سال نشر (خالی = بی‌توجه): ");
        }
        List<Book> results = withFullCriteria
                ? service.searchBooks(title, author, year)
                : service.searchBooksByTitle(title);
        if (results.isEmpty()) {
            System.out.println("کتابی یافت نشد.");
            return results;
        }
        for (Book book : results) {
            String line = book.toDisplayString();
            if (withFullCriteria) {
                line += " | وضعیت امانت: "
                        + (service.isBookAvailable(book.getId()) ? "موجود" : "در امانت است");
            }
            System.out.println(line);
        }
        System.out.println("تعداد نتایج: " + results.size());
        return results;
    }
}
