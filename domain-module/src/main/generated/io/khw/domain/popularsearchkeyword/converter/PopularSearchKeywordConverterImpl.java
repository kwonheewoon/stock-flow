package io.khw.domain.popularsearchkeyword.converter;

import io.khw.domain.popularsearchkeyword.dto.PopularSearchKeywordApiDto;
import io.khw.domain.popularsearchkeyword.entity.PopularSearchKeywordEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-06-10T21:30:13+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.6 (Eclipse Adoptium)"
)
@Component
public class PopularSearchKeywordConverterImpl implements PopularSearchKeywordConverter {

    @Override
    public PopularSearchKeywordApiDto toApiDto(PopularSearchKeywordEntity entity) {
        if ( entity == null ) {
            return null;
        }

        String keyword = null;
        long searchVolume = 0L;

        keyword = entity.getKeyword();
        searchVolume = entity.getSearchVolume();

        PopularSearchKeywordApiDto popularSearchKeywordApiDto = new PopularSearchKeywordApiDto( keyword, searchVolume );

        return popularSearchKeywordApiDto;
    }

    @Override
    public List<PopularSearchKeywordApiDto> convertsToList(List<PopularSearchKeywordEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<PopularSearchKeywordApiDto> list = new ArrayList<PopularSearchKeywordApiDto>( entities.size() );
        for ( PopularSearchKeywordEntity popularSearchKeywordEntity : entities ) {
            list.add( toApiDto( popularSearchKeywordEntity ) );
        }

        return list;
    }
}
