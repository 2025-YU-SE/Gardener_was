package com.example.codegardener.feedback.dto;

import com.example.codegardener.feedback.domain.LineFeedback;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LineFeedbackDto {
    private Long lineFeedbackId;
    private Long userId;
    private Integer lineNumber;
    private Integer endLineNumber;
    private String content;
    private LocalDateTime createdAt;

    public static LineFeedbackDto fromEntity(LineFeedback entity) {
        return LineFeedbackDto.builder()
                .lineFeedbackId(entity.getLineFeedbackId())
                .userId(entity.getUser().getId())
                .lineNumber(entity.getLineNumber())
                .endLineNumber(entity.getEndLineNumber())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
