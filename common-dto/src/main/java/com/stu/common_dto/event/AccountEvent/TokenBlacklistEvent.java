package com.stu.common_dto.event.AccountEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklistEvent {
    private String jti;
    private long ttl;
}