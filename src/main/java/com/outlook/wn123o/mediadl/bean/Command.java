package com.outlook.wn123o.mediadl.bean;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Command {
    private String action;
    private String args;
}
