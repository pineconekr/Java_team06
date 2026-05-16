# 인터페이스 계약서

## 공용 모델 (팀 전원 공동 정의)

| 클래스 | 주요 필드 | 비고 |
|---|---|---|
| `Book` | isbn, title, author, category, status(BookStatus) | |
| `Member` | memberId, name, grade(MemberGrade), currentBorrowCount, suspendedUntil | |
| `Loan` | loanId, memberId, isbn, borrowDate, dueDate, returnDate | 대출 기간 14일 고정 |
| `BookStatus` | AVAILABLE / BORROWED / RESERVED | enum |
| `MemberGrade` | REGULAR(3권) / PREMIUM(5권) | enum, 괄호는 최대 대출 수 |

---

## 담당별 노출 인터페이스

### 김근형 — 대출 로직 · 메인 UI

| 클래스 | 메서드 | 반환 | 설명 |
|---|---|---|---|
| `BorrowSvc` | `borrow(memberId, isbn)` | `BorrowResult` | 대출 처리 |
| `BorrowSvc` | `canBorrow(memberId)` | `boolean` | 대출 가능 여부 |

---

### 서솔빈 — 반납 · 연체 패널티 · 대출 이력

| 클래스 | 메서드 | 반환 | 설명 |
|---|---|---|---|
| `ReturnSvc` | `returnBook(loanId)` | `ReturnInfo` | 반납 + 패널티 적용 |
| `ReturnSvc` | `returnByIsbn(isbn)` | `ReturnInfo` | ISBN으로 반납 |
| `HistorySvc` | `getHistory(memberId)` | `List<Loan>` | 전체 이력 |
| `HistorySvc` | `getActive(memberId)` | `List<Loan>` | 대출 중 목록 |
| `HistorySvc` | `getOverdue()` | `List<Loan>` | 전체 연체 목록 |

---

### 조준수 — 검색 알고리즘 · 검색 UI

| 클래스 | 메서드 | 반환 | 설명 |
|---|---|---|---|
| `SearchSvc` | `searchByTitle(keyword)` | `List<Book>` | 제목 검색 |
| `SearchSvc` | `searchByAuthor(keyword)` | `List<Book>` | 저자 검색 |
| `SearchSvc` | `searchByIsbn(isbn)` | `Optional<Book>` | ISBN 검색 |
| `SearchSvc` | `searchByCategory(category)` | `List<Book>` | 카테고리 검색 |

---

### 이재원 — 도서 등록·수정·삭제 · 관리자 화면

| 클래스 | 메서드 | 반환 | 설명 |
|---|---|---|---|
| `BookAdminSvc` | `addBook(book)` | `void` | 도서 등록 |
| `BookAdminSvc` | `updateBook(isbn, book)` | `boolean` | 도서 수정 |
| `BookAdminSvc` | `deleteBook(isbn)` | `boolean` | 도서 삭제 |

---

## 패널티 규칙 (전원 공유)

- 연체일 × 2일 = 대출 정지 기간
- 정지 중 대출 시도 시 `BorrowSvc.canBorrow()` 가 `false` 반환
- 정지 기간은 `Member.suspendedUntil` 에 저장
