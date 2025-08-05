package com.stu.common_dto.event.CartEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartCheckedOutEvent {
    private Long userId;
    private List<CartItemDTO> items;
}
