package com.coresaken.mcserverlist.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String link;
    String filePath;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    User owner;

    @Enumerated(EnumType.STRING)
    Size size;

    @Enumerated(EnumType.STRING)
    Status status;

    String rejectedReason;

    LocalDateTime publishedAt;
    LocalDateTime finishedAt;

    boolean paid = false;

    public enum Size {
        BIG, NORMAL, SMALL
    }

    public enum Status{
        PUBLISHED, ACCEPTED, REJECTED, NOT_VERIFIED, FINISHED
    }

    public void changeStatus(Status status, String rejectedReason){
        this.status = status;
        this.rejectedReason = rejectedReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Banner banner = (Banner) o;
        return Objects.equals(id, banner.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

