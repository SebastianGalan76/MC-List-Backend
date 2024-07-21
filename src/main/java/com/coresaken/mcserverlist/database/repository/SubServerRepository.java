package com.coresaken.mcserverlist.database.repository;

import com.coresaken.mcserverlist.database.model.server.SubServer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubServerRepository extends JpaRepository<SubServer, Long> {
    @Query("SELECT ss FROM SubServer ss WHERE ss.parent.id = :parentId ORDER BY ss.index ASC")
    List<SubServer> findSubServersForServerId(@Param("parentId") Long parentId);
}
