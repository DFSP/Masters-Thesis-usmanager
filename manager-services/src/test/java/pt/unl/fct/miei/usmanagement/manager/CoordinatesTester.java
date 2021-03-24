package pt.unl.fct.miei.usmanagement.manager;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import pt.unl.fct.miei.usmanagement.manager.hosts.Coordinates;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class CoordinatesTester {

	@Test
	public void testDistance() {
		Coordinates coordinates1 = new Coordinates(50.110991, 8.632203);
		assertThat(coordinates1.distanceTo(coordinates1)).isEqualTo(0);
	}

}
