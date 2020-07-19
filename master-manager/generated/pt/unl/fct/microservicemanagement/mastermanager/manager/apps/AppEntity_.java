package pt.unl.fct.microservicemanagement.mastermanager.manager.apps;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(AppEntity.class)
public abstract class AppEntity_ {

  public static final String APP_SERVICES = "appServices";
  public static final String NAME = "name";
  public static final String ID = "id";
  public static volatile SetAttribute<AppEntity, AppServiceEntity> appServices;
  public static volatile SingularAttribute<AppEntity, String> name;
  public static volatile SingularAttribute<AppEntity, Long> id;

}

