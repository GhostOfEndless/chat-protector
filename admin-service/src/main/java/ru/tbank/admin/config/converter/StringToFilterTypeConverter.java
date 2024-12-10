package ru.tbank.admin.config.converter;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.tbank.admin.exceptions.InvalidFilterTypeException;
import ru.tbank.common.entity.enums.FilterType;

@Component
public class StringToFilterTypeConverter implements Converter<String, FilterType> {

    @Override
    public FilterType convert(@NonNull String source) {
        try {
            var type = source.replace('-', '_').toUpperCase();
            return FilterType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new InvalidFilterTypeException();
        }
    }
}
