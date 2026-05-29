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
 *
 * - 저장소 인스턴스를 '한 곳에서' 만들어 모든 서비스에 주입한다.
 *   -> 등록 / 검색 / 대출 / 반납 / 이력이 동일한 데이터 소스를 공유한다(연결성).
 * - DB 전환 시 아래 '저장소 선택' 블록 세 줄만 교체하면 된다.
 *   서비스 · UI 코드는 인터페이스(IBookRepository 등)에만 의존하므로 변경 불필요.
 */
public class Main {

    public static void main(String[] args) {

        // ───────────────────────── 저장소 선택 (DB 스위치) ─────────────────────────
        // 현재: SQLite 파일 DB(library.db). 프로그램을 꺼도 데이터가 유지된다.
        // 인메모리로 되돌리려면 이 블록만 InMemory*Repository.getInstance()로 바꾸면 된다.
        Db db = new Db("library.db");                 // 연결 + 스키마 초기화 + 최초 시드
        IBookRepository   bookRepo   = new SqliteBookRepository(db);
        IMemberRepository memberRepo = new SqliteMemberRepository(db);
        ILoanRepository   loanRepo   = new SqliteLoanRepository(db);
        // ──────────────────────────────────────────────────────────────────────────

        LibraryAPICollector apiCollector = new LibraryAPICollector();

        // 모든 서비스가 위에서 만든 동일한 저장소 인스턴스를 공유한다.
        BorrowSvc    borrowSvc  = new BorrowSvc(bookRepo, memberRepo, loanRepo);
        ReturnSvc    returnSvc  = new ReturnSvc(loanRepo, bookRepo, memberRepo);
        SearchSvc    searchSvc  = new SearchSvc(bookRepo);
        HistorySvc   historySvc = new HistorySvc(loanRepo, bookRepo);
        BookAdminSvc adminSvc   = new BookAdminSvc(bookRepo, apiCollector);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // 시스템 룩앤필 적용 실패 시 기본 룩앤필로 계속 진행
            }
            new LibraryMainUI(borrowSvc, searchSvc, returnSvc, historySvc, adminSvc);
        });
    }
}
