package com.monglife.module.common.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "group_code")
public class GroupCodeEntity {

    @Id
    @Column(name = "group_code")
    private String code;

    @Column(name = "group_name")
    private String name;

    @Builder
    public GroupCodeEntity(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
