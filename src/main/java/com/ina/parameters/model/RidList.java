package com.ina.parameters.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RidList {
    private List<RidData> ridDataList;
}

