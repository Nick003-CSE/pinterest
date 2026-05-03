package com.example.demo.repository;

import com.example.demo.entity.Invitation;
import com.example.demo.entity.Invitation.InvitationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    @Query("SELECT i FROM Invitation i WHERE i.invitee.id = :userId AND i.status = :status")
    List<Invitation> findByInviteeIdAndStatus(@Param("userId") Long userId, @Param("status") InvitationStatus status);

    @Query("SELECT i FROM Invitation i WHERE i.invitee.id = :userId")
    List<Invitation> findByInviteeId(@Param("userId") Long userId);
}

