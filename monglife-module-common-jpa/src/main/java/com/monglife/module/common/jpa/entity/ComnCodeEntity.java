package com.monglife.module.common.jpa.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "monglife_comn_code")
public class ComnCodeEntity {

    @Id
    @Column(name = "comn_code")
    private String code;

    @Column(name = "comn_name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_code")
    private GroupCodeEntity group;

    @Builder
    public ComnCodeEntity(String code, String name, GroupCodeEntity group) {
        this.code = code;
        this.name = name;
        this.group = group;
    }
}
