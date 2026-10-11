package com.myProject.category.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryResponse implements Serializable {
    private Long id;
    private String name;
}
