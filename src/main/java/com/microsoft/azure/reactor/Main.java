package com.microsoft.azure.reactor;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;

public class Main {

    private static final ClientLogger logger = new ClientLogger(Main.class);
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String args[]) throws InterruptedException {

        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        AzureResourceManager manager = AzureResourceManager
                .configure()
                .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
                .authenticate(credential, profile)
                .withDefaultSubscription();

        String rgName = "rg1-weidxu";

        try {
            CountDownLatch signal = new CountDownLatch(1);

            Mono<ContainerGroup> containerGroupMono = manager.containerGroups()
                    .define("container1-weidxu")
                    .withRegion(Region.US_WEST3)
                    .withNewResourceGroup(rgName)
                    .withLinux()
                    .withPublicImageRegistryOnly()
                    .withoutVolume()
                    .withContainerInstance("nginx", 80)
                    .withNewVirtualNetwork("10.0.0.0/24")
                    .createAsync();

            Disposable disposable = containerGroupMono
                    .subscribe(resource -> {
                        // onNext
                        logger.atInfo().log("resource created, name: " + resource.name());
                    }, error -> {
                        // error
                        logger.atError().log(error);

                        // test only
                        signal.countDown();
                    }, () -> {
                        // completion (without error)
                        logger.atInfo().log("completed");

                        // test only
                        signal.countDown();
                    });

            // test only, wait for async above
            signal.await();
        } finally {
            manager.resourceGroups().deleteByName(rgName);
        }
    }
}
