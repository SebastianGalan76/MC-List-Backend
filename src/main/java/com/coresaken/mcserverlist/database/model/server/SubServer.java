package com.coresaken.mcserverlist.database.model.server;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubServer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "name_id")
    Name name;

    @ManyToOne
    @JoinColumn(name = "mode_id")
    Mode mode;

    @ManyToMany
    @JoinTable(
            name = "subserver_version",
            joinColumns = @JoinColumn(name = "subserver_id"),
            inverseJoinColumns = @JoinColumn(name = "version_id")
    )
    List<Version> versions;
}
