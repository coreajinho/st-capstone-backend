package org.example.stcapstonebackend.debate;

import org.example.stcapstonebackend.debate.model.DebatePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DebatePostRepository extends JpaRepository<DebatePost,Long> {
}
