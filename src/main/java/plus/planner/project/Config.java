package plus.planner.project;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

import static plus.planner.project.utils.PemUtils.readPublicKeyFromFile;

@Configuration
public class Config {
    @Bean
    public JWTVerifier jwtVerifier() throws IOException {
        final Algorithm algorithm = Algorithm.RSA512((RSAPublicKey) readPublicKeyFromFile("src/main/resources/PublicKey.pem", "RSA"), null);
        return JWT.require(algorithm).withIssuer("plus-planner-token-service").build();
    }
}
