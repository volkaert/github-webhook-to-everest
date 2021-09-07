package fr.volkaert.github_webhook_to_everest;

import lombok.Data;

@Data
public class EverestEvent {
    public String publicationCode;
    public EverestEventPayload payload;
}
