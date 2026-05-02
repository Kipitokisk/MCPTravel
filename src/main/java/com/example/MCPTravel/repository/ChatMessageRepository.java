package com.example.MCPTravel.repository;

import com.example.MCPTravel.entity.ChatMessage;
import com.example.MCPTravel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserAndSessionIdOrderByCreatedAtAsc(User user, String sessionId);

    @Query("SELECT DISTINCT cm.sessionId FROM ChatMessage cm WHERE cm.user = :user ORDER BY cm.sessionId")
    List<String> findDistinctSessionIdsByUser(@Param("user") User user);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user = :user AND cm.createdAt = " +
           "(SELECT MAX(cm2.createdAt) FROM ChatMessage cm2 WHERE cm2.sessionId = cm.sessionId AND cm2.user = :user) " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLatestMessagePerSession(@Param("user") User user);

    void deleteByUserAndSessionId(User user, String sessionId);
}
