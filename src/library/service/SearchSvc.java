package library.service;

import library.model.Book;
import library.repository.IBookRepository;
import library.repository.InMemoryBookRepository;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// 도서 검색 서비스 (저장소 기반 - 등록/수집된 도서가 그대로 검색됨)
public class SearchSvc {

    private final IBookRepository bookRepo;

    // 기본 생성자 (싱글톤 인메모리 저장소 바인딩 - 단독 사용 시 호환용)
    public SearchSvc() {
        this(InMemoryBookRepository.getInstance());
    }

    // 조립 지점(Main)에서 공용 저장소를 주입 - DB 전환 시에도 이 생성자만 사용
    public SearchSvc(IBookRepository bookRepo) {
        this.bookRepo = bookRepo;
    }

    public List<Book> searchByTitle(String keyword)     { return filter(b -> has(b.getTitle(), keyword)); }
    public List<Book> searchByAuthor(String keyword)    { return filter(b -> has(b.getAuthor(), keyword)); }
    public List<Book> searchByIsbn(String isbn)         { return filter(b -> isbn.equals(b.getIsbn())); }
    public List<Book> searchByCategory(String category) { return filter(b -> has(b.getCategory(), category)); }

    public List<Book> searchAll(String keyword) {
        return filter(b ->
                has(b.getTitle(), keyword) ||
                has(b.getAuthor(), keyword) ||
                has(b.getIsbn(), keyword) ||
                has(b.getCategory(), keyword));
    }

    /** 저장소 전체에서 조건에 맞는 도서를 추린다. (검색 대상 = 실제 등록된 도서) */
    private List<Book> filter(Predicate<Book> predicate) {
        return bookRepo.findAll().stream().filter(predicate).collect(Collectors.toList());
    }

    /** null 안전 부분 일치 (API 수집 데이터에 빈 값이 있어도 안전) */
    private static boolean has(String field, String keyword) {
        return field != null && field.contains(keyword);
    }
}
