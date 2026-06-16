package com.example.demo.service;

import com.example.demo.dto.AuthorDTO;
import com.example.demo.dto.AuthorRequestDTO;
import com.example.demo.entity.Author;
import com.example.demo.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    public List<AuthorDTO> getAllAuthors() {
        return authorRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public AuthorDTO getAuthorById(@NonNull Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
        return mapToDTO(author);
    }

    public AuthorDTO createAuthor(AuthorRequestDTO requestDTO) {
        Author author = new Author();
        author.setName(requestDTO.getName());
        author.setBio(requestDTO.getBio());
        author.setEmail(requestDTO.getEmail());

        Author savedAuthor = authorRepository.save(author);
        return mapToDTO(savedAuthor);
    }

    public AuthorDTO updateAuthor(@NonNull Long id, AuthorRequestDTO requestDTO) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));

        author.setName(requestDTO.getName());
        author.setBio(requestDTO.getBio());
        author.setEmail(requestDTO.getEmail());

        Author updatedAuthor = authorRepository.save(author);
        return mapToDTO(updatedAuthor);
    }

    public void deleteAuthor(@NonNull Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
        authorRepository.delete(Objects.requireNonNull(author));
    }

    private AuthorDTO mapToDTO(Author author) {
        AuthorDTO dto = new AuthorDTO();
        dto.setId(author.getId());
        dto.setName(author.getName());
        dto.setBio(author.getBio());
        dto.setEmail(author.getEmail());
        return dto;
    }
}
