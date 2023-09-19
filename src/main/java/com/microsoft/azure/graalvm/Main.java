package com.microsoft.azure.graalvm;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;

public class Main {

    private static final ClientLogger logger = new ClientLogger(Main.class);

    public static void main(String args[]) {

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        ContainerInstanceManager manager = ContainerInstanceManager
                .configure()
                .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .authenticate(credential, profile);

        long count = manager.containerGroups().list().stream().count();

        System.out.println("count: " + count);
    }
}
