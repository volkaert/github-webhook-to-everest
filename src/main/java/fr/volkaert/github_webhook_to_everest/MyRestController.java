package fr.volkaert.github_webhook_to_everest;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RestController
public class MyRestController {

    @Value("${everest.publication-url}")
    String everestPublicationURL;

    @Value("${everest.publication-code}")
    String everestPublicationCode;

    @Value("${github.webhook-secret}")
    String githubWebhookSecret;

    @Autowired
    RestTemplate restTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(MyRestController.class);

    @PostMapping(value = "/event", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> event(@RequestBody String githubEventPayload, @RequestHeader HttpHeaders httpHeaders) {
        LOGGER.info("Github Event received: {}", githubEventPayload);
        LOGGER.info("Github Headers received: {}", httpHeaders);

        String hubSignature256 = httpHeaders.getFirst("x-hub-signature-256");
        if (hubSignature256 != null) {
            if (! arePayloadAndSignatureMatching(githubEventPayload, hubSignature256)) {
                LOGGER.error("Payload and Signature are not matching");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        EverestEventPayload everestEventPayload = new EverestEventPayload();
        everestEventPayload.githubEventPayload = githubEventPayload;
        everestEventPayload.githubDeliveryHeader = httpHeaders.getFirst("x-github-delivery"); // example: "74f42ad0-1011-11ec-86d5-9325ea26beb4"
        everestEventPayload.githubEventType = httpHeaders.getFirst("x-github-event"); // example: "ping"
        everestEventPayload.githubHookId = httpHeaders.getFirst("x-github-hook-id"); // example: "317034115",
        everestEventPayload.githubHookInstallationTargetId = httpHeaders.getFirst("x-github-hook-installation-target-id"); // example: "315454814"
        everestEventPayload.githubHookInstallationTargetType =  httpHeaders.getFirst("x-github-hook-installation-target-type"); // example: "repository"
        everestEventPayload.hubSignature = httpHeaders.getFirst("x-hub-signature"); // example: "sha1=0676173b6e5778ecaa0c768bf780fdceb331e589"
        everestEventPayload.hubSignature256 = httpHeaders.getFirst("x-hub-signature-256"); // example: "sha256=fab1e81196a5c813a1762215c4f5909be6986044fc223ffb87d016b25c099769"

        EverestEvent everestEvent = new EverestEvent();
        everestEvent.publicationCode = everestPublicationCode;
        everestEvent.payload = everestEventPayload;

        LOGGER.info("Everest Event: {}", everestEvent);

        HttpEntity<EverestEvent> everestPublicationRequest = new HttpEntity<EverestEvent>(everestEvent);
        ResponseEntity<EverestResponse> everestResponse = restTemplate.exchange(
                everestPublicationURL, HttpMethod.POST, everestPublicationRequest, EverestResponse.class);
        LOGGER.info("Everest returned the HTTP code {}", everestResponse.getStatusCode());

        return ResponseEntity.status(everestResponse.getStatusCode()).build();
    }

    private boolean arePayloadAndSignatureMatching(String githubEventPayload, String hubSignature256) {
        try {
            String signature = hubSignature256.substring("sha256=".length());
            String expectedSignature = WebhookUtils.computeHmacSha256(githubWebhookSecret, githubEventPayload);
            return WebhookUtils.secureCompare(expectedSignature, signature);
        } catch (Exception ex) {
            LOGGER.error("ERROR: {}", ex.getMessage(), ex);
            return false;
        }
    }
}
