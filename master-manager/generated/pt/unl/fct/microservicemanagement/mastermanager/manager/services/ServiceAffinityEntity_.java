package pt.unl.fct.microservicemanagement.mastermanager.manager.services;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ServiceAffinityEntity.class)
public abstract class ServiceAffinityEntity_ {

  public static final String SERVICE = "service";
  public static final String OTHER_SERVICE = "otherService";
  public static final String ID = "id";
  public static final String AFFINITY = "affinity";
  public static volatile SingularAttribute<ServiceAffinityEntity, ServiceEntity> service;
  public static volatile SingularAttribute<ServiceAffinityEntity, ServiceEntity> otherService;
  public static volatile SingularAttribute<ServiceAffinityEntity, Long> id;
  public static volatile SingularAttribute<ServiceAffinityEntity, Float> affinity;

}

