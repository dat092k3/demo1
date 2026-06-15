package com.example.demo.service;

import com.example.demo.dto.ChapterDTO;
import com.example.demo.dto.ChapterRequestDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.Chapter;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private BookRepository bookRepository;

    public ChapterDTO createChapter(Long bookId, ChapterRequestDTO requestDTO) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        checkPermission(book, "create chapter for");

        Chapter chapter = new Chapter();
        chapter.setBook(book);
        chapter.setTitle(requestDTO.getTitle());
        chapter.setContent(requestDTO.getContent());
        chapter.setPageNumber(requestDTO.getPageNumber());
        if (requestDTO.getIsPublic() != null) {
            chapter.setPublic(requestDTO.getIsPublic());
        }

        Chapter savedChapter = chapterRepository.save(chapter);
        return mapToDTO(savedChapter);
    }

    public ChapterDTO updateChapter(Long chapterId, ChapterRequestDTO requestDTO) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found with id: " + chapterId));

        checkPermission(chapter.getBook(), "modify chapter in");

        chapter.setTitle(requestDTO.getTitle());
        chapter.setContent(requestDTO.getContent());
        chapter.setPageNumber(requestDTO.getPageNumber());
        if (requestDTO.getIsPublic() != null) {
            chapter.setPublic(requestDTO.getIsPublic());
        }

        Chapter updatedChapter = chapterRepository.save(chapter);
        return mapToDTO(updatedChapter);
    }

    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found with id: " + chapterId));

        checkPermission(chapter.getBook(), "delete chapter from");

        chapterRepository.delete(chapter);
    }

    public ChapterDTO toggleChapterVisibility(Long chapterId, boolean isPublic) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found with id: " + chapterId));

        checkPermission(chapter.getBook(), "modify visibility of chapter in");

        chapter.setPublic(isPublic);
        Chapter updatedChapter = chapterRepository.save(chapter);
        return mapToDTO(updatedChapter);
    }

    private void checkPermission(Book book, String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        String currentUserEmail = auth.getName();

        if (!isAdmin && (book.getUploadedBy() == null || !book.getUploadedBy().getEmail().equals(currentUserEmail))) {
            throw new RuntimeException("You do not have permission to " + action + " this book");
        }
    }

    private ChapterDTO mapToDTO(Chapter chapter) {
        ChapterDTO dto = new ChapterDTO();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setPageNumber(chapter.getPageNumber());
        dto.setContent(chapter.getContent());
        dto.setPublic(chapter.isPublic());
        return dto;
    }
}
