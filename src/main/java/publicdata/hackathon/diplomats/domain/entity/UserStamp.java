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
import publicdata.hackathon.diplomats.domain.enums.StampType;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StampType stampType;

    @Column(nullable = false)
    private Integer stampCount;

    @Column
    private String relatedEntityType; // 관련 엔티티 타입 (예: "DIARY", "VOTE")

    @Column
    private Long relatedEntityId; // 관련 엔티티 ID

    @Column
    private String description; // 스탬프 획득 설명

    @Column(nullable = false)
    private LocalDateTime earnedAt;

    @PrePersist
    protected void onCreate() {
        this.earnedAt = LocalDateTime.now();
        if (this.stampCount == null) {
            this.stampCount = this.stampType.getStampCount();
        }
        if (this.description == null) {
            this.description = this.stampType.getDescription();
        }
    }
}
