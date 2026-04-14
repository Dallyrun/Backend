package com.inseong.dallyrun.backend.entity;

import com.inseong.dallyrun.backend.entity.enums.ConditionType;
import jakarta.persistence.*;

@Entity
@Table(name = "badge")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
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
