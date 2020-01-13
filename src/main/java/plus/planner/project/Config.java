package plus.planner.project;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

import static plus.planner.project.utils.PemUtils.readPublicKeyFromFile;

@Configuration
public class Config {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        final TrustStrategy acceptingTrustStrategy = new TrustSelfSignedStrategy();

        final SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();

        final SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        final CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);

        final RestTemplate restTemplate = new RestTemplate(requestFactory);

        return restTemplate;
    }
    @Bean
    public JWTVerifier jwtVerifier() throws IOException {
        final Algorithm algorithm = Algorithm.RSA512((RSAPublicKey) readPublicKeyFromFile("src/main/resources/PublicKey.pem", "RSA"), null);
        return JWT.require(algorithm).withIssuer("plus-planner-token-service").build();
    }
}
