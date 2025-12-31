package com.skrudra.sso.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.Instant;


/**
 * Domain model representing a Book.
 */
public class Book {

    private Long id;

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotBlank(message = "author must not be blank")
    private String author;

    @PositiveOrZero(message = "price must be >= 0")
    private double price;

    // Audit fields
    private String createdBy;
    private Instant createdDatetime;
    private String updatedBy;
    private Instant updatedDatetime;

    public Book() {
    }

    public Book(Long id, String name, String author, double price) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.price = price;
    }

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(Instant createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Instant getUpdatedDatetime() {
        return updatedDatetime;
    }

    public void setUpdatedDatetime(Instant updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", createdBy='" + createdBy + '\'' +
                ", createdDatetime=" + createdDatetime +
                ", updatedBy='" + updatedBy + '\'' +
                ", updatedDatetime=" + updatedDatetime +
                '}';
    }
}
