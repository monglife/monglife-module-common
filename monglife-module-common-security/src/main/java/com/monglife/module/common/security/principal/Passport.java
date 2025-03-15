package com.monglife.module.common.security.principal;

import com.monglife.core.utils.CommonUtil;
import com.monglife.core.vo.passport.PassportVo;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
@ToString
public class Passport extends User {

    private final Long accountId;

    private final String deviceId;

    private final String email;

    private final String name;

    private final LocalDateTime createdAt;

    public Passport(PassportVo passportVo) {
        super(
                passportVo.getData().getAccount().getEmail(),
                CommonUtil.randomId(),
                Arrays.stream(passportVo.getData().getAccount().getRole().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );

        this.accountId = passportVo.getData().getAccount().getAccountId();
        this.deviceId = passportVo.getData().getAccount().getDeviceId();
        this.email = passportVo.getData().getAccount().getEmail();
        this.name = passportVo.getData().getAccount().getName();
        this.createdAt = passportVo.getCreatedAt();
    }
}
