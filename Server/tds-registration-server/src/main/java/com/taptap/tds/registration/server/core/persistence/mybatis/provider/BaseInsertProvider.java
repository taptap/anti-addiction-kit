package com.taptap.tds.registration.server.core.persistence.mybatis.provider;

import com.taptap.tds.registration.server.core.persistence.DuplicateKeyUpdateColumnMetadata;
import com.taptap.tds.registration.server.core.persistence.EntityInformation;
import com.taptap.tds.registration.server.util.Collections3;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BaseInsertProvider extends SqlProviderSupport {

    public static CharSequence insert(ProviderContext context) {

        EntityInformation entityInformation = getCurrentEntityInformation(context);
        Map<String, String> insertableColumns = entityInformation.getInsertableColumns();

        String baseInsertSql = generateBaseInsertSql(entityInformation);
        StringBuilder sqlBuilder = new StringBuilder(baseInsertSql.length() + 100);
        sqlBuilder.append(baseInsertSql);
        sqlBuilder.append('(');
        boolean first = true;
        for (String property : insertableColumns.values()) {
            if (first) {
                first = false;
            } else {
                sqlBuilder.append(',');
            }
            sqlBuilder.append("#{");
            sqlBuilder.append(property);
            sqlBuilder.append('}');
        }
        sqlBuilder.append(')');
        appendDuplicateKeyUpdateSql(sqlBuilder, entityInformation);

        return sqlBuilder;
    }

    public static CharSequence bulkInsert(ProviderContext providerContext, @Param("collection") Iterable<?> entities) {

        EntityInformation entityInformation = getCurrentEntityInformation(providerContext);
        Map<String, String> insertableColumns = entityInformation.getInsertableColumns();

        String baseInsertSql = generateBaseInsertSql(entityInformation);
        StringBuilder sqlBuilder = new StringBuilder(baseInsertSql.length() + 100);
        sqlBuilder.append(baseInsertSql);

        Iterator<?> iterator = entities.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            iterator.next();
            if (i > 0) {
                sqlBuilder.append(',');
            }
            sqlBuilder.append('(');
            boolean first = true;
            for (String property : insertableColumns.values()) {
                if (first) {
                    first = false;
                } else {
                    sqlBuilder.append(',');
                }
                sqlBuilder.append("#{collection[");
                sqlBuilder.append(i);
                sqlBuilder.append("].");
                sqlBuilder.append(property);
                sqlBuilder.append('}');
            }
            sqlBuilder.append(')');
        }
        appendDuplicateKeyUpdateSql(sqlBuilder, entityInformation);

        return sqlBuilder;
    }

    private static String generateBaseInsertSql(EntityInformation entityInformation) {

        StringBuilder sqlBuilder = new StringBuilder(128);
        sqlBuilder.append("INSERT INTO ");
        sqlBuilder.append(entityInformation.getTableName());
        sqlBuilder.append(" (");
        boolean first = true;
        for (String column : entityInformation.getInsertableColumns().keySet()) {
            if (first) {
                first = false;
            } else {
                sqlBuilder.append(',');
            }
            sqlBuilder.append('`');
            sqlBuilder.append(column);
            sqlBuilder.append('`');
        }

        sqlBuilder.append(") VALUES ");

        return sqlBuilder.toString();
    }

    private static void appendDuplicateKeyUpdateSql(StringBuilder sqlBuilder, EntityInformation entityInformation) {

        List<DuplicateKeyUpdateColumnMetadata> duplicateKeyUpdateColumns = entityInformation.getDuplicateKeyUpdateColumns();
        if (Collections3.isEmpty(duplicateKeyUpdateColumns)) {
            return;
        }

        sqlBuilder.append(" ON DUPLICATE KEY UPDATE ");
        boolean first = true;
        for (DuplicateKeyUpdateColumnMetadata metadata : duplicateKeyUpdateColumns) {
            if (first) {
                first = false;
            } else {
                sqlBuilder.append(',');
            }
            sqlBuilder.append(metadata.getColumn());
            sqlBuilder.append(" = ");
            if (metadata.isNullable()) {
                sqlBuilder.append("VALUES(");
                sqlBuilder.append(metadata.getColumn());
                sqlBuilder.append(')');
            } else {
                sqlBuilder.append("COALESCE(VALUES(");
                sqlBuilder.append(metadata.getColumn());
                sqlBuilder.append("), ");
                sqlBuilder.append(metadata.getColumn());
                sqlBuilder.append(')');
            }
        }
    }
}
