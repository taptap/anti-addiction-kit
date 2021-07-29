package com.taptap.tds.registration.server.core.manager;


import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.manager.interceptor.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractManager<T> {

    protected List<QueryInterceptor<T>> queryInterceptors;

    protected List<InsertInterceptor<T>> insertInterceptors;

    protected List<UpdateInterceptor<T>> updateInterceptors;

    public void addQueryInterceptor(QueryInterceptor<T> queryInterceptor) {
        if (queryInterceptors == null) {
            queryInterceptors = new ArrayList<>(3);
        }
        queryInterceptors.add(queryInterceptor);
    }

    public void setQueryInterceptors(List<QueryInterceptor<T>> queryInterceptors) {
        this.queryInterceptors = queryInterceptors;
    }

    public void addInsertInterceptor(InsertInterceptor<T> insertInterceptor) {
        if (insertInterceptors == null) {
            insertInterceptors = new ArrayList<>(3);
        }
        insertInterceptors.add(insertInterceptor);
    }

    public void setInsertInterceptors(List<InsertInterceptor<T>> insertInterceptors) {
        this.insertInterceptors = insertInterceptors;
    }

    public void addUpdateInterceptor(UpdateInterceptor<T> updateInterceptor) {
        if (updateInterceptors == null) {
            updateInterceptors = new ArrayList<>(3);
        }
        updateInterceptors.add(updateInterceptor);
    }

    public void setUpdateInterceptors(List<UpdateInterceptor<T>> updateInterceptors) {
        this.updateInterceptors = updateInterceptors;
    }

    public void addInsertUpdateInterceptor(InsertUpdateInterceptor<T> insertUpdateInterceptor) {
        addInsertInterceptor(insertUpdateInterceptor);
        addUpdateInterceptor(insertUpdateInterceptor);
    }

    public void addManagerInterceptor(ManagerInterceptor<T> managerInterceptor) {
        addQueryInterceptor(managerInterceptor);
        addInsertUpdateInterceptor(managerInterceptor);
    }

    @PostConstruct
    public void initialize() {
        queryInterceptors = queryInterceptors == null ? Collections.<QueryInterceptor<T>> emptyList()
                : Collections.<QueryInterceptor<T>> unmodifiableList(queryInterceptors);
        insertInterceptors = insertInterceptors == null ? Collections.<InsertInterceptor<T>> emptyList()
                : Collections.<InsertInterceptor<T>> unmodifiableList(insertInterceptors);
        updateInterceptors = updateInterceptors == null ? Collections.<UpdateInterceptor<T>> emptyList()
                : Collections.<UpdateInterceptor<T>> unmodifiableList(updateInterceptors);
    }

    protected void postQuery(T entity, FieldsExpand fieldsExpand) {
        for (QueryInterceptor<T> interceptor : queryInterceptors) {
            interceptor.postQuery(entity, fieldsExpand);
        }
    }

    protected void postQuery(Collection<T> entities, FieldsExpand fieldsExpand) {
        for (QueryInterceptor<T> interceptor : queryInterceptors) {
            interceptor.postQuery(entities, fieldsExpand);
        }
    }

    protected void preInsert(T entity) {
        for (InsertInterceptor<T> interceptor : insertInterceptors) {
            interceptor.preInsert(entity);
        }
    }

    protected void preInsert(Collection<T> entities) {
        for (InsertInterceptor<T> interceptor : insertInterceptors) {
            interceptor.preInsert(entities);
        }
    }

    protected void preUpdate(T entity) {
        for (UpdateInterceptor<T> interceptor : updateInterceptors) {
            interceptor.preUpdate(entity);
        }
    }

    protected void preUpdate(Collection<T> entities) {
        for (UpdateInterceptor<T> interceptor : updateInterceptors) {
            interceptor.preUpdate(entities);
        }
    }
}
