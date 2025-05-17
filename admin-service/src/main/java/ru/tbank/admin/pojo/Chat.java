package ru.tbank.admin.pojo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Chat {

    private Long id;
    private String name;
    private LocalDateTime additionDate;
}
