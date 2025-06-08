package org.omok.newomok.domain;

import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVO {
    private String userId;
    private String userPW;
    private String bio;
    private int image;

    @Builder.Default
    private int win = 0;

    @Builder.Default
    private int lose = 0;

    @Builder.Default
    private int rate = 0;
}
