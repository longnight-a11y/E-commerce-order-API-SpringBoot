package com.example.ecapi.dto.pagination;

import java.util.List;

public record PageResponse<T>(
        List<T> item,
        int total,
        int page,
        int size
) {
}
