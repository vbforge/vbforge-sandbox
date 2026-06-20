package com.vbforge.org.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventMsg {

    private String id;
    private String type;                         //"ORDER_PLACED", "USER_REGISTERED", etc.
    private String payload;
    private int sequenceNumber;
    private LocalDateTime createdAt;



}
