package com.coresaken.mcserverlist.database.repository;

import com.coresaken.mcserverlist.database.model.server.Server;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {
    @Query("SELECT s FROM Server s WHERE LOWER(s.ip) = LOWER(:ip)")
    Optional<Server> findByIp(@Param("ip") String ip);

    @Query("SELECT s FROM Server s WHERE TYPE(s) = Server ORDER BY s.id DESC")
    Page<Server> findAllGlobalServers(Pageable pageable);

    @Query("SELECT s FROM Server s WHERE s.nextRefreshAt IS NOT NULL")
    List<Server> findAllServersWithNextRefreshAtNotNull();
}
