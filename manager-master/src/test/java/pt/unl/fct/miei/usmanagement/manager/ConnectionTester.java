package pt.unl.fct.miei.usmanagement.manager;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.keyprovider.PKCS8KeyFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

@RunWith(SpringRunner.class)
public class ConnectionTester {

	@Test
	public void testEdgeConnection() throws IOException {
		SSHClient sshClient = new SSHClient();
		sshClient.addHostKeyVerifier(new PromiscuousVerifier());
		sshClient.connect("127.0.0.1");
		PKCS8KeyFile keyFile = new PKCS8KeyFile();
		InputStream is = getClass().getClassLoader().getResourceAsStream("edge/daniel_danielfct_ddns_net");
		Reader reader = new InputStreamReader(is);
		keyFile.init(reader);
		sshClient.authPublickey("root", keyFile);
	}

}
