package com.taptap.tds.registration.server.configuration;

import com.google.common.collect.ImmutableList;
import com.taptap.tds.registration.server.core.enums.EnumValueUtils;
import com.taptap.tds.registration.server.core.persistence.EntityInformation;
import com.taptap.tds.registration.server.core.persistence.EntityInformationFactory;
import com.taptap.tds.registration.server.core.persistence.OneToOneMetadata;
import com.taptap.tds.registration.server.core.persistence.mybatis.dialect.DialectFactory;
import com.taptap.tds.registration.server.core.persistence.mybatis.mapper.BaseSelectMapper;
import com.taptap.tds.registration.server.core.persistence.mybatis.plugin.FieldsExpandInterceptor;
import com.taptap.tds.registration.server.core.persistence.mybatis.plugin.OffsetLimitInterceptor;
import com.taptap.tds.registration.server.core.persistence.mybatis.provider.BaseInsertProvider;
import com.taptap.tds.registration.server.core.persistence.mybatis.type.EnumValueTypeHandler;
import com.taptap.tds.registration.server.core.persistence.mybatis.util.MybatisUtils;
import com.taptap.tds.registration.server.util.Collections3;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.io.ResolverUtil;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

@org.springframework.context.annotation.Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@AutoConfigureBefore(MybatisAutoConfiguration.class)
public class CustomMybatisConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomMybatisConfiguration.class);

    private Configuration configuration;

    private ApplicationContext applicationContext;

    public CustomMybatisConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer(final MybatisProperties properties, final DataSource dataSource) {
        return configuration -> {
            configuration.setVfsImpl(SpringBootVFS.class);
            configuration.setCacheEnabled(false);
            configuration.setUseGeneratedKeys(false);
            configuration.setDefaultExecutorType(ExecutorType.REUSE);
            configuration.setMapUnderscoreToCamelCase(true);
            configuration.setLogImpl(Slf4jImpl.class);

            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            String enumValuePackages = null;
            if (Collections3.isNotEmpty(properties.getConfigurationProperties())) {
                enumValuePackages = (String) properties.getConfigurationProperties().get("enumValuePackages");
            }
            if (StringUtils.isEmpty(enumValuePackages)) {
                LOGGER.warn("Enum value packages are not configured.");
            } else {
                EnumValueUtils.scanEnumValuePackages(
                        type -> typeHandlerRegistry.register(type, EnumValueTypeHandler.create(configuration, type)),
                        tokenizeToStringArray(enumValuePackages, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
            }

            // Execution order from buttom to top
            configuration.addInterceptor(new OffsetLimitInterceptor(DialectFactory.fromProductName(dataSource)));
            configuration.addInterceptor(new FieldsExpandInterceptor());
            generateBaseResultMaps(configuration);
            CustomMybatisConfiguration.this.configuration = configuration;
        };
    }

    private void generateBaseResultMaps(final Configuration configuration) {

        Map<Class<?>, Class<?>> entityClassMapperClassMap = new HashMap<>();
        MethodFilter methodFilter = new BaseResultMapAnnotatedMethodFilter();

        List<String> packages = AutoConfigurationPackages.get(this.applicationContext);
        for (String packageToScan : packages) {
            ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
            resolverUtil.setClassLoader(ClassUtils.getDefaultClassLoader());
            resolverUtil.find(new ResolverUtil.AnnotatedWith(Mapper.class), packageToScan);
            Set<Class<? extends Class<?>>> typeSet = resolverUtil.getClasses();
            for (Class<?> type : typeSet) {
                // Ignore inner classes (including package-info.java)
                if (type.isInterface() && !type.isAnonymousClass() && !type.isMemberClass()) {
                    ReflectionUtils.doWithMethods(type, method -> {
                        Class<?> entityClass = MybatisUtils.getMapperMethodReturnType(type, method);
                        if (!entityClassMapperClassMap.containsKey(entityClass)) {
                            entityClassMapperClassMap.put(entityClass, type);
                        }
                    }, methodFilter);
                }
            }
        }

        for (Class<?> entityClass : entityClassMapperClassMap.keySet()) {
            LOGGER.debug("Start to build BaseResultMap for {}", entityClass);
            buildBaseResultMap(configuration, entityClass, entityClassMapperClassMap);
        }
    }

    private void buildBaseResultMap(Configuration configuration, Class<?> entityClass,
            Map<Class<?>, Class<?>> entityClassMapperClassMap) {

        EntityInformation entityInformation = EntityInformationFactory.getEntityInformation(entityClass);

        List<ResultMapping> propertyResultMappings = buildPropertyResultMappings(configuration, entityInformation);
        List<ResultMapping> associationResultMappings = buildAssociationResultMappings(configuration, entityInformation,
                entityClassMapperClassMap);

        List<ResultMapping> resultMappings = new ArrayList<>(associationResultMappings.size() + propertyResultMappings.size());
        resultMappings.addAll(propertyResultMappings);
        resultMappings.addAll(associationResultMappings);

        Class<?> type = entityClassMapperClassMap.get(entityClass);
        if (type == null) {
            type = entityClass;
        }
        String resultMapId = generateBaseResultMapId(type);
        ResultMap.Builder builder = new ResultMap.Builder(configuration, resultMapId, entityInformation.getEntityClass(),
                resultMappings);

        configuration.addResultMap(builder.build());
    }

    private String generateBaseResultMapId(Class<?> type) {
        return type.getName() + '.' + BaseSelectMapper.BASE_RESULT_MAP_NAME;
    }

    private List<ResultMapping> buildPropertyResultMappings(Configuration configuration, EntityInformation entityInformation) {

        List<ResultMapping> resultMappings = new ArrayList<>(entityInformation.getColumnPropertyMap().size());
        for (Map.Entry<String, Field> entry : entityInformation.getColumnPropertyMap().entrySet()) {
            String column = entry.getKey();
            Field property = entry.getValue();
            ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property.getName());
            builder.column(column).javaType(property.getType());
            if (entityInformation.getIdColumnPropertyMap().containsKey(column)) {
                builder.flags(ImmutableList.of(ResultFlag.ID));
            }
            resultMappings.add(builder.build());
        }

        return resultMappings;
    }

    private List<ResultMapping> buildAssociationResultMappings(Configuration configuration, EntityInformation entityInformation,
            Map<Class<?>, Class<?>> entityClassMapperClassMap) {

        Collection<OneToOneMetadata> oneToOneMetadatas = entityInformation.getOneToOneMetadatas();
        List<ResultMapping> resultMappings = new ArrayList<>(oneToOneMetadatas.size());
        for (OneToOneMetadata metadata : oneToOneMetadatas) {

            Class<?> targetEntityClass = metadata.getTargetEntityClass();
            Class<?> mapperClass = entityClassMapperClassMap.get(targetEntityClass);
            String nestedResultMapId;
            if (mapperClass == null) {
                nestedResultMapId = generateBaseResultMapId(targetEntityClass);
                if (!configuration.hasResultMap(nestedResultMapId)) {
                    buildBaseResultMap(configuration, targetEntityClass, entityClassMapperClassMap);
                }
            } else {
                nestedResultMapId = generateBaseResultMapId(mapperClass);
            }

            EntityInformation targetEntityInformation = EntityInformationFactory.getEntityInformation(targetEntityClass);
            ResultMapping.Builder builder = new ResultMapping.Builder(configuration, metadata.getProperty().getName());
            builder.javaType(targetEntityClass).nestedResultMapId(nestedResultMapId).columnPrefix(
                    entityInformation.getTableNameAlias() + "_" + targetEntityInformation.getTableNameAlias() + "_");
            resultMappings.add(builder.build());
        }

        return resultMappings;
    }

    @Bean
    @ConditionalOnResource(resources = "classpath:databaseIdProvider.properties")
    public DatabaseIdProvider databaseIdProvider() throws IOException {

        Resource resource = applicationContext.getResource("classpath:databaseIdProvider.properties");
        Properties properties = PropertiesLoaderUtils.loadProperties(resource);

        DatabaseIdProvider databaseIdProvider = new VendorDatabaseIdProvider();
        databaseIdProvider.setProperties(properties);
        return databaseIdProvider;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        for (Object object : configuration.getMappedStatements()) {
            if (object instanceof MappedStatement) {
                MappedStatement mappedStatement = (MappedStatement) object;
                if (mappedStatement.getSqlCommandType() == SqlCommandType.INSERT
                        && isProvidedByBaseInsertProvider(mappedStatement)) {
                    processMappedStatementKeyGenerationInfo(mappedStatement);
                }
            }
        }
    }

    private boolean isProvidedByBaseInsertProvider(MappedStatement mappedStatement) {
        if (!(mappedStatement.getSqlSource() instanceof ProviderSqlSource)) {
            return false;
        }

        SqlSource sqlSource = mappedStatement.getSqlSource();
        Field providerTypeField = ReflectionUtils.findField(ProviderSqlSource.class, "providerType");
        ReflectionUtils.makeAccessible(providerTypeField);
        Class<?> providerType = (Class<?>) ReflectionUtils.getField(providerTypeField, sqlSource);
        return providerType == BaseInsertProvider.class;
    }

    private void processMappedStatementKeyGenerationInfo(MappedStatement mappedStatement) {

        Class<?> entityClass = MybatisUtils.getMapperEntityType(mappedStatement.getId());
        EntityInformation entityInformation = EntityInformationFactory.getEntityInformation(entityClass);
        if (!entityInformation.getIdMetadata().isAutoGenerated()) {
            return;
        }

        Map<String, String> idColumnPropertyMap = entityInformation.getIdColumnPropertyMap();
        MetaObject mappedStatementMeta = null;
        if (ArrayUtils.isEmpty(mappedStatement.getKeyProperties())) {
            mappedStatementMeta = SystemMetaObject.forObject(mappedStatement);
            mappedStatementMeta.setValue("keyProperties",
                    idColumnPropertyMap.values().toArray(new String[idColumnPropertyMap.size()]));
        }
        if (ArrayUtils.isEmpty(mappedStatement.getKeyColumns())) {
            if (mappedStatementMeta == null) {
                mappedStatementMeta = SystemMetaObject.forObject(mappedStatement);
            }
            mappedStatementMeta.setValue("keyColumns",
                    idColumnPropertyMap.keySet().toArray(new String[idColumnPropertyMap.size()]));
        }
        if (mappedStatement.getKeyGenerator() == NoKeyGenerator.INSTANCE) {
            if (mappedStatementMeta == null) {
                mappedStatementMeta = SystemMetaObject.forObject(mappedStatement);
            }
            mappedStatementMeta.setValue("keyGenerator", Jdbc3KeyGenerator.INSTANCE);
        }
    }

    private static class BaseResultMapAnnotatedMethodFilter implements MethodFilter {

        @Override
        public boolean matches(Method method) {
            org.apache.ibatis.annotations.ResultMap resultMap = method
                    .getAnnotation(org.apache.ibatis.annotations.ResultMap.class);
            if (resultMap == null) {
                return false;
            }
            return ArrayUtils.contains(resultMap.value(), BaseSelectMapper.BASE_RESULT_MAP_NAME);
        }
    }
}
