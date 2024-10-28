package codes.shiftmc.shiftEconomyProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
    id = "shifteconomy",
    name = "ShiftEconomyProxy",
    version = BuildConstants.VERSION,
    authors = {"ShiftSad"}
)
public class ShiftEconomyProxy {

    @Inject private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {

    }
}
