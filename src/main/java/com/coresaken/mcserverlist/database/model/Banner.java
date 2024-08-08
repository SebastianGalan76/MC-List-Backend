package com.coresaken.mcserverlist.database.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.LocalDateTime;

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
    User owner;

    @Enumerated(EnumType.STRING)
    BannerSize bannerSize;

    @Enumerated(EnumType.STRING)
    Status status;

    String rejectedReason;

    LocalDateTime publishedAt;
    LocalDateTime finishedAt;

    boolean paid = false;

    public enum BannerSize{
        BIG, NORMAL, SMALL
    }

    public enum Status{
        PUBLISHED, ACCEPTED, REJECTED, NOT_VERIFIED, FINISHED
    }

    public void changeStatus(Status status, String rejectedReason){
        this.status = status;
        this.rejectedReason = rejectedReason;
    }
}

