package com.example.demo.service;

import com.example.demo.dto.BookDTO;
import com.example.demo.dto.BookRequestDTO;
import com.example.demo.entity.Author;
import com.example.demo.entity.Book;
import com.example.demo.entity.Category;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        return mapToDTO(book);
    }

    public BookDTO createBook(BookRequestDTO requestDTO) {
        Author author = authorRepository.findById(requestDTO.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + requestDTO.getAuthorId()));
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + requestDTO.getCategoryId()));

        Book book = new Book();
        book.setTitle(requestDTO.getTitle());
        book.setDescription(requestDTO.getDescription());
        book.setPublishedDate(requestDTO.getPublishedDate());
        book.setQuantity(requestDTO.getQuantity());
        book.setAuthor(author);
        book.setCategory(category);

        Book savedBook = bookRepository.save(book);
        return mapToDTO(savedBook);
    }

    public BookDTO updateBook(Long id, BookRequestDTO requestDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        Author author = authorRepository.findById(requestDTO.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + requestDTO.getAuthorId()));
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + requestDTO.getCategoryId()));

        book.setTitle(requestDTO.getTitle());
        book.setDescription(requestDTO.getDescription());
        book.setPublishedDate(requestDTO.getPublishedDate());
        book.setQuantity(requestDTO.getQuantity());
        book.setAuthor(author);
        book.setCategory(category);

        Book updatedBook = bookRepository.save(book);
        return mapToDTO(updatedBook);
    }

    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        bookRepository.delete(book);
    }

    private BookDTO mapToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setDescription(book.getDescription());
        dto.setPublishedDate(book.getPublishedDate());
        dto.setQuantity(book.getQuantity());
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
