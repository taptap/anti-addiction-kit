package com.taptap.tds.registration.server.core.persistence;

import com.taptap.tds.registration.server.util.Collections3;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;

public class OneToOneExpandMetadata {

    private final String selectListExpression;

    private final Collection<OneToOneMetadata> oneToOneMetadatas;

    private String joinExpression = "";

    public OneToOneExpandMetadata(String selectListExpression, Collection<OneToOneMetadata> oneToOneMetadatas) {
        this.selectListExpression = selectListExpression;
        this.oneToOneMetadatas = oneToOneMetadatas == null ? Collections.<OneToOneMetadata> emptyList() : oneToOneMetadatas;
    }

    public String getSelectListExpression() {
        return selectListExpression;
    }

    public Collection<OneToOneMetadata> getOneToOneMetadatas() {
        return oneToOneMetadatas;
    }

    public String getJoinExpression() {
        if (Collections3.isEmpty(oneToOneMetadatas)) {
            return joinExpression;
        }
        if (StringUtils.isEmpty(joinExpression)) {
            StringBuilder joinBuilder = new StringBuilder(64);
            appendJoinExpression(joinBuilder);
            joinExpression = joinBuilder.toString();
        }
        return joinExpression;
    }

    public void appendJoinExpression(StringBuilder sqlBuilder) {
        for (OneToOneMetadata oneToOneMetadata : oneToOneMetadatas) {
            switch (oneToOneMetadata.getJoinType()) {
                case INNER:
                    sqlBuilder.append(" INNER ");
                    break;
                case LEFT:
                    sqlBuilder.append(" LEFT ");
                    break;
                default:
                    break;
            }
            EntityInformation mainEntityInformation = EntityInformationFactory
                    .getEntityInformation(oneToOneMetadata.getProperty().getDeclaringClass());
            EntityInformation targetEntityInformation = EntityInformationFactory
                    .getEntityInformation(oneToOneMetadata.getTargetEntityClass());
            sqlBuilder.append("JOIN ");
            sqlBuilder.append(targetEntityInformation.getTableName());
            sqlBuilder.append(" ON ");
            sqlBuilder.append(mainEntityInformation.getTableName());
            sqlBuilder.append(".");
            sqlBuilder.append(oneToOneMetadata.getForeignKeyColumn());
            sqlBuilder.append(" = ");
            sqlBuilder.append(targetEntityInformation.getTableName());
            sqlBuilder.append(".");
            sqlBuilder.append(oneToOneMetadata.getReferencedColumn());
            String logicalDeletionColumn = targetEntityInformation.getLogicalDeletionColumn();
            if (StringUtils.isNotEmpty(logicalDeletionColumn)) {
                sqlBuilder.append(" AND ");
                sqlBuilder.append(targetEntityInformation.getTableName());
                sqlBuilder.append(".");
                sqlBuilder.append(logicalDeletionColumn);
                sqlBuilder.append(" = 0");
            }
        }
    }
}
