package com.example.demo.module.iam.entity;

import com.example.demo.module.reading.entity.UserFavoriteBook;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotBlank(message = "User name cannot be blank")
    @Size(min = 2, max = 100, message = "User name must be between 2 and 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "User name can only contain letters, spaces, hyphens, and apostrophes")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit")
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Pattern(regexp = "^(\\+\\d{1,3})?\\d{7,15}$|^$", 
            message = "Phone number must be valid (7-15 digits, optionally starting with + and country code)")
    @Column(name = "phone", length = 15)
    private String phone;

    @Column(name = "registered_date")
    private LocalDateTime registeredDate;

    @Pattern(regexp = "^(ACTIVE|PREMIUM|INACTIVE)$", 
            message = "Membership status must be ACTIVE, PREMIUM, or INACTIVE")
    @Column(name = "membership_status", length = 50)
    private String membershipStatus;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserFavoriteBook> favoriteBooks;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    public User() {
        this.registeredDate = LocalDateTime.now();
        this.membershipStatus = "ACTIVE";
    }

    public User(String name, String email, String password, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.registeredDate = LocalDateTime.now();
        this.membershipStatus = "ACTIVE";
    }

    // ...existing code...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(LocalDateTime registeredDate) {
        this.registeredDate = registeredDate;
    }

    public String getMembershipStatus() {
        return membershipStatus;
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus = membershipStatus;
    }

    public List<UserFavoriteBook> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(List<UserFavoriteBook> favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", membershipStatus='" + membershipStatus + '\'' +
                '}';
    }
}
