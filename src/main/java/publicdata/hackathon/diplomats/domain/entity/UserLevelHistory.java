package publicdata.hackathon.diplomats.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import publicdata.hackathon.diplomats.domain.enums.UserLevel;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLevelHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserLevel previousLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserLevel newLevel;

    @Column(nullable = false)
    private Integer stampCount; // 레벨업 당시 스탬프 개수

    @Column(nullable = false)
    private LocalDateTime levelUpAt;

    @PrePersist
    protected void onCreate() {
        this.levelUpAt = LocalDateTime.now();
    }
}
