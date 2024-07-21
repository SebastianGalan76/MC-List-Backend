package com.coresaken.mcserverlist.database.repository;

import com.coresaken.mcserverlist.database.model.server.SubServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubServerRepository extends JpaRepository<SubServer, Long> {
}
