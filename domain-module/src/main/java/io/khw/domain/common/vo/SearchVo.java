package io.khw.domain.common.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchVo {

    private int page;

    private int size;

    public long getStartIndex() {

        if (page <= 0) {
            page = 1;
        }

        if (size < 1) {
            size = 1;
        }

        return (page - 1) * size;
    }

    public long getEndIndex() {
        return getStartIndex() + size -1;
    }
}
