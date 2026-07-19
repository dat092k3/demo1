package com.example.demo.messaging;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.message.ReadingProgressMessage;
import com.example.demo.entity.Book;
import com.example.demo.entity.Chapter;
import com.example.demo.entity.User;
import com.example.demo.entity.UserReadingProgress;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserReadingProgressRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReadingProgressConsumer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private UserReadingProgressRepository userReadingProgressRepository;

    @RabbitListener(queues = RabbitMQConfig.READING_PROGRESS_QUEUE)
    public void consumeReadingProgressMessage(ReadingProgressMessage message) {
        if (message.getUserId() == null || message.getChapterId() == null || message.getBookId() == null) {
            return;
        }

        User user = userRepository.findById(message.getUserId()).orElse(null);
        Book book = bookRepository.findById(message.getBookId()).orElse(null);
        Chapter chapter = chapterRepository.findById(message.getChapterId()).orElse(null);

        if (user == null || book == null || chapter == null) {
            return;
        }

        Optional<UserReadingProgress> existing = userReadingProgressRepository.findByUserIdAndBookId(user.getId(), book.getId());
        UserReadingProgress progress;
        if (existing.isPresent()) {
            progress = existing.get();
            progress.setLastReadChapter(chapter);
            progress.setLastReadDate(message.getTimestamp());
        } else {
            progress = new UserReadingProgress(user, book, chapter);
            progress.setLastReadDate(message.getTimestamp());
        }
        userReadingProgressRepository.save(progress);
    }
}
