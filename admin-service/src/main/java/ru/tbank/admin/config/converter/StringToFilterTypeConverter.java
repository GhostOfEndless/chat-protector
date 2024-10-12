package ru.tbank.admin.config.converter;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.tbank.admin.exceptions.InvalidFilterTypeException;
import ru.tbank.common.entity.FilterType;

import java.util.Arrays;

@Component
public class StringToFilterTypeConverter implements Converter<String, FilterType> {

    @Override
    public FilterType convert(@NotNull String source) {
        return Arrays.stream(FilterType.values())
                .filter(element -> element.getType().equals(source))
                .findFirst()
                .orElseThrow(InvalidFilterTypeException::new);
    }
}
