package fr.volkaert.github_webhook_to_everest;

import lombok.Data;

@Data
public class EverestEventPayload {

    public String githubEventPayload;
    public String githubDeliveryHeader; // example: "74f42ad0-1011-11ec-86d5-9325ea26beb4"
    public String githubEventType; // example: "ping"
    public String githubHookId; // example: "317034115",
    public String githubHookInstallationTargetId; // example: "315454814"
    public String githubHookInstallationTargetType; // example: "repository"

    public String hubSignature; // example: "sha1=0676173b6e5778ecaa0c768bf780fdceb331e589",
    public String hubSignature256; // example: "sha256=fab1e81196a5c813a1762215c4f5909be6986044fc223ffb87d016b25c099769"

}
