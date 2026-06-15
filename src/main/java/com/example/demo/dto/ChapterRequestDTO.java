package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ChapterRequestDTO {

    @NotBlank(message = "Chapter title cannot be blank")
    @Size(min = 1, max = 200, message = "Chapter title must be between 1 and 200 characters")
    @Pattern(regexp = "^[\\p{L}\\s'-:.,&()]+$",
            message = "Chapter title can only contain letters, numbers, spaces, and common punctuation marks")
    private String title;

    @Positive(message = "Page number must be greater than 0")
    private Integer pageNumber;

    private String content;

    private Boolean isPublic;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}
