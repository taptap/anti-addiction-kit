package com.taptap.tds.registration.server.core.orika;


import com.taptap.tds.registration.server.core.enums.DescribedEnumValue;
import com.taptap.tds.registration.server.core.enums.EnumValue;
import com.taptap.tds.registration.server.core.enums.EnumValueFactory;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.Type;

public class EnumValueConverter extends CustomConverter<Object, Object> {

    @Override
    public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
        return (EnumValue.class.isAssignableFrom(sourceType.getRawType())
                && !EnumValue.class.isAssignableFrom(destinationType.getRawType()))
                || (!EnumValue.class.isAssignableFrom(sourceType.getRawType())
                        && EnumValue.class.isAssignableFrom(destinationType.getRawType()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object convert(Object source, Type<? extends Object> destinationType, MappingContext mappingContext) {
        if (source == null) {
            return null;
        }
        if (source instanceof DescribedEnumValue && destinationType.getRawType().equals(String.class)) {
            return ((DescribedEnumValue<?>) source).getDescription();
        }
        if (EnumValue.class.isAssignableFrom(destinationType.getRawType())) {
            return EnumValueFactory.get((Class<EnumValue<?>>) destinationType.getRawType(), source);
        } else {
            return ((EnumValue<?>) source).getValue();
        }
    }
}
