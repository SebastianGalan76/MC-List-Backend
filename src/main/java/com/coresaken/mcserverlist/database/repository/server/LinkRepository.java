package com.coresaken.mcserverlist.database.repository.server;

import com.coresaken.mcserverlist.database.model.server.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
}
