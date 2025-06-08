package org.omok.newomok.domain;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatVO {
    private int rank;
    private String userId;
    private int win;
    private int loss;
    private double rate;
    private int imageNum;
}

