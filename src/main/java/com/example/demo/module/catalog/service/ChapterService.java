package com.example.demo.module.catalog.service;

import com.example.demo.module.catalog.dto.ChapterDTO;
import com.example.demo.module.catalog.dto.ChapterRequestDTO;
import com.example.demo.module.catalog.entity.Book;
import com.example.demo.module.catalog.entity.Chapter;
import com.example.demo.module.catalog.repository.BookRepository;
import com.example.demo.module.catalog.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.module.core.messaging.MessagePublisher;
import com.example.demo.module.core.dto.message.SearchIndexMessage;

@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MessagePublisher messagePublisher;

    @Autowired
    private FileStorageService fileStorageService;

    @CacheEvict(value = "chapters", allEntries = true)
    public ChapterDTO createChapter(Long bookId, ChapterRequestDTO requestDTO, MultipartFile file) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + bookId));

        checkPermission(book, "create chapter for");

        String filePath = fileStorageService.saveFile(bookId, requestDTO.getContent(), file);

        Chapter chapter = new Chapter();
        chapter.setBook(book);
        chapter.setTitle(requestDTO.getTitle());
        chapter.setFilePath(filePath);
        chapter.setPageNumber(requestDTO.getPageNumber());
        if (requestDTO.getIsPublic() != null) {
            chapter.setPublic(requestDTO.getIsPublic());
        }

        Chapter savedChapter = chapterRepository.save(chapter);
        messagePublisher.publishSearchIndex(new SearchIndexMessage("CHAPTER", savedChapter.getId(), "CREATE"));
        return mapToDTO(savedChapter);
    }

    @CacheEvict(value = "chapters", allEntries = true)
    public ChapterDTO updateChapter(Long chapterId, ChapterRequestDTO requestDTO, MultipartFile file) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found with id: " + chapterId));

        checkPermission(chapter.getBook(), "modify chapter in");

        if (file != null || (requestDTO.getContent() != null && !requestDTO.getContent().isEmpty())) {
            // Xóa file cũ nếu có
            if (chapter.getFilePath() != null) {
                fileStorageService.deleteFile(chapter.getFilePath());
            }
            String newFilePath = fileStorageService.saveFile(chapter.getBook().getId(), requestDTO.getContent(), file);
            chapter.setFilePath(newFilePath);
        }

        chapter.setTitle(requestDTO.getTitle());
        chapter.setPageNumber(requestDTO.getPageNumber());
        if (requestDTO.getIsPublic() != null) {
            chapter.setPublic(requestDTO.getIsPublic());
        }

        Chapter updatedChapter = chapterRepository.save(chapter);
        messagePublisher.publishSearchIndex(new SearchIndexMessage("CHAPTER", updatedChapter.getId(), "UPDATE"));
        return mapToDTO(updatedChapter);
    }

    @CacheEvict(value = "chapters", allEntries = true)
    public void deleteChapter(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found with id: " + chapterId));

        checkPermission(chapter.getBook(), "delete chapter from");

        if (chapter.getFilePath() != null) {
            fileStorageService.deleteFile(chapter.getFilePath());
        }

        chapterRepository.delete(chapter);
        messagePublisher.publishSearchIndex(new SearchIndexMessage("CHAPTER", chapterId, "DELETE"));
    }

    @CacheEvict(value = "chapters", allEntries = true)
    public ChapterDTO toggleChapterVisibility(Long chapterId, boolean isPublic) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found with id: " + chapterId));

        checkPermission(chapter.getBook(), "modify visibility of chapter in");

        chapter.setPublic(isPublic);
        Chapter updatedChapter = chapterRepository.save(chapter);
        messagePublisher.publishSearchIndex(new SearchIndexMessage("CHAPTER", updatedChapter.getId(), "UPDATE"));
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
        
        String content = fileStorageService.readFile(chapter.getFilePath());
        dto.setContent(content);
        
        dto.setPublic(chapter.isPublic());
        return dto;
    }
}
