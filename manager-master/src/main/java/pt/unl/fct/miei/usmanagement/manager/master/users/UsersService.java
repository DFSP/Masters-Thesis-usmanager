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

package pt.unl.fct.miei.usmanagement.manager.master.users;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.users.UserEntity;
import pt.unl.fct.miei.usmanagement.manager.database.users.UserRole;
import pt.unl.fct.miei.usmanagement.manager.database.users.UsersRepository;
import pt.unl.fct.miei.usmanagement.manager.services.exceptions.EntityNotFoundException;

import java.util.List;

@Service
public class UsersService implements UserDetailsService {

	private final UsersRepository users;
	private final PasswordEncoder encoder;

	public UsersService(UsersRepository users, @Lazy PasswordEncoder encoder) {
		this.users = users;
		this.encoder = encoder;
	}

	public UserEntity addUser(UserEntity userEntity) {
		userEntity.setPassword(encoder.encode(userEntity.getPassword()));
		return users.save(userEntity);
	}

	public UserEntity addUser(String firstName, String lastName, String username, String password, String email,
							  UserRole role) {
		UserEntity user = UserEntity.builder()
			.firstName(firstName)
			.lastName(lastName)
			.username(username)
			.password(encoder.encode(password))
			.email(email)
			.role(role)
			.build();
		return users.save(user);
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		UserEntity user = users.findByUsername(username);
		if (user == null) {
			throw new EntityNotFoundException(UserEntity.class, "username", username);
		}
		return new User(username, user.getPassword(), List.of(new SimpleGrantedAuthority(user.getRole().name())));
	}

	public boolean hasUser(String username) {
		return users.hasUser(username);
	}


	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

}
