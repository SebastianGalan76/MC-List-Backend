package com.coresaken.mcserverlist.database.model.server;

import com.coresaken.mcserverlist.database.model.server.staff.Rank;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "server")
@Inheritance(strategy = InheritanceType.JOINED)
public class Server {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String ip;
    int port;

    @ManyToOne
    @JoinColumn(name = "detail_id")
    ServerDetail detail;

    @ManyToOne
    @JoinColumn(name = "name_id")
    Name name;

    @Column(columnDefinition = "TEXT")
    String description;

    String bannerUrl;

    @ManyToOne
    @JoinColumn(name = "mode_id")
    Mode mode;

    boolean online;

    int onlinePlayers;

    LocalDateTime nextRefreshAt;

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("index ASC")
    List<Rank> staff;

    @ManyToMany
    @JoinTable(
            name = "server_version",
            joinColumns = @JoinColumn(name = "server_id"),
            inverseJoinColumns = @JoinColumn(name = "version_id")
    )
    List<Version> versions = new ArrayList<>();

    @OneToMany(
            mappedBy = "server",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    List<Vote> votes = new ArrayList<>();

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("index ASC")
    List<Link> links = new ArrayList<>();

    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true)
    List<HourlyPlayerCount> hourlyPlayerCounts;
}
