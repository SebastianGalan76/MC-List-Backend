package com.coresaken.mcserverlist.database.repository.server;

import com.coresaken.mcserverlist.database.model.server.staff.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {
}
