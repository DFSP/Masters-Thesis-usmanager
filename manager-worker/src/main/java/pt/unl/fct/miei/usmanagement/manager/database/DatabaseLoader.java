package pt.unl.fct.miei.usmanagement.manager.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pt.unl.fct.miei.usmanagement.manager.exceptions.EntityNotFoundException;
import pt.unl.fct.miei.usmanagement.manager.services.users.User;
import pt.unl.fct.miei.usmanagement.manager.services.users.UserRoleEnum;
import pt.unl.fct.miei.usmanagement.manager.services.users.UsersService;

@Slf4j
@Component
public class DatabaseLoader {

	@Bean
	CommandLineRunner initDatabase(UsersService usersService) {
		return args -> loadUsers(usersService);
	}

	@Transactional
	void loadUsers(UsersService usersService) {
		try {
			usersService.getUser("admin");
		}
		catch (EntityNotFoundException e) {
			User admin = User.builder()
				.firstName("admin")
				.lastName("admin")
				.username("admin")
				.password("admin")
				.email("admin@admin.pt")
				.role(UserRoleEnum.ROLE_SYS_ADMIN)
				.build();
			usersService.addUser(admin);
		}

		try {
			usersService.getUser("danielfct");
		}
		catch (EntityNotFoundException e) {
			User danielfct = User.builder()
				.firstName("daniel")
				.lastName("pimenta")
				.username("danielfct")
				.password("danielfct")
				.email("d.pimenta@campus.fct.unl.pt")
				.role(UserRoleEnum.ROLE_SYS_ADMIN)
				.build();
			usersService.addUser(danielfct);
		}
	}


}
