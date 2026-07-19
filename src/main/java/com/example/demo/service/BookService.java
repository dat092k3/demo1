package com.example.demo.service;

import com.example.demo.dto.BookDTO;
import com.example.demo.dto.BookRequestDTO;
import com.example.demo.entity.Author;
import com.example.demo.entity.Book;
import com.example.demo.enums.ERole;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import com.example.demo.messaging.MessagePublisher;
import com.example.demo.dto.message.SearchIndexMessage;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessagePublisher messagePublisher;

    @Cacheable(value = "books", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication() != null ? T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() : 'anonymous'")
    public List<BookDTO> getAllBooks() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = false;
        String currentUserEmail = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + ERole.ADMIN.name()));
            currentUserEmail = auth.getName();
        }

        final boolean finalIsAdmin = isAdmin;
        final String finalEmail = currentUserEmail;

        return bookRepository.findAll().stream()
                .filter(book -> finalIsAdmin || book.isPublic() ||
                        (book.getUploadedBy() != null && book.getUploadedBy().getEmail().equals(finalEmail)))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "book", key = "#id")
    public BookDTO getBookById(@NonNull Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        return mapToDTO(book);
    }

    @CacheEvict(value = "books", allEntries = true)
    public BookDTO createBook(BookRequestDTO requestDTO) {
        Author author = authorRepository.findById(requestDTO.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + requestDTO.getAuthorId()));
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + requestDTO.getCategoryId()));

        Book book = new Book();
        book.setTitle(requestDTO.getTitle());
        book.setDescription(requestDTO.getDescription());
        book.setPublishedDate(requestDTO.getPublishedDate());
        book.setAuthor(author);
        book.setCategory(category);

        if (requestDTO.getIsPublic() != null) {
            book.setPublic(requestDTO.getIsPublic());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            User currentUser = userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            book.setUploadedBy(currentUser);
        }

        Book savedBook = bookRepository.save(book);
        messagePublisher.publishSearchIndex(new SearchIndexMessage("BOOK", savedBook.getId(), "CREATE"));
        return mapToDTO(savedBook);
    }

    @Caching(evict = {
            @CacheEvict(value = "books", allEntries = true),
            @CacheEvict(value = "book", key = "#id")
    })
    public BookDTO updateBook(@NonNull Long id, BookRequestDTO requestDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + ERole.ADMIN.name()));
        String currentUserEmail = auth.getName();

        if (!isAdmin && (book.getUploadedBy() == null || !book.getUploadedBy().getEmail().equals(currentUserEmail))) {
            throw new RuntimeException("You do not have permission to modify this book");
        }

        Author author = authorRepository.findById(requestDTO.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + requestDTO.getAuthorId()));
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + requestDTO.getCategoryId()));

        book.setTitle(requestDTO.getTitle());
        book.setDescription(requestDTO.getDescription());
        book.setPublishedDate(requestDTO.getPublishedDate());
        book.setAuthor(author);
        book.setCategory(category);

        if (requestDTO.getIsPublic() != null) {
            book.setPublic(requestDTO.getIsPublic());
        }

        Book updatedBook = bookRepository.save(book);
        messagePublisher.publishSearchIndex(new SearchIndexMessage("BOOK", updatedBook.getId(), "UPDATE"));
        return mapToDTO(updatedBook);
    }

    @Caching(evict = {
            @CacheEvict(value = "books", allEntries = true),
            @CacheEvict(value = "book", key = "#id")
    })
    @Transactional
    public BookDTO toggleBookVisibility(@NonNull Long id, boolean isPublic) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        String currentUserEmail = auth.getName();

        if (!isAdmin && (book.getUploadedBy() == null || !book.getUploadedBy().getEmail().equals(currentUserEmail))) {
            throw new RuntimeException("You do not have permission to modify this book's visibility");
        }

        book.setPublic(isPublic);

        if (!isPublic && book.getChapters() != null) {
            book.getChapters().forEach(chapter -> chapter.setPublic(false));
        }

        Book updatedBook = bookRepository.save(book);
        messagePublisher.publishSearchIndex(new SearchIndexMessage("BOOK", updatedBook.getId(), "UPDATE"));
        return mapToDTO(updatedBook);
    }

    @Caching(evict = {
            @CacheEvict(value = "books", allEntries = true),
            @CacheEvict(value = "book", key = "#id")
    })
    public void deleteBook(@NonNull Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        String currentUserEmail = auth.getName();

        if (!isAdmin && (book.getUploadedBy() == null || !book.getUploadedBy().getEmail().equals(currentUserEmail))) {
            throw new RuntimeException("You do not have permission to delete this book");
        }

        bookRepository.delete(Objects.requireNonNull(book));
        messagePublisher.publishSearchIndex(new SearchIndexMessage("BOOK", id, "DELETE"));
    }

    private BookDTO mapToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setDescription(book.getDescription());
        dto.setPublishedDate(book.getPublishedDate());
        dto.setViewCount(book.getViewCount());
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
