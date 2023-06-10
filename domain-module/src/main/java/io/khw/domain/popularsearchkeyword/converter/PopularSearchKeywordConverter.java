package io.khw.domain.popularsearchkeyword.converter;


import io.khw.domain.popularsearchkeyword.dto.PopularSearchKeywordApiDto;
import io.khw.domain.popularsearchkeyword.entity.PopularSearchKeywordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PopularSearchKeywordConverter {

    @Mapping(source = "keyword", target = "keyword")
    @Mapping(source = "searchVolume", target = "searchVolume")
    PopularSearchKeywordApiDto toApiDto(PopularSearchKeywordEntity entity);

    @Mapping(source = "keyword", target = "keyword")
    @Mapping(source = "searchVolume", target = "searchVolume")
    List<PopularSearchKeywordApiDto> convertsToList(List<PopularSearchKeywordEntity> entities);

}
