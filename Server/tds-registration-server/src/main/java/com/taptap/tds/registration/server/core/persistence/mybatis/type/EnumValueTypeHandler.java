package com.taptap.tds.registration.server.core.persistence.mybatis.type;


import com.taptap.tds.registration.server.core.enums.EnumValue;
import com.taptap.tds.registration.server.core.enums.EnumValueFactory;
import com.taptap.tds.registration.server.core.enums.EnumValueUtils;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class EnumValueTypeHandler<T extends EnumValue<?>> extends BaseTypeHandler<T> {

    private final Map<Object, EnumValue<?>> enumMap;

    private final TypeHandler<Object> typeHandler;

    @SuppressWarnings("unchecked")
    private EnumValueTypeHandler(Configuration configuration, Class<T> enumValueClass) {
        setConfiguration(configuration);
        this.enumMap = EnumValueFactory.getEnumValueMap(enumValueClass);
        Class<?> valueType = EnumValueUtils.getEnumValueActualType(enumValueClass);
        this.typeHandler = (TypeHandler<Object>) configuration.getTypeHandlerRegistry().getTypeHandler(valueType);
    }

    public static <T extends EnumValue<?>> EnumValueTypeHandler<T> create(Configuration configuration, Class<T> enumValueClass) {
        return new EnumValueTypeHandler<>(configuration, enumValueClass);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
        typeHandler.setParameter(ps, i, parameter.getValue(), jdbcType);
    }

    @Override
    public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return convertResult(typeHandler.getResult(rs, columnName));
    }

    @Override
    public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return convertResult(typeHandler.getResult(rs, columnIndex));
    }

    @Override
    public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return convertResult(typeHandler.getResult(cs, columnIndex));
    }

    @SuppressWarnings("unchecked")
    private T convertResult(Object result) {
        return result == null ? null : (T) enumMap.get(result);
    }
}
