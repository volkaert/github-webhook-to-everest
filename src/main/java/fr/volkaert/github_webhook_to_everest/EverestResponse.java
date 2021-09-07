package fr.volkaert.github_webhook_to_everest;

import lombok.Data;

import java.time.Instant;

@Data
public class EverestResponse {
    private String id;
    private Instant creationDate;
    private Instant expirationDate;
}
