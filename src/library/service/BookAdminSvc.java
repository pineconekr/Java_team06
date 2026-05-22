package library.service;

import java.util.List;
import java.util.Optional;
import library.api.LibraryAPICollector;
import library.model.Book;
import library.model.BookStatus;
import library.repository.IBookRepository;
import library.repository.InMemoryBookRepository;

/**
 * 도서 관리자 서비스
 * - 프로젝트의 기존 String 식별자 규격을 그대로 유지하여 컴파일 에러를 방지합니다.
 * - 내부적으로는 ISBN 필수 유효성 검사 및 중복 체크를 완전히 제거했습니다.
 */
public class BookAdminSvc {

    private final IBookRepository bookRepo;
    private final LibraryAPICollector apiCollector;

    // 기본 생성자 (싱글톤 인메모리 저장소 및 API 수집기 기본 바인딩)
    public BookAdminSvc() {
        this(InMemoryBookRepository.getInstance(), new LibraryAPICollector());
    }

    // 외부 주입용 생성자
    public BookAdminSvc(IBookRepository bookRepo, LibraryAPICollector apiCollector) {
        this.bookRepo = bookRepo;
        this.apiCollector = apiCollector;
    }

    /**
     * 1. 관리자가 시스템에 도서를 수동으로 직접 등록합니다.
     * (ISBN 검증과 중복 검사를 모두 없애고 자유롭게 등록을 허용합니다.)
     */
    public boolean registerBook(Book book) {
        if (book == null) {
            return false;
        }

        // 도서 상태를 AVAILABLE(대출 가능)로 보장한 후 중복 검사 없이 바로 추가
        book.setStatus(BookStatus.AVAILABLE);
        bookRepo.add(book);
        return true;
    }

    /**
     * 2. 국립중앙도서관 OpenAPI를 통해 도서를 검색하고 대량 수집/등록합니다.
     * (기존에 존재하던 중복 ISBN 패스 로직을 없애고 수집된 데이터를 그대로 등록합니다.)
     */
    public int collectApiBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return 0;
        }

        List<Book> collectedBooks = apiCollector.collect(keyword);
        int successCount = 0;

        for (Book book : collectedBooks) {
            // 이미 존재하는 책인지 묻지도 따지지도 않고 무조건 저장소에 추가
            bookRepo.add(book);
            successCount++;
        }

        return successCount;
    }

    /**
     * 3. 특정 키값(기존 ISBN 메서드 규격 호환)으로 단일 도서 상세 정보를 조회합니다.
     */
    public Optional<Book> getBookDetails(String isbn) {
        if (isbn == null) {
            return Optional.empty();
        }
        return bookRepo.findByIsbn(isbn);
    }

    /**
     * 4. 현재 시스템에 저장되어 있는 모든 도서 목록을 반환합니다.
     */
    public List<Book> getAllRegisteredBooks() {
        return bookRepo.findAll();
    }

    /**
     * 5. 기존 도서의 정보를 새 Book 객체로 무조건 교체합니다.
     */
    public boolean updateBook(String isbn, Book newBook) {
        if (isbn == null || newBook == null) {
            return false;
        }

        Optional<Book> existing = bookRepo.findByIsbn(isbn);
        if (existing.isEmpty()) {
            return false;
        }

        Book oldBook = existing.get();
        
        // 데이터 수정 중에도 대출/연체 상태가 풀리지 않도록 기존 상태 보존
        newBook.setStatus(oldBook.getStatus());

        // 기존 식별 레코드를 삭제하고 새 데이터 객체로 완전히 교체
        bookRepo.delete(isbn);
        bookRepo.add(newBook);
        return true;
    }

    /**
     * 6. 시스템에서 도서를 완전히 삭제합니다.
     */
    public boolean deleteBook(String isbn) {
        if (isbn == null) {
            return false;
        }

        Optional<Book> bookOpt = bookRepo.findByIsbn(isbn);
        if (bookOpt.isEmpty()) {
            return false; // 삭제할 대상 도서 없음
        }

        Book book = bookOpt.get();
        
        // 최소한의 데이터 안전장치: 대출 중(BORROWED)인 도서는 반납 전까지 삭제 방지
        if (book.getStatus() == BookStatus.BORROWED) {
            return false; 
        }

        return bookRepo.delete(isbn);
    }
}