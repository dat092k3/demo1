package com.example.demo.config;

import com.example.demo.entity.*;
import com.example.demo.enums.ERole;
import com.example.demo.repository.*;
import com.example.demo.service.FileStorageService;
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
            ChapterRepository chapterRepository,
            FileStorageService fileStorageService) {

        return args -> {
            if (roleRepository.count() > 0) {
                return;
            }

            // 1. Roles
            Role roleUser = new Role(ERole.USER);
            Role roleAdmin = new Role(ERole.ADMIN);
            roleRepository.saveAll(List.of(roleUser, roleAdmin));

            // 2. Users
            User admin = new User("Admin User", "admin@example.com", "Admin@123", "+1234567890");
            admin.setRoles(List.of(roleAdmin, roleUser));
            admin.setMembershipStatus("PREMIUM");

            User user1 = new User("John Doe", "john@example.com", "User@123", "+0987654321");
            user1.setRoles(List.of(roleUser));

            userRepository.saveAll(List.of(admin, user1));

            // 3. Categories
            Category fiction = new Category("Fiction", "Fictional stories and novels");
            Category science = new Category("Science", "Science and educational books");
            Category tech = new Category("Technology", "Books about technology and programming");
            categoryRepository.saveAll(List.of(fiction, science, tech));

            // 4. Authors
            Author author1 = new Author("J.K. Rowling", "British author, best known for the Harry Potter series.", "jk@example.com");
            Author author2 = new Author("Stephen Hawking", "English theoretical physicist and cosmologist.", "stephen@example.com");
            Author author3 = new Author("Robert C. Martin", "American software engineer and instructor.", "unclebob@example.com");
            Author author4 = new Author("George Orwell", "English novelist, essayist, and critic.", "orwell@example.com");
            Author author5 = new Author("Carl Sagan", "American astronomer and science communicator.", "sagan@example.com");
            Author author6 = new Author("Martin Fowler", "British software developer and author on software design.", "fowler@example.com");
            authorRepository.saveAll(List.of(author1, author2, author3, author4, author5, author6));

            // 5. Books
            Book book1 = new Book("Harry Potter and the Sorcerer's Stone", "A young boy discovers he is a wizard.",
                    LocalDate.of(1997, 6, 26), author1, fiction);
            book1.setPublic(true);

            Book book4 = new Book("1984", "A dystopian social science fiction novel about totalitarian control.",
                    LocalDate.of(1949, 6, 8), author4, fiction);
            book4.setPublic(true);

            Book book5 = new Book("Animal Farm", "A satirical allegorical novella about a group of farm animals.",
                    LocalDate.of(1945, 8, 17), author4, fiction);
            book5.setPublic(true);

            Book book2 = new Book("A Brief History of Time", "A popular-science book on cosmology.",
                    LocalDate.of(1988, 3, 1), author2, science);
            book2.setPublic(true);

            Book book6 = new Book("Cosmos", "An exploration of the universe and our place within it.",
                    LocalDate.of(1980, 1, 1), author5, science);
            book6.setPublic(true);

            Book book7 = new Book("The Grand Design", "A book on the nature of the universe and physical laws.",
                    LocalDate.of(2010, 9, 7), author2, science);
            book7.setPublic(true);

            Book book3 = new Book("Clean Code", "A Handbook of Agile Software Craftsmanship.",
                    LocalDate.of(2008, 8, 1), author3, tech);
            book3.setPublic(true);

            Book book8 = new Book("Refactoring", "Improving the Design of Existing Code.",
                    LocalDate.of(1999, 7, 8), author6, tech);
            book8.setPublic(true);

            Book book9 = new Book("The Clean Coder", "A Code of Conduct for Professional Programmers.",
                    LocalDate.of(2011, 5, 13), author3, tech);
            book9.setPublic(true);

            bookRepository.saveAll(List.of(book1, book2, book3, book4, book5, book6, book7, book8, book9));

            // 6. Chapters — nội dung được ghi ra file thật qua FileStorageService,
            // Chapter chỉ lưu lại filePath trả về (KHÔNG lưu text trực tiếp).
            Chapter ch1_book1 = new Chapter("The Boy Who Lived", 1,
                    fileStorageService.saveFile(book1.getId(),
                            "Mr. and Mrs. Dursley, of number four, Privet Drive, were proud to say that they were perfectly normal, thank you very much. They were the last people you'd expect to be involved in anything strange or mysterious, because they just didn't hold with such nonsense.",
                            null),
                    book1);
            ch1_book1.setPublic(true);

            Chapter ch2_book1 = new Chapter("The Vanishing Glass", 2,
                    fileStorageService.saveFile(book1.getId(),
                            "Nearly ten years had passed since the Dursleys had woken up to find their nephew on the front step, but Privet Drive had hardly changed at all. The sun rose on the same tidy front gardens and lit up the brass number four on the Dursleys' front door.",
                            null),
                    book1);
            ch2_book1.setPublic(true);

            Chapter ch1_book2 = new Chapter("Our Picture of the Universe", 1,
                    fileStorageService.saveFile(book2.getId(),
                            "A well-known scientist (some say it was Bertrand Russell) once gave a public lecture on astronomy. He described how the earth orbits around the sun and how the sun, in turn, orbits around the center of a vast collection of stars called our galaxy.",
                            null),
                    book2);
            ch1_book2.setPublic(true);

            Chapter ch2_book2 = new Chapter("Space and Time", 2,
                    fileStorageService.saveFile(book2.getId(),
                            "Our present ideas about the motion of bodies date from Galileo and Newton. Before them people believed Aristotle, who said that the natural state of a body was to be at rest, and that it moved only if driven by a force or impulse.",
                            null),
                    book2);
            ch2_book2.setPublic(true);

            Chapter ch1_book3 = new Chapter("Clean Code", 1,
                    fileStorageService.saveFile(book3.getId(),
                            "There will be code. One might argue that a book about clean code is not going to help much. Bad code can kill a company. Consider all the horror stories you've heard about bad software.",
                            null),
                    book3);
            ch1_book3.setPublic(true);

            Chapter ch1_book4 = new Chapter("Chapter 1", 1,
                    fileStorageService.saveFile(book4.getId(),
                            "It was a bright cold day in April, and the clocks were striking thirteen. Winston Smith, his chin nuzzled into his breast in an effort to escape the vile wind, slipped quickly through the glass doors of Victory Mansions, though not quickly enough to prevent a swirl of gritty dust from entering along with him.",
                            null),
                    book4);
            ch1_book4.setPublic(true);

            Chapter ch1_book5 = new Chapter("Chapter 1", 1,
                    fileStorageService.saveFile(book5.getId(),
                            "Mr. Jones, of the Manor Farm, had locked the hen-houses for the night, but was too drunk to remember to shut the pop-holes. With the ring of light from his lantern dancing from side to side, he lurched across the yard, kicked off his boots at the back door, drew himself a last glass of beer from the barrel in the scullery, and made his way up to bed.",
                            null),
                    book5);
            ch1_book5.setPublic(true);

            Chapter ch1_book6 = new Chapter("The Shores of the Cosmic Ocean", 1,
                    fileStorageService.saveFile(book6.getId(),
                            "The Cosmos is all that is or ever was or ever will be. Our feeblest contemplations of the Cosmos stir us. There is a tingling in the spine, a catch in the voice, a faint sensation, as if a distant memory, of falling from a height.",
                            null),
                    book6);
            ch1_book6.setPublic(true);

            Chapter ch1_book7 = new Chapter("The Mystery of Being", 1,
                    fileStorageService.saveFile(book7.getId(),
                            "We each exist for but a short time, and in that time explore but a small part of the whole universe. But humans are a curious species. We wonder, we seek answers.",
                            null),
                    book7);
            ch1_book7.setPublic(true);

            Chapter ch1_book8 = new Chapter("Refactoring, a First Example", 1,
                    fileStorageService.saveFile(book8.getId(),
                            "I am going to begin this book with a war story. It's a story about a program I refactored, and about the way in which I did it. My colleagues had been raving about refactoring, so I had a good, if vague, idea of what it was.",
                            null),
                    book8);
            ch1_book8.setPublic(true);

            Chapter ch1_book9 = new Chapter("Professionalism", 1,
                    fileStorageService.saveFile(book9.getId(),
                            "What does it mean to be a professional? For programmers, being a professional means holding yourself accountable for your career, your estimates, your failures, and your successes.",
                            null),
                    book9);
            ch1_book9.setPublic(true);

            chapterRepository.saveAll(List.of(
                    ch1_book1, ch2_book1,
                    ch1_book2, ch2_book2,
                    ch1_book3,
                    ch1_book4,
                    ch1_book5,
                    ch1_book6,
                    ch1_book7,
                    ch1_book8,
                    ch1_book9
            ));

            System.out.println("Demo data has been successfully initialized!");
        };
    }
}