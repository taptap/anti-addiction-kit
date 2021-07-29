package com.taptap.tds.registration.server.manager;

import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.domain.IdentificationDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class ReactiveIdentificationDetailsManager {

    @Autowired
    private IdentificationDetailsManager identificationDetailsManager;

    public Mono<IdentificationDetails> findByUserId(String userId, FieldsExpand fieldsExpand){
        return Mono.fromCallable(()->identificationDetailsManager.findByUserId(userId, fieldsExpand))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> delete(IdentificationDetails identificationDetails){
        return Mono.fromCallable(()->identificationDetailsManager.delete(identificationDetails))
                .subscribeOn(Schedulers.boundedElastic()).then();

    }

    public Mono<Void> save(IdentificationDetails identificationDetails){
        return Mono.fromCallable(()->identificationDetailsManager.save(identificationDetails))
                .subscribeOn(Schedulers.boundedElastic()).then();

    }

}
