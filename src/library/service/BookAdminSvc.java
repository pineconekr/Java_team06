package library.service;

import java.util.List;
import java.util.Optional;
import library.api.LibraryAPICollector;
import library.model.Book;
import library.model.BookStatus;
import library.repository.IBookRepository;
import library.repository.InMemoryBookRepository;

/**
 * 도서 관리자 서비스 (도서 수동 등록 및 국립중앙도서관 OpenAPI 연동 수집)
 */
public class BookAdminSvc {

    private final IBookRepository bookRepo;
    private final LibraryAPICollector apiCollector;

    // 기본 생성자 (싱글톤 인메모리 저장소 및 API 수집기 기본 바인딩)
    public BookAdminSvc() {
        this(InMemoryBookRepository.getInstance(), new LibraryAPICollector());
    }

    // 외부 주입용 생성자 (향후 DB 구현체나 모크 객체 변경 대비)
    public BookAdminSvc(IBookRepository bookRepo, LibraryAPICollector apiCollector) {
        this.bookRepo = bookRepo;
        this.apiCollector = apiCollector;
    }

    /**
     * 1. 관리자가 시스템에 도서를 수동으로 직접 등록합니다.
     */
    public boolean registerBook(Book book) {
        if (book == null || book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
            return false;
        }

        // 중복 ISBN 검증
        Optional<Book> existingBook = bookRepo.findByIsbn(book.getIsbn());
        if (existingBook.isPresent()) {
            return false; // 이미 등록된 도서
        }

        // 기본 상태를 AVAILABLE로 보장하며 저장소에 추가
        if (book.getStatus() == null) {
            book.setStatus(BookStatus.AVAILABLE);
        }
        bookRepo.add(book);
        return true;
    }

    /**
     * 2. 국립중앙도서관 OpenAPI를 통해 키워드로 도서를 검색하고,
     * 검색된 도서들을 시스템 저장소에 자동으로 일괄 등록(수집)합니다.
     */
    public int collectAndRegisterFromAPI(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return 0;
        }

        // 외부 API로부터 도서 리스트 수집
        List<Book> collectedBooks = apiCollector.collect(keyword);
        int successCount = 0;

        for (Book book : collectedBooks) {
            // 이미 저장소에 존재하는 ISBN인지 체크하여 중복 등록 방지
            if (bookRepo.findByIsbn(book.getIsbn()).isEmpty()) {
                bookRepo.add(book);
                successCount++;
            }
        }

        return successCount;
    }

    /**
     * 3. ISBN으로 단일 도서 상세 정보를 조회합니다.
     */
    public Optional<Book> getBookDetails(String isbn) {
        return bookRepo.findByIsbn(isbn);
    }

    /**
     * 4. 현재 시스템에 저장되어 있는 모든 도서 목록을 반환합니다.
     */
    public List<Book> getAllRegisteredBooks() {
        return bookRepo.findAll();
    }

    /**
     * 5. 기존 도서의 정보를 새 Book 객체로 교체합니다 (ISBN 기준).
     * Book 모델에 setter가 부족하므로 신규 객체로 덮어쓰는 방식 사용.
     */
    public boolean updateBook(String isbn, Book newBook) {
        if (isbn == null || newBook == null) return false;

        Optional<Book> existing = bookRepo.findByIsbn(isbn);
        if (existing.isEmpty()) return false;

        // 기존 상태 유지하며 교체 (대출 중인 책의 상태가 리셋되지 않도록)
        if (newBook.getStatus() == null) {
            newBook.setStatus(existing.get().getStatus());
        }
        bookRepo.add(newBook); // InMemoryBookRepository.add는 isbn 기준 put이라 덮어쓰기 동작
        return true;
    }

    /**
     * 6. ISBN으로 도서를 삭제합니다.
     */
    public boolean deleteBook(String isbn) {
        if (isbn == null) return false;
        return bookRepo.delete(isbn);
    }
}
