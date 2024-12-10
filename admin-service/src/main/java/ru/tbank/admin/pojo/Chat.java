package ru.tbank.admin.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Chat {

    private Long id;
    private String name;
    private LocalDateTime additionDate;
}
