package library;

import library.api.LibraryAPICollector;
import library.repository.Db;
import library.repository.IBookRepository;
import library.repository.ILoanRepository;
import library.repository.IMemberRepository;
import library.repository.SqliteBookRepository;
import library.repository.SqliteLoanRepository;
import library.repository.SqliteMemberRepository;
import library.service.BookAdminSvc;
import library.service.BorrowSvc;
import library.service.HistorySvc;
import library.service.ReturnSvc;
import library.service.SearchSvc;
import library.ui.LibraryMainUI;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 애플리케이션 조립 지점(Composition Root).
 */
public class Main {

    public static void main(String[] args) {

        // ───────────────────────── 저장소 선택 (DB 스위치) ─────────────────────────
        Db db = new Db("library.db");
        IBookRepository   bookRepo   = new SqliteBookRepository(db);
        IMemberRepository memberRepo = new SqliteMemberRepository(db);
        ILoanRepository   loanRepo   = new SqliteLoanRepository(db);
        // ──────────────────────────────────────────────────────────────────────────

        LibraryAPICollector apiCollector = new LibraryAPICollector();

        BorrowSvc    borrowSvc  = new BorrowSvc(bookRepo, memberRepo, loanRepo);
        ReturnSvc    returnSvc  = new ReturnSvc(loanRepo, bookRepo, memberRepo);
        SearchSvc    searchSvc  = new SearchSvc(bookRepo);
        HistorySvc   historySvc = new HistorySvc(loanRepo, bookRepo);
        BookAdminSvc adminSvc   = new BookAdminSvc(bookRepo, apiCollector);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            // memberRepo 추가 전달
            new LibraryMainUI(borrowSvc, searchSvc, returnSvc, historySvc, adminSvc, memberRepo);
        });
    }
}
