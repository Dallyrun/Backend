package com.inseong.dallyrun.backend.entity;

import com.inseong.dallyrun.backend.entity.enums.ConditionType;
import jakarta.persistence.*;

/**
 * 배지 정의 엔티티.
 * conditionType(달성 조건 유형)과 conditionValue(달성 기준값)로 수여 조건을 정의한다.
 * 배지 데이터는 사전 정의되며, 회원의 배지 획득 이력은 MemberBadge에서 관리한다.
 */
@Entity
@Table(name = "badge")
public class Badge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(length = 500)
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ConditionType conditionType;

    @Column(nullable = false)
    private Double conditionValue;

    protected Badge() {
    }

    public Badge(String name, String description, String iconUrl,
                 ConditionType conditionType, Double conditionValue) {
        this.name = name;
        this.description = description;
        this.iconUrl = iconUrl;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public Double getConditionValue() {
        return conditionValue;
    }
}
