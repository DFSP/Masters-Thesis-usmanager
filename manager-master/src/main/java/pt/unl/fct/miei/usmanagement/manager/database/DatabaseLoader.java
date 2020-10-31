/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package pt.unl.fct.miei.usmanagement.manager.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pt.unl.fct.miei.usmanagement.manager.MasterManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceType;
import pt.unl.fct.miei.usmanagement.manager.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServiceRepository;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEntity;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencyEntity;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencyRepository;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.fields.FieldEntity;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHostEntity;
import pt.unl.fct.miei.usmanagement.manager.management.apps.AppsService;
import pt.unl.fct.miei.usmanagement.manager.management.componenttypes.ComponentTypesService;
import pt.unl.fct.miei.usmanagement.manager.management.docker.DockerProperties;
import pt.unl.fct.miei.usmanagement.manager.management.docker.proxy.DockerApiProxyService;
import pt.unl.fct.miei.usmanagement.manager.management.fields.FieldsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.cloud.CloudHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.hosts.edge.EdgeHostsService;
import pt.unl.fct.miei.usmanagement.manager.management.loadbalancer.nginx.NginxLoadBalancerService;
import pt.unl.fct.miei.usmanagement.manager.management.location.LocationRequestsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.HostsMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.ServicesMonitoringService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.HostsEventsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.events.ServicesEventsService;
import pt.unl.fct.miei.usmanagement.manager.management.monitoring.prometheus.PrometheusService;
import pt.unl.fct.miei.usmanagement.manager.management.operators.OperatorsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.condition.ConditionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.decision.DecisionsService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.HostRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.rulesystem.rules.ServiceRulesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.ServicesService;
import pt.unl.fct.miei.usmanagement.manager.management.services.discovery.registration.RegistrationServerService;
import pt.unl.fct.miei.usmanagement.manager.management.valuemodes.ValueModesService;
import pt.unl.fct.miei.usmanagement.manager.management.workermanagers.WorkerManagerProperties;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQuery;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEntity;
import pt.unl.fct.miei.usmanagement.manager.regions.Region;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.ConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.DecisionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleConditionRepository;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleConditionEntity;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleConditionRepository;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleEntity;
import pt.unl.fct.miei.usmanagement.manager.users.UserEntity;
import pt.unl.fct.miei.usmanagement.manager.users.UserRole;
import pt.unl.fct.miei.usmanagement.manager.users.UsersService;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueModeEntity;

import java.util.List;

@Slf4j
@Component
public class DatabaseLoader {

	@Bean
	CommandLineRunner initDatabase(UsersService usersService,
								   ServicesService servicesService,
								   AppsService appsService, AppServiceRepository appsServices,
								   ServiceDependencyRepository servicesDependencies, /*RegionsService regionsService,*/
								   EdgeHostsService edgeHostsService, CloudHostsService cloudHostsService,
								   ComponentTypesService componentTypesService,
								   OperatorsService operatorsService, DecisionsService decisionsService,
								   FieldsService fieldsService, ValueModesService valueModesService,
								   ConditionsService conditionsService, HostRulesService hostRulesService,
								   HostRuleConditionRepository hostRuleConditions,
								   ServiceRulesService serviceRulesService,
								   ServiceRuleConditionRepository serviceRuleConditions,
								   DockerProperties dockerProperties, HostsEventsService hostsEventsService,
								   ServicesEventsService servicesEventsService, HostsMonitoringService hostsMonitoringService,
								   ServicesMonitoringService servicesMonitoringService) {
		return args -> {

			String dockerHubUsername = dockerProperties.getHub().getUsername();

			// users
			if (!usersService.hasUser("admin")) {
				UserEntity sysAdmin = UserEntity.builder()
					.firstName("admin")
					.lastName("admin")
					.username("admin")
					.password("admin")
					.email("admin@admin.pt")
					.role(UserRole.ROLE_SYS_ADMIN)
					.build();
				usersService.addUser(sysAdmin);
			}
			if (!usersService.hasUser("danielfct")) {
				UserEntity sysAdmin = UserEntity.builder()
					.firstName("daniel")
					.lastName("pimenta")
					.username("danielfct")
					.password("danielfct")
					.email("d.pimenta@campus.fct.unl.pt")
					.role(UserRole.ROLE_SYS_ADMIN)
					.build();
				usersService.addUser(sysAdmin);
			}
			// services
			ServiceEntity frontend;
			try {
				frontend = servicesService.getService("sock-shop-front-end");
			}
			catch (EntityNotFoundException ignored) {
				frontend = ServiceEntity.builder()
					.serviceName("sock-shop-front-end")
					.dockerRepository(dockerHubUsername + "/sock-shop-front-end")
					.defaultExternalPort("8081")
					.defaultInternalPort("80")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${front-endHost}")
					.serviceType(ServiceType.FRONTEND)
					.expectedMemoryConsumption(209715200d)
					.build();
				frontend = servicesService.addService(frontend);
			}
			ServiceEntity user;
			try {
				user = servicesService.getService("sock-shop-user");
			}
			catch (EntityNotFoundException ignored) {
				user = ServiceEntity.builder()
					.serviceName("sock-shop-user")
					.dockerRepository(dockerHubUsername + "/sock-shop-user")
					.defaultExternalPort("8082")
					.defaultInternalPort("80")
					.defaultDb("user-db:27017")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${userDatabaseHost}")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${userHost}")
					.serviceType(ServiceType.BACKEND)
					.expectedMemoryConsumption(62914560d)
					.build();
				user = servicesService.addService(user);
			}
			ServiceEntity userDb;
			try {
				userDb = servicesService.getService("sock-shop-user-db");
			}
			catch (EntityNotFoundException ignored) {
				userDb = ServiceEntity.builder()
					.serviceName("sock-shop-user-db")
					.dockerRepository(dockerHubUsername + "/sock-shop-user-db")
					.defaultExternalPort("27017")
					.defaultInternalPort("27017")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${userDatabaseHost}")
					.serviceType(ServiceType.DATABASE)
					.expectedMemoryConsumption(262144000d)
					.build();
				userDb = servicesService.addService(userDb);
			}
			ServiceEntity catalogue;
			try {
				catalogue = servicesService.getService("sock-shop-catalogue");
			}
			catch (EntityNotFoundException ignored) {
				catalogue = ServiceEntity.builder()
					.serviceName("sock-shop-catalogue")
					.dockerRepository(dockerHubUsername + "/sock-shop-catalogue")
					.defaultExternalPort("8083")
					.defaultInternalPort("80")
					.defaultDb("catalogue-db:3306")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${catalogueDatabaseHost}")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${catalogueHost}")
					.serviceType(ServiceType.BACKEND)
					.expectedMemoryConsumption(62914560d)
					.build();
				catalogue = servicesService.addService(catalogue);
			}
			ServiceEntity catalogueDb;
			try {
				catalogueDb = servicesService.getService("sock-shop-catalogue-db");
			}
			catch (EntityNotFoundException ignored) {
				catalogueDb = ServiceEntity.builder()
					.serviceName("sock-shop-catalogue-db")
					.dockerRepository(dockerHubUsername + "/sock-shop-catalogue-db")
					.defaultExternalPort("3306")
					.defaultInternalPort("3306")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${catalogueDatabaseHost}")
					.serviceType(ServiceType.DATABASE)
					.expectedMemoryConsumption(262144000d)
					.build();
				catalogueDb = servicesService.addService(catalogueDb);
			}
			ServiceEntity payment;
			try {
				payment = servicesService.getService("sock-shop-payment");
			}
			catch (EntityNotFoundException ignored) {
				payment = ServiceEntity.builder()
					.serviceName("sock-shop-payment")
					.dockerRepository(dockerHubUsername + "/sock-shop-payment")
					.defaultExternalPort("8084")
					.defaultInternalPort("80")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${paymentHost}")
					.serviceType(ServiceType.BACKEND)
					.expectedMemoryConsumption(62914560d)
					.build();
				payment = servicesService.addService(payment);
			}
			ServiceEntity carts;
			try {
				carts = servicesService.getService("sock-shop-carts");
			}
			catch (EntityNotFoundException ignored) {
				carts = ServiceEntity.builder()
					.serviceName("sock-shop-carts")
					.dockerRepository(dockerHubUsername + "/sock-shop-carts")
					.defaultExternalPort("8085")
					.defaultInternalPort("80")
					.defaultDb("carts-db:27017")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${cartsDatabaseHost}")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${cartsHost}")
					.serviceType(ServiceType.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				carts = servicesService.addService(carts);
			}
			ServiceEntity cartsDb;
			try {
				cartsDb = servicesService.getService("sock-shop-carts-db");
			}
			catch (EntityNotFoundException ignored) {
				cartsDb = ServiceEntity.builder()
					.serviceName("sock-shop-carts-db")
					.dockerRepository(dockerHubUsername + "/sock-shop-carts-db")
					.defaultExternalPort("27016")
					.defaultInternalPort("27017")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${cartsDatabaseHost}")
					.serviceType(ServiceType.DATABASE)
					.expectedMemoryConsumption(262144000d)
					.build();
				cartsDb = servicesService.addService(cartsDb);
			}
			ServiceEntity orders;
			try {
				orders = servicesService.getService("sock-shop-orders");
			}
			catch (EntityNotFoundException ignored) {
				orders = ServiceEntity.builder()
					.serviceName("sock-shop-orders")
					.dockerRepository(dockerHubUsername + "/sock-shop-orders")
					.defaultExternalPort("8086")
					.defaultInternalPort("80")
					.defaultDb("orders-db:27017")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${ordersDatabaseHost}")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${ordersHost}")
					.serviceType(ServiceType.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				orders = servicesService.addService(orders);
			}
			ServiceEntity ordersDb;
			try {
				ordersDb = servicesService.getService("sock-shop-orders-db");
			}
			catch (EntityNotFoundException ignored) {
				ordersDb = ServiceEntity.builder()
					.serviceName("sock-shop-orders-db")
					.dockerRepository(dockerHubUsername + "/sock-shop-orders-db")
					.defaultExternalPort("27015")
					.defaultInternalPort("27017")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${ordersDatabaseHost}")
					.serviceType(ServiceType.DATABASE)
					.expectedMemoryConsumption(262144000d)
					.build();
				ordersDb = servicesService.addService(ordersDb);
			}
			ServiceEntity shipping;
			try {
				shipping = servicesService.getService("sock-shop-shipping");
			}
			catch (EntityNotFoundException ignored) {
				shipping = ServiceEntity.builder()
					.serviceName("sock-shop-shipping")
					.dockerRepository(dockerHubUsername + "/sock-shop-shipping")
					.defaultExternalPort("8087")
					.defaultInternalPort("80")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${rabbitmqHost}")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${shippingHost}")
					.serviceType(ServiceType.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				shipping = servicesService.addService(shipping);
			}
			ServiceEntity queueMaster;
			try {
				queueMaster = servicesService.getService("sock-shop-queue-master");
			}
			catch (EntityNotFoundException ignored) {
				queueMaster = ServiceEntity.builder()
					.serviceName("sock-shop-queue-master")
					.dockerRepository(dockerHubUsername + "/sock-shop-queue-master")
					.defaultExternalPort("8088")
					.defaultInternalPort("80")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${rabbitmqHost}")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${queue-masterHost}")
					.serviceType(ServiceType.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				queueMaster = servicesService.addService(queueMaster);
			}
			ServiceEntity rabbitmq;
			try {
				rabbitmq = servicesService.getService("sock-shop-rabbitmq");
			}
			catch (EntityNotFoundException ignored) {
				rabbitmq = ServiceEntity.builder()
					.serviceName("sock-shop-rabbitmq")
					.dockerRepository(dockerHubUsername + "/sock-shop-rabbitmq")
					.defaultExternalPort("5672")
					.defaultInternalPort("5672")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.minReplicas(1)
					.maxReplicas(1)
					.outputLabel("${rabbitmqHost}")
					.serviceType(ServiceType.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				rabbitmq = servicesService.addService(rabbitmq);
			}
			ServiceEntity loadBalancer;
			try {
				loadBalancer = servicesService.getService(NginxLoadBalancerService.LOAD_BALANCER);
			}
			catch (EntityNotFoundException ignored) {
				loadBalancer = ServiceEntity.builder()
					.serviceName(NginxLoadBalancerService.LOAD_BALANCER)
					.dockerRepository(dockerHubUsername + "/nginx-load-balancer")
					.defaultExternalPort("1906")
					.defaultInternalPort("80")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${loadBalancerHost}")
					.serviceType(ServiceType.SYSTEM)
					.expectedMemoryConsumption(10485760d)
					.build();
				loadBalancer = servicesService.addService(loadBalancer);
			}
			ServiceEntity requestLocationMonitor;
			try {
				requestLocationMonitor = servicesService.getService(LocationRequestsService.REQUEST_LOCATION_MONITOR);
			}
			catch (EntityNotFoundException ignored) {
				requestLocationMonitor = ServiceEntity.builder()
					.serviceName(LocationRequestsService.REQUEST_LOCATION_MONITOR)
					.dockerRepository(dockerHubUsername + "/request-location-monitor")
					.defaultExternalPort("1919")
					.defaultInternalPort("1919")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${requestLocationMonitorHost}")
					.serviceType(ServiceType.SYSTEM)
					.expectedMemoryConsumption(52428800d)
					.build();
				requestLocationMonitor = servicesService.addService(requestLocationMonitor);
			}
			ServiceEntity registrationServer;
			try {
				registrationServer = servicesService.getService(RegistrationServerService.REGISTRATION_SERVER);
			}
			catch (EntityNotFoundException ignored) {
				registrationServer = ServiceEntity.builder()
					.serviceName(RegistrationServerService.REGISTRATION_SERVER)
					.dockerRepository(dockerHubUsername + "/registration-server")
					.defaultExternalPort("8761")
					.defaultInternalPort("8761")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("${externalPort} ${internalPort} ${hostname} ${zone}")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${registrationHost}")
					.serviceType(ServiceType.SYSTEM)
					.expectedMemoryConsumption(262144000d)
					.build();
				registrationServer = servicesService.addService(registrationServer);
			}
			ServiceEntity prometheus;
			try {
				prometheus = servicesService.getService(PrometheusService.PROMETHEUS);
			}
			catch (EntityNotFoundException ignored) {
				prometheus = ServiceEntity.builder()
					.serviceName(PrometheusService.PROMETHEUS)
					.dockerRepository(dockerHubUsername + "/prometheus")
					.defaultExternalPort("9090")
					.defaultInternalPort("9090")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${prometheusHost}")
					.serviceType(ServiceType.SYSTEM)
					.expectedMemoryConsumption(52428800d)
					.build();
				prometheus = servicesService.addService(prometheus);
			}
			ServiceEntity dockerApiProxy;
			try {
				dockerApiProxy = servicesService.getService(DockerApiProxyService.DOCKER_API_PROXY);
			}
			catch (EntityNotFoundException ignored) {
				dockerApiProxy = ServiceEntity.builder()
					.serviceName(DockerApiProxyService.DOCKER_API_PROXY)
					.dockerRepository(dockerHubUsername + "/nginx-basic-auth-proxy")
					.defaultExternalPort(String.valueOf(dockerProperties.getApiProxy().getPort()))
					.defaultInternalPort("80")
					.defaultDb("NOT_APPLICABLE")
					.launchCommand("")
					.minReplicas(1)
					.maxReplicas(0)
					.outputLabel("${dockerApiProxyHost}")
					.serviceType(ServiceType.SYSTEM)
					.expectedMemoryConsumption(10485760d)
					.build();
				dockerApiProxy = servicesService.addService(dockerApiProxy);
			}
			ServiceEntity workerManager;
			try {
				workerManager = servicesService.getService(WorkerManagerProperties.WORKER_MANAGER);
			}
			catch (EntityNotFoundException ignored) {
				workerManager = ServiceEntity.builder()
					.serviceName(WorkerManagerProperties.WORKER_MANAGER)
					.dockerRepository(dockerHubUsername + "/manager-worker")
					.defaultExternalPort("8081")
					.defaultInternalPort("8081")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.minReplicas(1)
					.maxReplicas(1)
					.outputLabel("${workerManagerHost}")
					.serviceType(ServiceType.SYSTEM)
					.expectedMemoryConsumption(256901152d)
					.build();
				workerManager = servicesService.addService(workerManager);
			}
			ServiceEntity masterManager;
			try {
				masterManager = servicesService.getService(MasterManagerProperties.MASTER_MANAGER);
			}
			catch (EntityNotFoundException ignored) {
				masterManager = ServiceEntity.builder()
					.serviceName(MasterManagerProperties.MASTER_MANAGER)
					.dockerRepository(dockerHubUsername + "/manager-master")
					.defaultExternalPort("8080")
					.defaultInternalPort("8080")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.minReplicas(1)
					.maxReplicas(1)
					.outputLabel("${masterManagerHost}")
					.serviceType(ServiceType.SYSTEM)
					// TODO
					.expectedMemoryConsumption(256901152d)
					.build();
				masterManager = servicesService.addService(masterManager);
			}
			//apps
			AppEntity mixal;
			try {
				mixal = appsService.getApp("Mixal");
			}
			catch (EntityNotFoundException ignored) {
				mixal = AppEntity.builder()
					.name("Mixal")
					.build();
				mixal = appsService.addApp(mixal);
			}
			AppEntity sockShop;
			try {
				sockShop = appsService.getApp("Sock Shop");
			}
			catch (EntityNotFoundException ignored) {
				sockShop = AppEntity.builder()
					.name("Sock Shop")
					.build();
				sockShop = appsService.addApp(sockShop);
				appsServices.saveAll(List.of(
					AppServiceEntity.builder().app(sockShop).service(frontend).launchOrder(25).build(),
					AppServiceEntity.builder().app(sockShop).service(user).launchOrder(10).build(),
					AppServiceEntity.builder().app(sockShop).service(userDb).launchOrder(0).build(),
					AppServiceEntity.builder().app(sockShop).service(catalogue).launchOrder(5).build(),
					AppServiceEntity.builder().app(sockShop).service(catalogueDb).launchOrder(0).build(),
					AppServiceEntity.builder().app(sockShop).service(payment).launchOrder(5).build(),
					AppServiceEntity.builder().app(sockShop).service(carts).launchOrder(10).build(),
					AppServiceEntity.builder().app(sockShop).service(cartsDb).launchOrder(0).build(),
					AppServiceEntity.builder().app(sockShop).service(orders).launchOrder(20).build(),
					AppServiceEntity.builder().app(sockShop).service(ordersDb).launchOrder(0).build(),
					AppServiceEntity.builder().app(sockShop).service(shipping).launchOrder(15).build(),
					AppServiceEntity.builder().app(sockShop).service(queueMaster).launchOrder(15).build(),
					AppServiceEntity.builder().app(sockShop).service(rabbitmq).launchOrder(5).build()));
			}

			// service dependencies
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependencyEntity frontendRegistrationServerDependency = ServiceDependencyEntity.builder()
					.service(frontend)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(frontendRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), user.getServiceName())) {
				ServiceDependencyEntity frontendUserDependency = ServiceDependencyEntity.builder()
					.service(frontend)
					.dependency(user)
					.build();
				servicesDependencies.save(frontendUserDependency);
			}
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), catalogue.getServiceName())) {
				ServiceDependencyEntity frontendCatalogueDependency = ServiceDependencyEntity.builder()
					.service(frontend)
					.dependency(catalogue)
					.build();
				servicesDependencies.save(frontendCatalogueDependency);
			}
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), payment.getServiceName())) {
				ServiceDependencyEntity frontendPaymentDependency = ServiceDependencyEntity.builder()
					.service(frontend)
					.dependency(payment)
					.build();
				servicesDependencies.save(frontendPaymentDependency);
			}
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), carts.getServiceName())) {
				ServiceDependencyEntity frontendCartsDependency = ServiceDependencyEntity.builder()
					.service(frontend)
					.dependency(carts)
					.build();
				servicesDependencies.save(frontendCartsDependency);
			}
			if (!servicesDependencies.hasDependency(user.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependencyEntity userRegistrationServerDependency = ServiceDependencyEntity.builder()
					.service(user)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(userRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(user.getServiceName(), userDb.getServiceName())) {
				ServiceDependencyEntity userUserDbDependency = ServiceDependencyEntity.builder()
					.service(user)
					.dependency(userDb)
					.build();
				servicesDependencies.save(userUserDbDependency);
			}
			if (!servicesDependencies.hasDependency(catalogue.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependencyEntity catalogueRegistrationServerDependency = ServiceDependencyEntity.builder()
					.service(catalogue)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(catalogueRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(catalogue.getServiceName(), catalogueDb.getServiceName())) {
				ServiceDependencyEntity catalogueCatalogueDbDependency = ServiceDependencyEntity.builder()
					.service(catalogue)
					.dependency(catalogueDb)
					.build();
				servicesDependencies.save(catalogueCatalogueDbDependency);
			}
			if (!servicesDependencies.hasDependency(payment.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependencyEntity paymentRegistrationServerDependency = ServiceDependencyEntity.builder()
					.service(payment)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(paymentRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(carts.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependencyEntity cartsRegistrationServerDependency = ServiceDependencyEntity.builder()
					.service(carts)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(cartsRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(carts.getServiceName(), cartsDb.getServiceName())) {
				ServiceDependencyEntity cartsCartsDbDependency = ServiceDependencyEntity.builder()
					.service(carts)
					.dependency(cartsDb)
					.build();
				servicesDependencies.save(cartsCartsDbDependency);
			}
			if (!servicesDependencies.hasDependency(orders.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependencyEntity ordersRegistrationServerDependency = ServiceDependencyEntity.builder()
					.service(orders)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(ordersRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(orders.getServiceName(), payment.getServiceName())) {
				ServiceDependencyEntity ordersPaymentDependency = ServiceDependencyEntity.builder()
					.service(orders)
					.dependency(payment)
					.build();
				servicesDependencies.save(ordersPaymentDependency);
			}
			if (!servicesDependencies.hasDependency(orders.getServiceName(), shipping.getServiceName())) {
				ServiceDependencyEntity ordersShippingDependency = ServiceDependencyEntity.builder()
					.service(orders)
					.dependency(shipping)
					.build();
				servicesDependencies.save(ordersShippingDependency);
			}
			if (!servicesDependencies.hasDependency(orders.getServiceName(), ordersDb.getServiceName())) {
				ServiceDependencyEntity ordersOrdersDbDependency = ServiceDependencyEntity.builder()
					.service(orders)
					.dependency(ordersDb)
					.build();
				servicesDependencies.save(ordersOrdersDbDependency);
			}
			if (!servicesDependencies.hasDependency(shipping.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependencyEntity shippingRegistrationServerDependency = ServiceDependencyEntity.builder()
					.service(shipping)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(shippingRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(shipping.getServiceName(), rabbitmq.getServiceName())) {
				ServiceDependencyEntity shippingRabbitmqDependency = ServiceDependencyEntity.builder()
					.service(shipping)
					.dependency(rabbitmq)
					.build();
				servicesDependencies.save(shippingRabbitmqDependency);
			}
			if (!servicesDependencies.hasDependency(queueMaster.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependencyEntity queueMasterRegistrationServerDependency = ServiceDependencyEntity.builder()
					.service(queueMaster)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(queueMasterRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(queueMaster.getServiceName(), rabbitmq.getServiceName())) {
				ServiceDependencyEntity queueMasterRabbitmqDependency = ServiceDependencyEntity.builder()
					.service(queueMaster)
					.dependency(rabbitmq)
					.build();
				servicesDependencies.save(queueMasterRabbitmqDependency);
			}
			if (!servicesDependencies.hasDependency(rabbitmq.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependencyEntity rabbitmqRegistrationServerDependency = ServiceDependencyEntity.builder()
					.service(rabbitmq)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(rabbitmqRegistrationServerDependency);
			}

			/*// regions
			for (Region region : Region.values()) {
				if (!regionsService.hasRegion(region)) {
					RegionEntity regionEntity = RegionEntity.builder()
						.region(region)
						.active(true)
						.build();
					regionsService.addRegion(regionEntity);
				}
			}*/

			// edge hosts
			if (!edgeHostsService.hasEdgeHost("dpimenta.ddns.net")) {
				Coordinates coordinates = new Coordinates("Portugal", 39.575097, -8.909794);
				Region region = Region.getClosestRegion(coordinates);
				edgeHostsService.addEdgeHost(EdgeHostEntity.builder()
					.username("daniel")
					.publicIpAddress("2.82.208.89")
					.privateIpAddress("192.168.1.83")
					.publicDnsName("dpimenta.ddns.net")
					.region(region)
					.coordinates(coordinates)
					.build(), "ubuntusoldadox3");
			}

			// cloud hosts
			cloudHostsService.synchronizeDatabaseCloudHosts();

			// component types
			ComponentTypeEntity host;
			try {
				host = componentTypesService.getComponentType(ComponentType.HOST.name());
			}
			catch (EntityNotFoundException ignored) {
				host = ComponentTypeEntity.builder()
					.type(ComponentType.HOST)
					.build();
				host = componentTypesService.addComponentType(host);
			}
			ComponentTypeEntity service;
			try {
				service = componentTypesService.getComponentType(ComponentType.SERVICE.name());
			}
			catch (EntityNotFoundException ignored) {
				service = ComponentTypeEntity.builder()
					.type(ComponentType.SERVICE)
					.build();
				service = componentTypesService.addComponentType(service);
			}
			ComponentTypeEntity container;
			try {
				container = componentTypesService.getComponentType(ComponentType.CONTAINER.name());
			}
			catch (EntityNotFoundException ignored) {
				container = ComponentTypeEntity.builder()
					.type(ComponentType.CONTAINER)
					.build();
				container = componentTypesService.addComponentType(container);
			}

			// operator
			OperatorEntity notEqualTo;
			try {
				notEqualTo = operatorsService.getOperator(Operator.NOT_EQUAL_TO.name());
			}
			catch (EntityNotFoundException ignored) {
				notEqualTo = OperatorEntity.builder()
					.operator(Operator.NOT_EQUAL_TO)
					.symbol(Operator.NOT_EQUAL_TO.getSymbol())
					.build();
				notEqualTo = operatorsService.addOperator(notEqualTo);
			}
			OperatorEntity equalTo;
			try {
				equalTo = operatorsService.getOperator(Operator.EQUAL_TO.name());
			}
			catch (EntityNotFoundException ignored) {
				equalTo = OperatorEntity.builder()
					.operator(Operator.EQUAL_TO)
					.symbol(Operator.EQUAL_TO.getSymbol())
					.build();
				equalTo = operatorsService.addOperator(equalTo);
			}
			OperatorEntity greaterThan;
			try {
				greaterThan = operatorsService.getOperator(Operator.GREATER_THAN.name());
			}
			catch (EntityNotFoundException ignored) {
				greaterThan = OperatorEntity.builder()
					.operator(Operator.GREATER_THAN)
					.symbol(Operator.GREATER_THAN.getSymbol())
					.build();
				greaterThan = operatorsService.addOperator(greaterThan);
			}
			OperatorEntity lessThan;
			try {
				lessThan = operatorsService.getOperator(Operator.LESS_THAN.name());
			}
			catch (EntityNotFoundException ignored) {
				lessThan = OperatorEntity.builder()
					.operator(Operator.LESS_THAN)
					.symbol(Operator.LESS_THAN.getSymbol())
					.build();
				lessThan = operatorsService.addOperator(lessThan);
			}
			OperatorEntity greaterThanOrEqualTo;
			try {
				greaterThanOrEqualTo = operatorsService.getOperator(Operator.GREATER_THAN_OR_EQUAL_TO.name());
			}
			catch (EntityNotFoundException ignored) {
				greaterThanOrEqualTo = OperatorEntity.builder()
					.operator(Operator.GREATER_THAN_OR_EQUAL_TO)
					.symbol(Operator.GREATER_THAN_OR_EQUAL_TO.getSymbol())
					.build();
				greaterThanOrEqualTo = operatorsService.addOperator(greaterThanOrEqualTo);
			}
			OperatorEntity lessThanOrEqualTo;
			try {
				lessThanOrEqualTo = operatorsService.getOperator(Operator.LESS_THAN_OR_EQUAL_TO.name());
			}
			catch (EntityNotFoundException ignored) {
				lessThanOrEqualTo = OperatorEntity.builder()
					.operator(Operator.LESS_THAN_OR_EQUAL_TO)
					.symbol(Operator.LESS_THAN_OR_EQUAL_TO.getSymbol())
					.build();
				lessThanOrEqualTo = operatorsService.addOperator(lessThanOrEqualTo);
			}

			// service decisions
			DecisionEntity serviceDecisionNone;
			try {
				serviceDecisionNone = decisionsService.getServicePossibleDecision(RuleDecision.NONE.name());
			}
			catch (EntityNotFoundException ignored) {
				serviceDecisionNone = DecisionEntity.builder()
					.componentType(service)
					.ruleDecision(RuleDecision.NONE)
					.build();
				serviceDecisionNone = decisionsService.addDecision(serviceDecisionNone);
			}
			DecisionEntity serviceDecisionReplicate;
			try {
				serviceDecisionReplicate = decisionsService.getServicePossibleDecision(RuleDecision.REPLICATE.name());
			}
			catch (EntityNotFoundException ignored) {
				serviceDecisionReplicate = DecisionEntity.builder()
					.componentType(service)
					.ruleDecision(RuleDecision.REPLICATE)
					.build();
				serviceDecisionReplicate = decisionsService.addDecision(serviceDecisionReplicate);
			}
			DecisionEntity serviceDecisionMigrate;
			try {
				serviceDecisionMigrate = decisionsService.getServicePossibleDecision(RuleDecision.MIGRATE.name());
			}
			catch (EntityNotFoundException ignored) {
				serviceDecisionMigrate = DecisionEntity.builder()
					.componentType(service)
					.ruleDecision(RuleDecision.MIGRATE)
					.build();
				serviceDecisionMigrate = decisionsService.addDecision(serviceDecisionMigrate);
			}
			DecisionEntity serviceDecisionStop;
			try {
				serviceDecisionStop = decisionsService.getServicePossibleDecision(RuleDecision.STOP.name());
			}
			catch (EntityNotFoundException ignored) {
				serviceDecisionStop = DecisionEntity.builder()
					.componentType(service)
					.ruleDecision(RuleDecision.STOP)
					.build();
				serviceDecisionStop = decisionsService.addDecision(serviceDecisionStop);
			}
			// host decisions
			DecisionEntity hostDecisionNone;
			try {
				hostDecisionNone = decisionsService.getHostPossibleDecision(RuleDecision.NONE.name());
			}
			catch (EntityNotFoundException ignored) {
				hostDecisionNone = DecisionEntity.builder()
					.componentType(host)
					.ruleDecision(RuleDecision.NONE)
					.build();
				hostDecisionNone = decisionsService.addDecision(hostDecisionNone);
			}
			DecisionEntity hostDecisionStart;
			try {
				hostDecisionStart = decisionsService.getHostPossibleDecision(RuleDecision.OVERWORK.name());
			}
			catch (EntityNotFoundException ignored) {
				hostDecisionStart = DecisionEntity.builder()
					.componentType(host)
					.ruleDecision(RuleDecision.OVERWORK)
					.build();
				hostDecisionStart = decisionsService.addDecision(hostDecisionStart);
			}
			DecisionEntity hostDecisionUnderwork;
			try {
				hostDecisionUnderwork = decisionsService.getHostPossibleDecision(RuleDecision.UNDERWORK.name());
			}
			catch (EntityNotFoundException ignored) {
				hostDecisionUnderwork = DecisionEntity.builder()
					.componentType(host)
					.ruleDecision(RuleDecision.UNDERWORK)
					.build();
				hostDecisionUnderwork = decisionsService.addDecision(hostDecisionUnderwork);
			}

			// fields
			// TODO is missing more fields...
			FieldEntity cpu;
			try {
				cpu = fieldsService.getField("cpu");
			}
			catch (EntityNotFoundException ignored) {
				cpu = FieldEntity.builder()
					.name("cpu")
					.build();
				cpu = fieldsService.addField(cpu);
			}
			FieldEntity ram;
			try {
				ram = fieldsService.getField("ram");
			}
			catch (EntityNotFoundException ignored) {
				ram = FieldEntity.builder()
					.name("ram")
					.query(PrometheusQuery.MEMORY_USAGE)
					.build();
				ram = fieldsService.addField(ram);
			}
			FieldEntity cpuPercentage;
			try {
				cpuPercentage = fieldsService.getField("cpu-%");
			}
			catch (EntityNotFoundException ignored) {
				cpuPercentage = FieldEntity.builder()
					.name("cpu-%")
					.query(PrometheusQuery.CPU_USAGE_PERCENTAGE)
					.build();
				cpuPercentage = fieldsService.addField(cpuPercentage);
			}
			FieldEntity ramPercentage;
			try {
				ramPercentage = fieldsService.getField("ram-%");
			}
			catch (EntityNotFoundException ignored) {
				ramPercentage = FieldEntity.builder()
					.name("ram-%")
					.query(PrometheusQuery.MEMORY_USAGE_PERCENTAGE)
					.build();
				ramPercentage = fieldsService.addField(ramPercentage);
			}
			FieldEntity rxBytes;
			try {
				rxBytes = fieldsService.getField("rx-bytes");
			}
			catch (EntityNotFoundException ignored) {
				rxBytes = FieldEntity.builder()
					.name("rx-bytes")
					.build();
				rxBytes = fieldsService.addField(rxBytes);
			}
			FieldEntity txBytes;
			try {
				txBytes = fieldsService.getField("tx-bytes");
			}
			catch (EntityNotFoundException ignored) {
				txBytes = FieldEntity.builder()
					.name("tx-bytes")
					.build();
				txBytes = fieldsService.addField(txBytes);
			}
			FieldEntity rxBytesPerSec;
			try {
				rxBytesPerSec = fieldsService.getField("rx-bytes-per-sec");
			}
			catch (EntityNotFoundException ignored) {
				rxBytesPerSec = FieldEntity.builder()
					.name("rx-bytes-per-sec")
					.build();
				rxBytesPerSec = fieldsService.addField(rxBytesPerSec);
			}
			FieldEntity txBytesPerSec;
			try {
				txBytesPerSec = fieldsService.getField("tx-bytes-per-sec");
			}
			catch (EntityNotFoundException ignored) {
				txBytesPerSec = FieldEntity.builder()
					.name("tx-bytes-per-sec")
					.build();
				txBytesPerSec = fieldsService.addField(txBytesPerSec);
			}
			FieldEntity latency;
			try {
				latency = fieldsService.getField("latency");
			}
			catch (EntityNotFoundException ignored) {
				latency = FieldEntity.builder()
					.name("latency")
					.build();
				latency = fieldsService.addField(latency);
			}
			FieldEntity bandwidthPercentage;
			try {
				bandwidthPercentage = fieldsService.getField("bandwidth-%");
			}
			catch (EntityNotFoundException ignored) {
				bandwidthPercentage = FieldEntity.builder()
					.name("bandwidth-%")
					.build();
				bandwidthPercentage = fieldsService.addField(bandwidthPercentage);
			}
			FieldEntity filesystemAvailableSpace;
			try {
				filesystemAvailableSpace = fieldsService.getField("filesystem-available-space");
			}
			catch (EntityNotFoundException ignored) {
				latency = FieldEntity.builder()
					.name("filesystem-available-space")
					.query(PrometheusQuery.FILESYSTEM_AVAILABLE_SPACE)
					.build();
				filesystemAvailableSpace = fieldsService.addField(latency);
			}


			// value modes
			ValueModeEntity effectiveValue;
			try {
				effectiveValue = valueModesService.getValueMode("effective-val");
			}
			catch (EntityNotFoundException ignored) {
				effectiveValue = ValueModeEntity.builder()
					.name("effective-val")
					.build();
				effectiveValue = valueModesService.addValueMode(effectiveValue);
			}
			ValueModeEntity averageValue;
			try {
				averageValue = valueModesService.getValueMode("avg-val");
			}
			catch (EntityNotFoundException ignored) {
				averageValue = ValueModeEntity.builder()
					.name("avg-val")
					.build();
				averageValue = valueModesService.addValueMode(averageValue);
			}
			ValueModeEntity deviationPercentageOnAverageValue;
			try {
				deviationPercentageOnAverageValue = valueModesService.getValueMode("deviation-%-on-avg-val");
			}
			catch (EntityNotFoundException ignored) {
				deviationPercentageOnAverageValue = ValueModeEntity.builder()
					.name("deviation-%-on-avg-val")
					.build();
				deviationPercentageOnAverageValue = valueModesService.addValueMode(deviationPercentageOnAverageValue);
			}
			ValueModeEntity deviationPercentageOnLastValue;
			try {
				deviationPercentageOnLastValue = valueModesService.getValueMode("deviation-%-on-last-val");
			}
			catch (EntityNotFoundException ignored) {
				deviationPercentageOnLastValue = ValueModeEntity.builder()
					.name("deviation-%-on-last-val")
					.build();
				deviationPercentageOnLastValue = valueModesService.addValueMode(deviationPercentageOnLastValue);
			}

			// conditions
			ConditionEntity cpuPercentageOver90;
			try {
				cpuPercentageOver90 = conditionsService.getCondition("CpuPercentageOver90");
			}
			catch (EntityNotFoundException ignored) {
				cpuPercentageOver90 = ConditionEntity.builder()
					.name("CpuPercentageOver90")
					.valueMode(effectiveValue)
					.field(cpuPercentage)
					.operator(greaterThan)
					.value(90)
					.build();
				cpuPercentageOver90 = conditionsService.addCondition(cpuPercentageOver90);
			}
			ConditionEntity ramPercentageOver90;
			try {
				ramPercentageOver90 = conditionsService.getCondition("RamPercentageOver90");
			}
			catch (EntityNotFoundException ignored) {
				ramPercentageOver90 = ConditionEntity.builder()
					.name("RamPercentageOver90")
					.valueMode(effectiveValue)
					.field(ramPercentage)
					.operator(greaterThan)
					.value(90)
					.build();
				ramPercentageOver90 = conditionsService.addCondition(ramPercentageOver90);
			}
			ConditionEntity rxBytesPerSecOver500000;
			try {
				rxBytesPerSecOver500000 = conditionsService.getCondition("RxBytesPerSecOver500000");
			}
			catch (EntityNotFoundException ignored) {
				rxBytesPerSecOver500000 = ConditionEntity.builder()
					.name("RxBytesPerSecOver500000")
					.valueMode(effectiveValue)
					.field(rxBytesPerSec)
					.operator(greaterThan)
					.value(500000)
					.build();
				rxBytesPerSecOver500000 = conditionsService.addCondition(rxBytesPerSecOver500000);
			}
			ConditionEntity txBytesPerSecOver100000;
			try {
				txBytesPerSecOver100000 = conditionsService.getCondition("TxBytesPerSecOver100000");
			}
			catch (EntityNotFoundException ignored) {
				txBytesPerSecOver100000 = ConditionEntity.builder()
					.name("TxBytesPerSecOver100000")
					.valueMode(effectiveValue)
					.field(txBytesPerSec)
					.operator(greaterThan)
					.value(100000)
					.build();
				txBytesPerSecOver100000 = conditionsService.addCondition(txBytesPerSecOver100000);
			}

			// generic host rules
			HostRuleEntity cpuAndRamOver90GenericHostRule;
			try {
				cpuAndRamOver90GenericHostRule = hostRulesService.getRule("CpuAndRamOver90");
			}
			catch (EntityNotFoundException ignored) {
				cpuAndRamOver90GenericHostRule = HostRuleEntity.builder()
					.name("CpuAndRamOver90")
					.priority(1)
					.decision(hostDecisionStart)
					.generic(true)
					.build();
				cpuAndRamOver90GenericHostRule = hostRulesService.addRule(cpuAndRamOver90GenericHostRule);

				HostRuleConditionEntity cpuOver90Condition = HostRuleConditionEntity.builder()
					.hostRule(cpuAndRamOver90GenericHostRule)
					.hostCondition(cpuPercentageOver90)
					.build();
				hostRuleConditions.save(cpuOver90Condition);
				HostRuleConditionEntity ramOver90Condition = HostRuleConditionEntity.builder()
					.hostRule(cpuAndRamOver90GenericHostRule)
					.hostCondition(ramPercentageOver90)
					.build();
				hostRuleConditions.save(ramOver90Condition);
			}
			// generic service rules
			ServiceRuleEntity rxOver500000GenericServiceRule;
			try {
				rxOver500000GenericServiceRule = serviceRulesService.getRule("RxOver500000");
			}
			catch (EntityNotFoundException ignored) {
				rxOver500000GenericServiceRule = ServiceRuleEntity.builder()
					.name("RxOver500000")
					.priority(1)
					.decision(serviceDecisionReplicate)
					.generic(true)
					.build();
				rxOver500000GenericServiceRule = serviceRulesService.addRule(rxOver500000GenericServiceRule);
				ServiceRuleConditionEntity rxOver500000Condition = ServiceRuleConditionEntity.builder()
					.serviceRule(rxOver500000GenericServiceRule)
					.serviceCondition(rxBytesPerSecOver500000)
					.build();
				serviceRuleConditions.save(rxOver500000Condition);
			}

			hostsEventsService.reset();
			servicesEventsService.reset();

			hostsMonitoringService.reset();
			servicesMonitoringService.reset();
		};
	}

}
