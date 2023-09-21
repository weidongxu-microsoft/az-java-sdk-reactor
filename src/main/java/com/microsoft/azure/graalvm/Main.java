package com.microsoft.azure.graalvm;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;

public class Main {

    private static final ClientLogger logger = new ClientLogger(Main.class);

    public static void main(String args[]) {

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        ContainerInstanceManager manager = ContainerInstanceManager
                .configure()
                .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .authenticate(credential, profile);

        String rgName = "rg1-weidxu";

        // a case for CRUD
        try {
            System.out.println("create container group");
            ContainerGroup containerGroup = manager.containerGroups()
                    .define("container1-weidxu")
                    .withRegion(Region.US_WEST3)
                    .withNewResourceGroup(rgName)
                    .withLinux()
                    .withPublicImageRegistryOnly()
                    .withoutVolume()
                    .withContainerInstance("nginx", 80)
                    .withNewVirtualNetwork("10.0.0.0/24")
                    .create();
            System.out.println("container group created: " + containerGroup.name());

            manager.containerGroups().listByResourceGroup(rgName)
                    .forEach(cg -> System.out.println("list container group: " + cg.name()));

            containerGroup.update()
                    .withTag("use", "testing")
                    .apply();
            System.out.println("container group updated: " + containerGroup.name());

            containerGroup = manager.containerGroups().getByResourceGroup("rg1-weidxu", "container1-weidxu");
            System.out.println("get container group: " + containerGroup.name());

            manager.containerGroups().deleteById(containerGroup.id());
            System.out.println("container group deleted: " + containerGroup.name());
        } finally {
            manager.resourceManager().resourceGroups().deleteByName(rgName);
        }

        // a case for exception
        try {
            manager.containerGroups().getByResourceGroup("rg1-weidxu", "container2-not-exist");
        } catch (ManagementException e) {
            System.out.println("expected exception: " + e.getValue().getCode());
        }
    }
}
