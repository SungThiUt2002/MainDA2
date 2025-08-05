package com.stu.common_dto.event.AccountEvent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusEvent {
    private Long userId;
    private String action; // "DELETED" hoặc "DISABLED"
}

