package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            AuthorRepository authorRepository,
            BookRepository bookRepository,
            ChapterRepository chapterRepository) {

        return args -> {
            // Check if data already exists to avoid duplication if ddl-auto is not create/drop
            if (roleRepository.count() > 0) {
                return;
            }

            // 1. Create Roles
            Role roleUser = new Role("ROLE_USER");
            Role roleAdmin = new Role("ROLE_ADMIN");
            roleRepository.saveAll(List.of(roleUser, roleAdmin));

            // 2. Create Users
            User admin = new User("Admin User", "admin@example.com", "Admin@123", "+1234567890");
            admin.setRoles(List.of(roleAdmin, roleUser));
            admin.setMembershipStatus("PREMIUM");
            
            User user1 = new User("John Doe", "john@example.com", "User@123", "+0987654321");
            user1.setRoles(List.of(roleUser));

            userRepository.saveAll(List.of(admin, user1));

            // 3. Create Categories
            Category fiction = new Category("Fiction", "Fictional stories and novels");
            Category science = new Category("Science", "Science and educational books");
            Category tech = new Category("Technology", "Books about technology and programming");
            categoryRepository.saveAll(List.of(fiction, science, tech));

            // 4. Create Authors
            Author author1 = new Author("J.K. Rowling", "British author, best known for the Harry Potter series.", "jk@example.com");
            Author author2 = new Author("Stephen Hawking", "English theoretical physicist and cosmologist.", "stephen@example.com");
            Author author3 = new Author("Robert C. Martin", "American software engineer and instructor.", "unclebob@example.com");
            authorRepository.saveAll(List.of(author1, author2, author3));

            // 5. Create Books
            Book book1 = new Book("Harry Potter and the Sorcerer's Stone", "A young boy discovers he is a wizard.", 
                    LocalDate.of(1997, 6, 26), 100, author1, fiction);
            
            Book book2 = new Book("A Brief History of Time", "A popular-science book on cosmology.", 
                    LocalDate.of(1988, 3, 1), 50, author2, science);

            Book book3 = new Book("Clean Code", "A Handbook of Agile Software Craftsmanship.", 
                    LocalDate.of(2008, 8, 1), 200, author3, tech);

            bookRepository.saveAll(List.of(book1, book2, book3));

            // 6. Create Chapters
            Chapter ch1_book1 = new Chapter("The Boy Who Lived", 1, "Mr. and Mrs. Dursley, of number four, Privet Drive...", book1);
            Chapter ch2_book1 = new Chapter("The Vanishing Glass", 2, "Nearly ten years had passed since the Dursleys...", book1);

            Chapter ch1_book2 = new Chapter("Our Picture of the Universe", 1, "A well-known scientist (some say it was Bertrand Russell)...", book2);
            Chapter ch2_book2 = new Chapter("Space and Time", 2, "Our present ideas about the motion of bodies...", book2);

            Chapter ch1_book3 = new Chapter("Clean Code", 1, "There will be code. One might argue that a book...", book3);

            chapterRepository.saveAll(List.of(ch1_book1, ch2_book1, ch1_book2, ch2_book2, ch1_book3));
            
            System.out.println("Demo data has been successfully initialized!");
        };
    }
}
