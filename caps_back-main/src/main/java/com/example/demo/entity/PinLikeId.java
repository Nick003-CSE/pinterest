package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class PinLikeId implements Serializable {

    @Column(name = "pin_id")
    private Long pinId;

    @Column(name = "user_id")
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PinLikeId pinLikeId = (PinLikeId) o;
        return Objects.equals(pinId, pinLikeId.pinId) && Objects.equals(userId, pinLikeId.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pinId, userId);
    }
}

