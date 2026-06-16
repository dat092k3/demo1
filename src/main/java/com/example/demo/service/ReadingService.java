package com.example.demo.service;

import com.example.demo.dto.BookDTO;
import com.example.demo.dto.ChapterDTO;
import com.example.demo.dto.ReadingProgressDTO;
import com.example.demo.dto.ReadingProgressRequestDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class ReadingService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private UserFavoriteBookRepository userFavoriteBookRepository;

    @Autowired
    private UserReadingProgressRepository userReadingProgressRepository;

    // --- Favorite Books ---

    public void addFavoriteBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Optional<UserFavoriteBook> existing = userFavoriteBookRepository.findByUserIdAndBookId(userId, bookId);
        if (existing.isEmpty()) {
            UserFavoriteBook favorite = new UserFavoriteBook(user, book);
            userFavoriteBookRepository.save(favorite);
        }
    }

    public void removeFavoriteBook(Long userId, Long bookId) {
        Optional<UserFavoriteBook> existing = userFavoriteBookRepository.findByUserIdAndBookId(userId, bookId);
        existing.ifPresent(userFavoriteBookRepository::delete);
    }

    public List<BookDTO> getFavoriteBooks(Long userId) {
        List<UserFavoriteBook> favorites = userFavoriteBookRepository.findByUserId(userId);
        return favorites.stream()
                .map(UserFavoriteBook::getBook)
                .map(this::mapToBookDTO)
                .collect(Collectors.toList());
    }

    // --- Reading Progress ---

    public ReadingProgressDTO getReadingProgress(Long userId, Long bookId) {
        Optional<UserReadingProgress> progressOpt = userReadingProgressRepository.findByUserIdAndBookId(userId, bookId);
        if (progressOpt.isPresent()) {
            UserReadingProgress progress = progressOpt.get();
            ReadingProgressDTO dto = new ReadingProgressDTO();
            dto.setBookId(progress.getBook().getId());
            dto.setBookTitle(progress.getBook().getTitle());
            dto.setLastReadChapterId(progress.getLastReadChapter().getId());
            dto.setLastReadChapterTitle(progress.getLastReadChapter().getTitle());
            dto.setLastReadPageNumber(progress.getLastReadChapter().getPageNumber());
            dto.setLastReadDate(progress.getLastReadDate());
            return dto;
        }
        return null;
    }

    private void autoSaveReadingProgress(Chapter chapter) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return;
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return;
        }

        Book book = chapter.getBook();
        Optional<UserReadingProgress> existing = userReadingProgressRepository.findByUserIdAndBookId(user.getId(), book.getId());
        UserReadingProgress progress;
        if (existing.isPresent()) {
            progress = existing.get();
            progress.setLastReadChapter(chapter);
            progress.setLastReadDate(LocalDateTime.now());
        } else {
            progress = new UserReadingProgress(user, book, chapter);
        }
        userReadingProgressRepository.save(progress);
    }

    // --- Reading Books ---

    public List<ChapterDTO> getBookChapters(Long bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = false;
        String currentUserEmail = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            currentUserEmail = auth.getName();
        }

        final boolean finalIsAdmin = isAdmin;
        final String finalEmail = currentUserEmail;

        List<Chapter> chapters = chapterRepository.findByBookIdOrderByPageNumberAsc(bookId);
        return chapters.stream()
                .filter(chapter -> finalIsAdmin || chapter.isPublic() ||
                        (book.getUploadedBy() != null && book.getUploadedBy().getEmail().equals(finalEmail)))
                .map(chapter -> {
                    ChapterDTO dto = new ChapterDTO();
                    dto.setId(chapter.getId());
                    dto.setTitle(chapter.getTitle());
                    dto.setPageNumber(chapter.getPageNumber());
                    dto.setPublic(chapter.isPublic());
                    // Intentionally omit content for summary list
                    return dto;
                }).collect(Collectors.toList());
    }

    public ChapterDTO getChapterContent(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = false;
        String currentUserEmail = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
            currentUserEmail = auth.getName();
        }

        Book book = chapter.getBook();
        if (!isAdmin && !chapter.isPublic() && 
            (book.getUploadedBy() == null || !book.getUploadedBy().getEmail().equals(currentUserEmail))) {
            throw new RuntimeException("You do not have permission to view this chapter");
        }

        // Auto-save reading progress for logged-in users
        autoSaveReadingProgress(chapter);

        ChapterDTO dto = new ChapterDTO();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setPageNumber(chapter.getPageNumber());
        dto.setContent(chapter.getContent());
        dto.setPublic(chapter.isPublic());
        return dto;
    }

    private BookDTO mapToBookDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setDescription(book.getDescription());
        dto.setPublishedDate(book.getPublishedDate());
        if (book.getAuthor() != null) {
            dto.setAuthorId(book.getAuthor().getId());
            dto.setAuthorName(book.getAuthor().getName());
        }
        if (book.getCategory() != null) {
            dto.setCategoryId(book.getCategory().getId());
            dto.setCategoryName(book.getCategory().getName());
        }
        return dto;
    }
}
