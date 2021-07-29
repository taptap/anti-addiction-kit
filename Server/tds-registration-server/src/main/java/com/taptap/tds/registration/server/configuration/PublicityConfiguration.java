package com.taptap.tds.registration.server.configuration;

import com.taptap.tds.registration.server.core.datastore.DataStore;
import com.taptap.tds.registration.server.core.datastore.InMemoryDataStore;
import com.taptap.tds.registration.server.domain.UserAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PublicityConfiguration {

    @Bean
    public DataStore<UserAction> dataStore() {
        return new InMemoryDataStore(2000);
    }

}
