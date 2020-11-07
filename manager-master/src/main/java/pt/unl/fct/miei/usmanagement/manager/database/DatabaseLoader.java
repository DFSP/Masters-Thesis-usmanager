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
import pt.unl.fct.miei.usmanagement.manager.apps.App;
import pt.unl.fct.miei.usmanagement.manager.apps.AppService;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentType;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependency;
import pt.unl.fct.miei.usmanagement.manager.fields.Field;
import pt.unl.fct.miei.usmanagement.manager.hosts.edge.EdgeHost;
import pt.unl.fct.miei.usmanagement.manager.metrics.PrometheusQueryEnum;
import pt.unl.fct.miei.usmanagement.manager.operators.Operator;
import pt.unl.fct.miei.usmanagement.manager.regions.RegionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.condition.Condition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.RuleDecisionEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRule;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleCondition;
import pt.unl.fct.miei.usmanagement.manager.services.Service;
import pt.unl.fct.miei.usmanagement.manager.services.ServiceTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.apps.AppServices;
import pt.unl.fct.miei.usmanagement.manager.componenttypes.ComponentTypeEnum;
import pt.unl.fct.miei.usmanagement.manager.dependencies.ServiceDependencies;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;
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
import pt.unl.fct.miei.usmanagement.manager.operators.OperatorEnum;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.HostRuleConditions;
import pt.unl.fct.miei.usmanagement.manager.rulesystem.rules.ServiceRuleConditions;
import pt.unl.fct.miei.usmanagement.manager.users.User;
import pt.unl.fct.miei.usmanagement.manager.users.UserRoleEnum;
import pt.unl.fct.miei.usmanagement.manager.users.UsersService;
import pt.unl.fct.miei.usmanagement.manager.valuemodes.ValueMode;

import java.util.List;

@Slf4j
@Component
public class DatabaseLoader {

	@Bean
	CommandLineRunner initDatabase(UsersService usersService,
								   ServicesService servicesService,
								   AppsService appsService, AppServices appsServices,
								   ServiceDependencies servicesDependencies, /*RegionsService regionsService,*/
								   EdgeHostsService edgeHostsService, CloudHostsService cloudHostsService,
								   ComponentTypesService componentTypesService,
								   OperatorsService operatorsService, DecisionsService decisionsService,
								   FieldsService fieldsService, ValueModesService valueModesService,
								   ConditionsService conditionsService, HostRulesService hostRulesService,
								   HostRuleConditions hostRuleConditions,
								   ServiceRulesService serviceRulesService,
								   ServiceRuleConditions serviceRuleConditions,
								   DockerProperties dockerProperties, HostsEventsService hostsEventsService,
								   ServicesEventsService servicesEventsService, HostsMonitoringService hostsMonitoringService,
								   ServicesMonitoringService servicesMonitoringService) {
		return args -> {

			String dockerHubUsername = dockerProperties.getHub().getUsername();

			// users
			if (!usersService.hasUser("admin")) {
				User sysAdmin = User.builder()
					.firstName("admin")
					.lastName("admin")
					.username("admin")
					.password("admin")
					.email("admin@admin.pt")
					.role(UserRoleEnum.ROLE_SYS_ADMIN)
					.build();
				usersService.addUser(sysAdmin);
			}
			if (!usersService.hasUser("danielfct")) {
				User sysAdmin = User.builder()
					.firstName("daniel")
					.lastName("pimenta")
					.username("danielfct")
					.password("danielfct")
					.email("d.pimenta@campus.fct.unl.pt")
					.role(UserRoleEnum.ROLE_SYS_ADMIN)
					.build();
				usersService.addUser(sysAdmin);
			}

			// services

			Service frontend;
			try {
				frontend = servicesService.getService("sock-shop-front-end");
			}
			catch (EntityNotFoundException ignored) {
				frontend = Service.builder()
					.serviceName("sock-shop-front-end")
					.dockerRepository(dockerHubUsername + "/sock-shop-front-end")
					.defaultExternalPort(8081)
					.defaultInternalPort(80)
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.minimumReplicas(1)
					.outputLabel("${front-endHost}")
					.serviceType(ServiceTypeEnum.FRONTEND)
					.expectedMemoryConsumption(209715200d)
					.build();
				frontend = servicesService.addService(frontend);
			}
			Service user;
			try {
				user = servicesService.getService("sock-shop-user");
			}
			catch (EntityNotFoundException ignored) {
				user = Service.builder()
					.serviceName("sock-shop-user")
					.dockerRepository(dockerHubUsername + "/sock-shop-user")
					.defaultExternalPort(8082)
					.defaultInternalPort(80)
					.defaultDb("user-db:27017")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${userDatabaseHost}")
					.minimumReplicas(1)
					.outputLabel("${userHost}")
					.serviceType(ServiceTypeEnum.BACKEND)
					.expectedMemoryConsumption(62914560d)
					.build();
				user = servicesService.addService(user);
			}
			Service userDb;
			try {
				userDb = servicesService.getService("sock-shop-user-db");
			}
			catch (EntityNotFoundException ignored) {
				userDb = Service.builder()
					.serviceName("sock-shop-user-db")
					.dockerRepository(dockerHubUsername + "/sock-shop-user-db")
					.defaultExternalPort(27017)
					.defaultInternalPort(27017)
					.minimumReplicas(1)
					.outputLabel("${userDatabaseHost}")
					.serviceType(ServiceTypeEnum.DATABASE)
					.expectedMemoryConsumption(262144000d)
					.build();
				userDb = servicesService.addService(userDb);
			}
			Service catalogue;
			try {
				catalogue = servicesService.getService("sock-shop-catalogue");
			}
			catch (EntityNotFoundException ignored) {
				catalogue = Service.builder()
					.serviceName("sock-shop-catalogue")
					.dockerRepository(dockerHubUsername + "/sock-shop-catalogue")
					.defaultExternalPort(8083)
					.defaultInternalPort(80)
					.defaultDb("catalogue-db:3306")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${catalogueDatabaseHost}")
					.minimumReplicas(1)
					.outputLabel("${catalogueHost}")
					.serviceType(ServiceTypeEnum.BACKEND)
					.expectedMemoryConsumption(62914560d)
					.build();
				catalogue = servicesService.addService(catalogue);
			}
			Service catalogueDb;
			try {
				catalogueDb = servicesService.getService("sock-shop-catalogue-db");
			}
			catch (EntityNotFoundException ignored) {
				catalogueDb = Service.builder()
					.serviceName("sock-shop-catalogue-db")
					.dockerRepository(dockerHubUsername + "/sock-shop-catalogue-db")
					.defaultExternalPort(3306)
					.defaultInternalPort(3306)
					.minimumReplicas(1)
					.outputLabel("${catalogueDatabaseHost}")
					.serviceType(ServiceTypeEnum.DATABASE)
					.expectedMemoryConsumption(262144000d)
					.build();
				catalogueDb = servicesService.addService(catalogueDb);
			}
			Service payment;
			try {
				payment = servicesService.getService("sock-shop-payment");
			}
			catch (EntityNotFoundException ignored) {
				payment = Service.builder()
					.serviceName("sock-shop-payment")
					.dockerRepository(dockerHubUsername + "/sock-shop-payment")
					.defaultExternalPort(8084)
					.defaultInternalPort(80)
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.minimumReplicas(1)
					.outputLabel("${paymentHost}")
					.serviceType(ServiceTypeEnum.BACKEND)
					.expectedMemoryConsumption(62914560d)
					.build();
				payment = servicesService.addService(payment);
			}
			Service carts;
			try {
				carts = servicesService.getService("sock-shop-carts");
			}
			catch (EntityNotFoundException ignored) {
				carts = Service.builder()
					.serviceName("sock-shop-carts")
					.dockerRepository(dockerHubUsername + "/sock-shop-carts")
					.defaultExternalPort(8085)
					.defaultInternalPort(80)
					.defaultDb("carts-db:27017")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${cartsDatabaseHost}")
					.minimumReplicas(1)
					.outputLabel("${cartsHost}")
					.serviceType(ServiceTypeEnum.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				carts = servicesService.addService(carts);
			}
			Service cartsDb;
			try {
				cartsDb = servicesService.getService("sock-shop-carts-db");
			}
			catch (EntityNotFoundException ignored) {
				cartsDb = Service.builder()
					.serviceName("sock-shop-carts-db")
					.dockerRepository(dockerHubUsername + "/sock-shop-carts-db")
					.defaultExternalPort(27016)
					.defaultInternalPort(27017)
					.minimumReplicas(1)
					.outputLabel("${cartsDatabaseHost}")
					.serviceType(ServiceTypeEnum.DATABASE)
					.expectedMemoryConsumption(262144000d)
					.build();
				cartsDb = servicesService.addService(cartsDb);
			}
			Service orders;
			try {
				orders = servicesService.getService("sock-shop-orders");
			}
			catch (EntityNotFoundException ignored) {
				orders = Service.builder()
					.serviceName("sock-shop-orders")
					.dockerRepository(dockerHubUsername + "/sock-shop-orders")
					.defaultExternalPort(8086)
					.defaultInternalPort(80)
					.defaultDb("orders-db:27017")
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${ordersDatabaseHost}")
					.minimumReplicas(1)
					.outputLabel("${ordersHost}")
					.serviceType(ServiceTypeEnum.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				orders = servicesService.addService(orders);
			}
			Service ordersDb;
			try {
				ordersDb = servicesService.getService("sock-shop-orders-db");
			}
			catch (EntityNotFoundException ignored) {
				ordersDb = Service.builder()
					.serviceName("sock-shop-orders-db")
					.dockerRepository(dockerHubUsername + "/sock-shop-orders-db")
					.defaultExternalPort(27015)
					.defaultInternalPort(27017)
					.minimumReplicas(1)
					.outputLabel("${ordersDatabaseHost}")
					.serviceType(ServiceTypeEnum.DATABASE)
					.expectedMemoryConsumption(262144000d)
					.build();
				ordersDb = servicesService.addService(ordersDb);
			}
			Service shipping;
			try {
				shipping = servicesService.getService("sock-shop-shipping");
			}
			catch (EntityNotFoundException ignored) {
				shipping = Service.builder()
					.serviceName("sock-shop-shipping")
					.dockerRepository(dockerHubUsername + "/sock-shop-shipping")
					.defaultExternalPort(8087)
					.defaultInternalPort(80)
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${rabbitmqHost}")
					.minimumReplicas(1)
					.outputLabel("${shippingHost}")
					.serviceType(ServiceTypeEnum.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				shipping = servicesService.addService(shipping);
			}
			Service queueMaster;
			try {
				queueMaster = servicesService.getService("sock-shop-queue-master");
			}
			catch (EntityNotFoundException ignored) {
				queueMaster = Service.builder()
					.serviceName("sock-shop-queue-master")
					.dockerRepository(dockerHubUsername + "/sock-shop-queue-master")
					.defaultExternalPort(8088)
					.defaultInternalPort(80)
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname} ${rabbitmqHost}")
					.minimumReplicas(1)
					.outputLabel("${queue-masterHost}")
					.serviceType(ServiceTypeEnum.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				queueMaster = servicesService.addService(queueMaster);
			}
			Service rabbitmq;
			try {
				rabbitmq = servicesService.getService("sock-shop-rabbitmq");
			}
			catch (EntityNotFoundException ignored) {
				rabbitmq = Service.builder()
					.serviceName("sock-shop-rabbitmq")
					.dockerRepository(dockerHubUsername + "/sock-shop-rabbitmq")
					.defaultExternalPort(5672)
					.defaultInternalPort(5672)
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.minimumReplicas(1)
					.maximumReplicas(1)
					.outputLabel("${rabbitmqHost}")
					.serviceType(ServiceTypeEnum.BACKEND)
					.expectedMemoryConsumption(262144000d)
					.build();
				rabbitmq = servicesService.addService(rabbitmq);
			}

			Service crashTesting;
			try {
				crashTesting = servicesService.getService("crash-testing");
			}
			catch (EntityNotFoundException ignored) {
				crashTesting = Service.builder()
					.serviceName("crash-testing")
					.dockerRepository(dockerHubUsername + "/crash-testing")
					.defaultExternalPort(2500)
					.defaultInternalPort(80)
					.minimumReplicas(1)
					.serviceType(ServiceTypeEnum.SYSTEM)
					.build();
				crashTesting = servicesService.addService(crashTesting);
			}
			Service loadBalancer;
			try {
				loadBalancer = servicesService.getService(NginxLoadBalancerService.LOAD_BALANCER);
			}
			catch (EntityNotFoundException ignored) {
				loadBalancer = Service.builder()
					.serviceName(NginxLoadBalancerService.LOAD_BALANCER)
					.dockerRepository(dockerHubUsername + "/nginx-load-balancer")
					.defaultExternalPort(1906)
					.defaultInternalPort(80)
					.minimumReplicas(1)
					.outputLabel("${loadBalancerHost}")
					.serviceType(ServiceTypeEnum.SYSTEM)
					.expectedMemoryConsumption(10485760d)
					.build();
				loadBalancer = servicesService.addService(loadBalancer);
			}
			Service requestLocationMonitor;
			try {
				requestLocationMonitor = servicesService.getService(LocationRequestsService.REQUEST_LOCATION_MONITOR);
			}
			catch (EntityNotFoundException ignored) {
				requestLocationMonitor = Service.builder()
					.serviceName(LocationRequestsService.REQUEST_LOCATION_MONITOR)
					.dockerRepository(dockerHubUsername + "/request-location-monitor")
					.defaultExternalPort(1919)
					.defaultInternalPort(1919)
					.minimumReplicas(1)
					.outputLabel("${requestLocationMonitorHost}")
					.serviceType(ServiceTypeEnum.SYSTEM)
					.expectedMemoryConsumption(52428800d)
					.build();
				requestLocationMonitor = servicesService.addService(requestLocationMonitor);
			}
			Service registrationServer;
			try {
				registrationServer = servicesService.getService(RegistrationServerService.REGISTRATION_SERVER);
			}
			catch (EntityNotFoundException ignored) {
				registrationServer = Service.builder()
					.serviceName(RegistrationServerService.REGISTRATION_SERVER)
					.dockerRepository(dockerHubUsername + "/registration-server")
					.defaultExternalPort(8761)
					.defaultInternalPort(8761)
					.launchCommand("${externalPort} ${internalPort} ${hostname} ${zone}")
					.minimumReplicas(1)
					.outputLabel("${registrationHost}")
					.serviceType(ServiceTypeEnum.SYSTEM)
					.expectedMemoryConsumption(262144000d)
					.build();
				registrationServer = servicesService.addService(registrationServer);
			}
			Service prometheus;
			try {
				prometheus = servicesService.getService(PrometheusService.PROMETHEUS);
			}
			catch (EntityNotFoundException ignored) {
				prometheus = Service.builder()
					.serviceName(PrometheusService.PROMETHEUS)
					.dockerRepository(dockerHubUsername + "/prometheus")
					.defaultExternalPort(9090)
					.defaultInternalPort(9090)
					.minimumReplicas(1)
					.outputLabel("${prometheusHost}")
					.serviceType(ServiceTypeEnum.SYSTEM)
					.expectedMemoryConsumption(52428800d)
					.build();
				prometheus = servicesService.addService(prometheus);
			}
			Service dockerApiProxy;
			try {
				dockerApiProxy = servicesService.getService(DockerApiProxyService.DOCKER_API_PROXY);
			}
			catch (EntityNotFoundException ignored) {
				dockerApiProxy = Service.builder()
					.serviceName(DockerApiProxyService.DOCKER_API_PROXY)
					.dockerRepository(dockerHubUsername + "/nginx-basic-auth-proxy")
					.defaultExternalPort(dockerProperties.getApiProxy().getPort())
					.defaultInternalPort(80)
					.minimumReplicas(1)
					.outputLabel("${dockerApiProxyHost}")
					.serviceType(ServiceTypeEnum.SYSTEM)
					.expectedMemoryConsumption(10485760d)
					.build();
				dockerApiProxy = servicesService.addService(dockerApiProxy);
			}
			Service workerManager;
			try {
				workerManager = servicesService.getService(WorkerManagerProperties.WORKER_MANAGER);
			}
			catch (EntityNotFoundException ignored) {
				workerManager = Service.builder()
					.serviceName(WorkerManagerProperties.WORKER_MANAGER)
					.dockerRepository(dockerHubUsername + "/manager-worker")
					.defaultExternalPort(8081)
					.defaultInternalPort(8081)
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.outputLabel("${workerManagerHost}")
					.serviceType(ServiceTypeEnum.SYSTEM)
					.expectedMemoryConsumption(256901152d)
					.build();
				workerManager = servicesService.addService(workerManager);
			}
			Service masterManager;
			try {
				masterManager = servicesService.getService(MasterManagerProperties.MASTER_MANAGER);
			}
			catch (EntityNotFoundException ignored) {
				masterManager = Service.builder()
					.serviceName(MasterManagerProperties.MASTER_MANAGER)
					.dockerRepository(dockerHubUsername + "/manager-master")
					.defaultExternalPort(8080)
					.defaultInternalPort(8080)
					.launchCommand("${registrationHost} ${externalPort} ${internalPort} ${hostname}")
					.minimumReplicas(1)
					.maximumReplicas(1)
					.outputLabel("${masterManagerHost}")
					.serviceType(ServiceTypeEnum.SYSTEM)
					// TODO
					.expectedMemoryConsumption(256901152d)
					.build();
				masterManager = servicesService.addService(masterManager);
			}

			// apps
			App testing;
			try {
				testing = appsService.getApp("Test suite");
			}
			catch (EntityNotFoundException ignored) {
				testing = App.builder()
					.name("Test suite")
					.description("Microservices designed to test components of the system.")
					.build();
				testing = appsService.addApp(testing);
				appsServices.saveAll(List.of(
					AppService.builder().app(testing).service(crashTesting).build()));
			}
			App mixal;
			try {
				mixal = appsService.getApp("Mixal");
			}
			catch (EntityNotFoundException ignored) {
				mixal = App.builder()
					.name("Mixal")
					.build();
				mixal = appsService.addApp(mixal);
			}
			App onlineBoutique;
			try {
				onlineBoutique = appsService.getApp("Online Boutique");
			}
			catch (EntityNotFoundException ignored) {
				mixal = App.builder()
					.name("Online Boutique")
					.description("Online Boutique is a cloud-native microservices demo application. " +
						"Online Boutique consists of a 10-tier microservices application. The application is a web-based " +
						"e-commerce app where users can browse items, add them to the cart, and purchase them.")
					.build();
				onlineBoutique = appsService.addApp(mixal);
			}
			App sockShop;
			try {
				sockShop = appsService.getApp("Sock Shop");
			}
			catch (EntityNotFoundException ignored) {
				sockShop = App.builder()
					.name("Sock Shop")
					.description("Sock Shop simulates the user-facing part of an e-commerce website that sells socks. " +
						"It is intended to aid the demonstration and testing of microservice and cloud native technologies.")
					.build();
				sockShop = appsService.addApp(sockShop);
				appsServices.saveAll(List.of(
					AppService.builder().app(sockShop).service(frontend).launchOrder(25).build(),
					AppService.builder().app(sockShop).service(user).launchOrder(10).build(),
					AppService.builder().app(sockShop).service(userDb).launchOrder(0).build(),
					AppService.builder().app(sockShop).service(catalogue).launchOrder(5).build(),
					AppService.builder().app(sockShop).service(catalogueDb).launchOrder(0).build(),
					AppService.builder().app(sockShop).service(payment).launchOrder(5).build(),
					AppService.builder().app(sockShop).service(carts).launchOrder(10).build(),
					AppService.builder().app(sockShop).service(cartsDb).launchOrder(0).build(),
					AppService.builder().app(sockShop).service(orders).launchOrder(20).build(),
					AppService.builder().app(sockShop).service(ordersDb).launchOrder(0).build(),
					AppService.builder().app(sockShop).service(shipping).launchOrder(15).build(),
					AppService.builder().app(sockShop).service(queueMaster).launchOrder(15).build(),
					AppService.builder().app(sockShop).service(rabbitmq).launchOrder(5).build()));
			}

			// service dependencies
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependency frontendRegistrationServerDependency = ServiceDependency.builder()
					.service(frontend)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(frontendRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), user.getServiceName())) {
				ServiceDependency frontendUserDependency = ServiceDependency.builder()
					.service(frontend)
					.dependency(user)
					.build();
				servicesDependencies.save(frontendUserDependency);
			}
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), catalogue.getServiceName())) {
				ServiceDependency frontendCatalogueDependency = ServiceDependency.builder()
					.service(frontend)
					.dependency(catalogue)
					.build();
				servicesDependencies.save(frontendCatalogueDependency);
			}
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), payment.getServiceName())) {
				ServiceDependency frontendPaymentDependency = ServiceDependency.builder()
					.service(frontend)
					.dependency(payment)
					.build();
				servicesDependencies.save(frontendPaymentDependency);
			}
			if (!servicesDependencies.hasDependency(frontend.getServiceName(), carts.getServiceName())) {
				ServiceDependency frontendCartsDependency = ServiceDependency.builder()
					.service(frontend)
					.dependency(carts)
					.build();
				servicesDependencies.save(frontendCartsDependency);
			}
			if (!servicesDependencies.hasDependency(user.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependency userRegistrationServerDependency = ServiceDependency.builder()
					.service(user)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(userRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(user.getServiceName(), userDb.getServiceName())) {
				ServiceDependency userUserDbDependency = ServiceDependency.builder()
					.service(user)
					.dependency(userDb)
					.build();
				servicesDependencies.save(userUserDbDependency);
			}
			if (!servicesDependencies.hasDependency(catalogue.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependency catalogueRegistrationServerDependency = ServiceDependency.builder()
					.service(catalogue)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(catalogueRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(catalogue.getServiceName(), catalogueDb.getServiceName())) {
				ServiceDependency catalogueCatalogueDbDependency = ServiceDependency.builder()
					.service(catalogue)
					.dependency(catalogueDb)
					.build();
				servicesDependencies.save(catalogueCatalogueDbDependency);
			}
			if (!servicesDependencies.hasDependency(payment.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependency paymentRegistrationServerDependency = ServiceDependency.builder()
					.service(payment)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(paymentRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(carts.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependency cartsRegistrationServerDependency = ServiceDependency.builder()
					.service(carts)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(cartsRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(carts.getServiceName(), cartsDb.getServiceName())) {
				ServiceDependency cartsCartsDbDependency = ServiceDependency.builder()
					.service(carts)
					.dependency(cartsDb)
					.build();
				servicesDependencies.save(cartsCartsDbDependency);
			}
			if (!servicesDependencies.hasDependency(orders.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependency ordersRegistrationServerDependency = ServiceDependency.builder()
					.service(orders)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(ordersRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(orders.getServiceName(), payment.getServiceName())) {
				ServiceDependency ordersPaymentDependency = ServiceDependency.builder()
					.service(orders)
					.dependency(payment)
					.build();
				servicesDependencies.save(ordersPaymentDependency);
			}
			if (!servicesDependencies.hasDependency(orders.getServiceName(), shipping.getServiceName())) {
				ServiceDependency ordersShippingDependency = ServiceDependency.builder()
					.service(orders)
					.dependency(shipping)
					.build();
				servicesDependencies.save(ordersShippingDependency);
			}
			if (!servicesDependencies.hasDependency(orders.getServiceName(), ordersDb.getServiceName())) {
				ServiceDependency ordersOrdersDbDependency = ServiceDependency.builder()
					.service(orders)
					.dependency(ordersDb)
					.build();
				servicesDependencies.save(ordersOrdersDbDependency);
			}
			if (!servicesDependencies.hasDependency(shipping.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependency shippingRegistrationServerDependency = ServiceDependency.builder()
					.service(shipping)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(shippingRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(shipping.getServiceName(), rabbitmq.getServiceName())) {
				ServiceDependency shippingRabbitmqDependency = ServiceDependency.builder()
					.service(shipping)
					.dependency(rabbitmq)
					.build();
				servicesDependencies.save(shippingRabbitmqDependency);
			}
			if (!servicesDependencies.hasDependency(queueMaster.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependency queueMasterRegistrationServerDependency = ServiceDependency.builder()
					.service(queueMaster)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(queueMasterRegistrationServerDependency);
			}
			if (!servicesDependencies.hasDependency(queueMaster.getServiceName(), rabbitmq.getServiceName())) {
				ServiceDependency queueMasterRabbitmqDependency = ServiceDependency.builder()
					.service(queueMaster)
					.dependency(rabbitmq)
					.build();
				servicesDependencies.save(queueMasterRabbitmqDependency);
			}
			if (!servicesDependencies.hasDependency(rabbitmq.getServiceName(), registrationServer.getServiceName())) {
				ServiceDependency rabbitmqRegistrationServerDependency = ServiceDependency.builder()
					.service(rabbitmq)
					.dependency(registrationServer)
					.build();
				servicesDependencies.save(rabbitmqRegistrationServerDependency);
			}

			/*// regions
			for (Region region : Region.values()) {
				if (!regionsService.hasRegion(region)) {
					Region region = Region.builder()
						.region(region)
						.active(true)
						.build();
					regionsService.addRegion(region);
				}
			}*/

			// edge hosts
			if (!edgeHostsService.hasEdgeHost("dpimenta.ddns.net")) {
				Coordinates coordinates = new Coordinates("Portugal", 39.575097, -8.909794);
				RegionEnum region = RegionEnum.getClosestRegion(coordinates);
				edgeHostsService.addManualEdgeHost(EdgeHost.builder()
					.username("daniel")
					.publicIpAddress("2.82.208.89")
					.privateIpAddress("192.168.1.83")
					.publicDnsName("dpimenta.ddns.net")
					.region(region)
					.coordinates(coordinates)
					.build());
			}

			// cloud hosts
			cloudHostsService.synchronizeDatabaseCloudHosts();

			// component types
			ComponentType host;
			try {
				host = componentTypesService.getComponentType(ComponentTypeEnum.HOST.name());
			}
			catch (EntityNotFoundException ignored) {
				host = ComponentType.builder()
					.type(ComponentTypeEnum.HOST)
					.build();
				host = componentTypesService.addComponentType(host);
			}
			ComponentType service;
			try {
				service = componentTypesService.getComponentType(ComponentTypeEnum.SERVICE.name());
			}
			catch (EntityNotFoundException ignored) {
				service = ComponentType.builder()
					.type(ComponentTypeEnum.SERVICE)
					.build();
				service = componentTypesService.addComponentType(service);
			}
			ComponentType container;
			try {
				container = componentTypesService.getComponentType(ComponentTypeEnum.CONTAINER.name());
			}
			catch (EntityNotFoundException ignored) {
				container = ComponentType.builder()
					.type(ComponentTypeEnum.CONTAINER)
					.build();
				container = componentTypesService.addComponentType(container);
			}

			// operator
			Operator notEqualTo;
			try {
				notEqualTo = operatorsService.getOperator(OperatorEnum.NOT_EQUAL_TO.name());
			}
			catch (EntityNotFoundException ignored) {
				notEqualTo = Operator.builder()
					.operator(OperatorEnum.NOT_EQUAL_TO)
					.symbol(OperatorEnum.NOT_EQUAL_TO.getSymbol())
					.build();
				notEqualTo = operatorsService.addOperator(notEqualTo);
			}
			Operator equalTo;
			try {
				equalTo = operatorsService.getOperator(OperatorEnum.EQUAL_TO.name());
			}
			catch (EntityNotFoundException ignored) {
				equalTo = Operator.builder()
					.operator(OperatorEnum.EQUAL_TO)
					.symbol(OperatorEnum.EQUAL_TO.getSymbol())
					.build();
				equalTo = operatorsService.addOperator(equalTo);
			}
			Operator greaterThan;
			try {
				greaterThan = operatorsService.getOperator(OperatorEnum.GREATER_THAN.name());
			}
			catch (EntityNotFoundException ignored) {
				greaterThan = Operator.builder()
					.operator(OperatorEnum.GREATER_THAN)
					.symbol(OperatorEnum.GREATER_THAN.getSymbol())
					.build();
				greaterThan = operatorsService.addOperator(greaterThan);
			}
			Operator lessThan;
			try {
				lessThan = operatorsService.getOperator(OperatorEnum.LESS_THAN.name());
			}
			catch (EntityNotFoundException ignored) {
				lessThan = Operator.builder()
					.operator(OperatorEnum.LESS_THAN)
					.symbol(OperatorEnum.LESS_THAN.getSymbol())
					.build();
				lessThan = operatorsService.addOperator(lessThan);
			}
			Operator greaterThanOrEqualTo;
			try {
				greaterThanOrEqualTo = operatorsService.getOperator(OperatorEnum.GREATER_THAN_OR_EQUAL_TO.name());
			}
			catch (EntityNotFoundException ignored) {
				greaterThanOrEqualTo = Operator.builder()
					.operator(OperatorEnum.GREATER_THAN_OR_EQUAL_TO)
					.symbol(OperatorEnum.GREATER_THAN_OR_EQUAL_TO.getSymbol())
					.build();
				greaterThanOrEqualTo = operatorsService.addOperator(greaterThanOrEqualTo);
			}
			Operator lessThanOrEqualTo;
			try {
				lessThanOrEqualTo = operatorsService.getOperator(OperatorEnum.LESS_THAN_OR_EQUAL_TO.name());
			}
			catch (EntityNotFoundException ignored) {
				lessThanOrEqualTo = Operator.builder()
					.operator(OperatorEnum.LESS_THAN_OR_EQUAL_TO)
					.symbol(OperatorEnum.LESS_THAN_OR_EQUAL_TO.getSymbol())
					.build();
				lessThanOrEqualTo = operatorsService.addOperator(lessThanOrEqualTo);
			}

			// service decisions
			Decision serviceDecisionNone;
			try {
				serviceDecisionNone = decisionsService.getServicePossibleDecision(RuleDecisionEnum.NONE.name());
			}
			catch (EntityNotFoundException ignored) {
				serviceDecisionNone = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
					.componentType(service)
					.ruleDecision(RuleDecisionEnum.NONE)
					.build();
				serviceDecisionNone = decisionsService.addDecision(serviceDecisionNone);
			}
			Decision serviceDecisionReplicate;
			try {
				serviceDecisionReplicate = decisionsService.getServicePossibleDecision(RuleDecisionEnum.REPLICATE.name());
			}
			catch (EntityNotFoundException ignored) {
				serviceDecisionReplicate = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
					.componentType(service)
					.ruleDecision(RuleDecisionEnum.REPLICATE)
					.build();
				serviceDecisionReplicate = decisionsService.addDecision(serviceDecisionReplicate);
			}
			Decision serviceDecisionMigrate;
			try {
				serviceDecisionMigrate = decisionsService.getServicePossibleDecision(RuleDecisionEnum.MIGRATE.name());
			}
			catch (EntityNotFoundException ignored) {
				serviceDecisionMigrate = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
					.componentType(service)
					.ruleDecision(RuleDecisionEnum.MIGRATE)
					.build();
				serviceDecisionMigrate = decisionsService.addDecision(serviceDecisionMigrate);
			}
			Decision serviceDecisionStop;
			try {
				serviceDecisionStop = decisionsService.getServicePossibleDecision(RuleDecisionEnum.STOP.name());
			}
			catch (EntityNotFoundException ignored) {
				serviceDecisionStop = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
					.componentType(service)
					.ruleDecision(RuleDecisionEnum.STOP)
					.build();
				serviceDecisionStop = decisionsService.addDecision(serviceDecisionStop);
			}
			// host decisions
			Decision hostDecisionNone;
			try {
				hostDecisionNone = decisionsService.getHostPossibleDecision(RuleDecisionEnum.NONE.name());
			}
			catch (EntityNotFoundException ignored) {
				hostDecisionNone = Decision.builder()
					.componentType(host)
					.ruleDecision(RuleDecisionEnum.NONE)
					.build();
				hostDecisionNone = decisionsService.addDecision(hostDecisionNone);
			}
			Decision hostDecisionStart;
			try {
				hostDecisionStart = decisionsService.getHostPossibleDecision(RuleDecisionEnum.OVERWORK.name());
			}
			catch (EntityNotFoundException ignored) {
				hostDecisionStart = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
					.componentType(host)
					.ruleDecision(RuleDecisionEnum.OVERWORK)
					.build();
				hostDecisionStart = decisionsService.addDecision(hostDecisionStart);
			}
			Decision hostDecisionUnderwork;
			try {
				hostDecisionUnderwork = decisionsService.getHostPossibleDecision(RuleDecisionEnum.UNDERWORK.name());
			}
			catch (EntityNotFoundException ignored) {
				hostDecisionUnderwork = pt.unl.fct.miei.usmanagement.manager.rulesystem.decision.Decision.builder()
					.componentType(host)
					.ruleDecision(RuleDecisionEnum.UNDERWORK)
					.build();
				hostDecisionUnderwork = decisionsService.addDecision(hostDecisionUnderwork);
			}

			// fields
			// TODO is missing more fields...
			Field cpu;
			try {
				cpu = fieldsService.getField("cpu");
			}
			catch (EntityNotFoundException ignored) {
				cpu = Field.builder()
					.name("cpu")
					.build();
				cpu = fieldsService.addField(cpu);
			}
			Field ram;
			try {
				ram = fieldsService.getField("ram");
			}
			catch (EntityNotFoundException ignored) {
				ram = Field.builder()
					.name("ram")
					.query(PrometheusQueryEnum.MEMORY_USAGE)
					.build();
				ram = fieldsService.addField(ram);
			}
			Field cpuPercentage;
			try {
				cpuPercentage = fieldsService.getField("cpu-%");
			}
			catch (EntityNotFoundException ignored) {
				cpuPercentage = Field.builder()
					.name("cpu-%")
					.query(PrometheusQueryEnum.CPU_USAGE_PERCENTAGE)
					.build();
				cpuPercentage = fieldsService.addField(cpuPercentage);
			}
			Field ramPercentage;
			try {
				ramPercentage = fieldsService.getField("ram-%");
			}
			catch (EntityNotFoundException ignored) {
				ramPercentage = Field.builder()
					.name("ram-%")
					.query(PrometheusQueryEnum.MEMORY_USAGE_PERCENTAGE)
					.build();
				ramPercentage = fieldsService.addField(ramPercentage);
			}
			Field rxBytes;
			try {
				rxBytes = fieldsService.getField("rx-bytes");
			}
			catch (EntityNotFoundException ignored) {
				rxBytes = Field.builder()
					.name("rx-bytes")
					.build();
				rxBytes = fieldsService.addField(rxBytes);
			}
			Field txBytes;
			try {
				txBytes = fieldsService.getField("tx-bytes");
			}
			catch (EntityNotFoundException ignored) {
				txBytes = Field.builder()
					.name("tx-bytes")
					.build();
				txBytes = fieldsService.addField(txBytes);
			}
			Field rxBytesPerSec;
			try {
				rxBytesPerSec = fieldsService.getField("rx-bytes-per-sec");
			}
			catch (EntityNotFoundException ignored) {
				rxBytesPerSec = Field.builder()
					.name("rx-bytes-per-sec")
					.build();
				rxBytesPerSec = fieldsService.addField(rxBytesPerSec);
			}
			Field txBytesPerSec;
			try {
				txBytesPerSec = fieldsService.getField("tx-bytes-per-sec");
			}
			catch (EntityNotFoundException ignored) {
				txBytesPerSec = Field.builder()
					.name("tx-bytes-per-sec")
					.build();
				txBytesPerSec = fieldsService.addField(txBytesPerSec);
			}
			Field latency;
			try {
				latency = fieldsService.getField("latency");
			}
			catch (EntityNotFoundException ignored) {
				latency = Field.builder()
					.name("latency")
					.build();
				latency = fieldsService.addField(latency);
			}
			Field bandwidthPercentage;
			try {
				bandwidthPercentage = fieldsService.getField("bandwidth-%");
			}
			catch (EntityNotFoundException ignored) {
				bandwidthPercentage = Field.builder()
					.name("bandwidth-%")
					.build();
				bandwidthPercentage = fieldsService.addField(bandwidthPercentage);
			}
			Field filesystemAvailableSpace;
			try {
				filesystemAvailableSpace = fieldsService.getField("filesystem-available-space");
			}
			catch (EntityNotFoundException ignored) {
				latency = Field.builder()
					.name("filesystem-available-space")
					.query(PrometheusQueryEnum.FILESYSTEM_AVAILABLE_SPACE)
					.build();
				filesystemAvailableSpace = fieldsService.addField(latency);
			}


			// value modes
			ValueMode effectiveValue;
			try {
				effectiveValue = valueModesService.getValueMode("effective-val");
			}
			catch (EntityNotFoundException ignored) {
				effectiveValue = ValueMode.builder()
					.name("effective-val")
					.build();
				effectiveValue = valueModesService.addValueMode(effectiveValue);
			}
			ValueMode averageValue;
			try {
				averageValue = valueModesService.getValueMode("avg-val");
			}
			catch (EntityNotFoundException ignored) {
				averageValue = ValueMode.builder()
					.name("avg-val")
					.build();
				averageValue = valueModesService.addValueMode(averageValue);
			}
			ValueMode deviationPercentageOnAverageValue;
			try {
				deviationPercentageOnAverageValue = valueModesService.getValueMode("deviation-%-on-avg-val");
			}
			catch (EntityNotFoundException ignored) {
				deviationPercentageOnAverageValue = ValueMode.builder()
					.name("deviation-%-on-avg-val")
					.build();
				deviationPercentageOnAverageValue = valueModesService.addValueMode(deviationPercentageOnAverageValue);
			}
			ValueMode deviationPercentageOnLastValue;
			try {
				deviationPercentageOnLastValue = valueModesService.getValueMode("deviation-%-on-last-val");
			}
			catch (EntityNotFoundException ignored) {
				deviationPercentageOnLastValue = ValueMode.builder()
					.name("deviation-%-on-last-val")
					.build();
				deviationPercentageOnLastValue = valueModesService.addValueMode(deviationPercentageOnLastValue);
			}

			// conditions
			Condition cpuPercentageOver90;
			try {
				cpuPercentageOver90 = conditionsService.getCondition("CpuPercentageOver90");
			}
			catch (EntityNotFoundException ignored) {
				cpuPercentageOver90 = Condition.builder()
					.name("CpuPercentageOver90")
					.valueMode(effectiveValue)
					.field(cpuPercentage)
					.operator(greaterThan)
					.value(90)
					.build();
				cpuPercentageOver90 = conditionsService.addCondition(cpuPercentageOver90);
			}
			Condition ramPercentageOver90;
			try {
				ramPercentageOver90 = conditionsService.getCondition("RamPercentageOver90");
			}
			catch (EntityNotFoundException ignored) {
				ramPercentageOver90 = Condition.builder()
					.name("RamPercentageOver90")
					.valueMode(effectiveValue)
					.field(ramPercentage)
					.operator(greaterThan)
					.value(90)
					.build();
				ramPercentageOver90 = conditionsService.addCondition(ramPercentageOver90);
			}
			Condition rxBytesPerSecOver500000;
			try {
				rxBytesPerSecOver500000 = conditionsService.getCondition("RxBytesPerSecOver500000");
			}
			catch (EntityNotFoundException ignored) {
				rxBytesPerSecOver500000 = Condition.builder()
					.name("RxBytesPerSecOver500000")
					.valueMode(effectiveValue)
					.field(rxBytesPerSec)
					.operator(greaterThan)
					.value(500000)
					.build();
				rxBytesPerSecOver500000 = conditionsService.addCondition(rxBytesPerSecOver500000);
			}
			Condition txBytesPerSecOver100000;
			try {
				txBytesPerSecOver100000 = conditionsService.getCondition("TxBytesPerSecOver100000");
			}
			catch (EntityNotFoundException ignored) {
				txBytesPerSecOver100000 = Condition.builder()
					.name("TxBytesPerSecOver100000")
					.valueMode(effectiveValue)
					.field(txBytesPerSec)
					.operator(greaterThan)
					.value(100000)
					.build();
				txBytesPerSecOver100000 = conditionsService.addCondition(txBytesPerSecOver100000);
			}

			// generic host rules
			HostRule cpuAndRamOver90GenericHostRule;
			try {
				cpuAndRamOver90GenericHostRule = hostRulesService.getRule("CpuAndRamOver90");
			}
			catch (EntityNotFoundException ignored) {
				cpuAndRamOver90GenericHostRule = HostRule.builder()
					.name("CpuAndRamOver90")
					.priority(1)
					.decision(hostDecisionStart)
					.generic(true)
					.build();
				cpuAndRamOver90GenericHostRule = hostRulesService.addRule(cpuAndRamOver90GenericHostRule);

				HostRuleCondition cpuOver90Condition = HostRuleCondition.builder()
					.hostRule(cpuAndRamOver90GenericHostRule)
					.hostCondition(cpuPercentageOver90)
					.build();
				hostRuleConditions.save(cpuOver90Condition);
				HostRuleCondition ramOver90Condition = HostRuleCondition.builder()
					.hostRule(cpuAndRamOver90GenericHostRule)
					.hostCondition(ramPercentageOver90)
					.build();
				hostRuleConditions.save(ramOver90Condition);
			}
			// generic service rules
			ServiceRule rxOver500000GenericServiceRule;
			try {
				rxOver500000GenericServiceRule = serviceRulesService.getRule("RxOver500000");
			}
			catch (EntityNotFoundException ignored) {
				rxOver500000GenericServiceRule = ServiceRule.builder()
					.name("RxOver500000")
					.priority(1)
					.decision(serviceDecisionReplicate)
					.generic(true)
					.build();
				rxOver500000GenericServiceRule = serviceRulesService.addRule(rxOver500000GenericServiceRule);
				ServiceRuleCondition rxOver500000Condition = ServiceRuleCondition.builder()
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
