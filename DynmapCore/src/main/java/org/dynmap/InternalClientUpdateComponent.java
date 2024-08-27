package org.dynmap;

import java.util.concurrent.ConcurrentHashMap;

import org.dynmap.servlet.ClientUpdateServlet;
import org.json.simple.JSONObject;
import static org.dynmap.JSONUtils.*;

public class InternalClientUpdateComponent extends ClientUpdateComponent {
    protected long jsonInterval;
    protected long currentTimestamp = 0;
    protected long lastTimestamp = 0;
    private long last_confighash;
    private ConcurrentHashMap<String, JSONObject> updates = new ConcurrentHashMap<String, JSONObject>();
    private JSONObject clientConfiguration = null;
    private static InternalClientUpdateComponent singleton;
    
    public InternalClientUpdateComponent(final DynmapCore dcore, final ConfigurationNode configuration) {
        super(dcore, configuration);
        dcore.addServlet("/up/world/*", new ClientUpdateServlet(dcore));

        if (dcore.isInternalWebServerDisabled) {
        	Log.severe("Using InternalClientUpdateComponent with disable-webserver=true is not supported: there will likely be problems");        	
        }
        jsonInterval = (long)(configuration.getFloat("writeinterval", 1) * 1000);

        dcore.events.addListener("buildclientconfiguration", new Event.Listener<JSONObject>() {
            @Override
            public void triggered(JSONObject t) {
                s(t, "allowwebchat", false);
                s(t, "webchat-interval", 0);
                s(t, "webchat-requires-login", false);
                s(t, "chatlengthlimit", 0);
            }
        });

        core.getServer().scheduleServerTask(new Runnable() {
            @Override
            public void run() {
                currentTimestamp = System.currentTimeMillis();
                if(last_confighash != core.getConfigHashcode()) {
                    writeConfiguration();
                }
                writeUpdates();
                lastTimestamp = currentTimestamp;
                core.getServer().scheduleServerTask(this, jsonInterval/50);
            }}, jsonInterval/50);
        
        core.events.addListener("initialized", new Event.Listener<Object>() {
            @Override
            public void triggered(Object t) {
                writeConfiguration();
                writeUpdates(); /* Make sure we stay in sync */
            }
        });
        core.events.addListener("worldactivated", new Event.Listener<DynmapWorld>() {
            @Override
            public void triggered(DynmapWorld t) {
                writeConfiguration();
                writeUpdates(); /* Make sure we stay in sync */
            }
        });

        /* Initialize */
        writeConfiguration();
        writeUpdates();
        
        singleton = this;
    }
    @SuppressWarnings("unchecked")
    protected void writeUpdates() {
        if(core.mapManager == null) return;
        //Handles Updates
        for (DynmapWorld dynmapWorld : core.mapManager.getWorlds()) {
            JSONObject update = new JSONObject();
            update.put("timestamp", currentTimestamp);
            ClientUpdateEvent clientUpdate = new ClientUpdateEvent(currentTimestamp - 30000, dynmapWorld, update);
            clientUpdate.include_all_users = true;
            core.events.trigger("buildclientupdate", clientUpdate);

            updates.put(dynmapWorld.getName(), update);
        }
    }
    protected void writeConfiguration() {
        JSONObject clientConfiguration = new JSONObject();
        core.events.trigger("buildclientconfiguration", clientConfiguration);
        this.clientConfiguration = clientConfiguration;
        last_confighash = core.getConfigHashcode();
    }
    public static JSONObject getWorldUpdate(String wname) {
        if(singleton != null) {
            return singleton.updates.get(wname);
        }
        return null;
    }
    public static JSONObject getClientConfig() {
        if(singleton != null)
            return singleton.clientConfiguration;
        return null;
    }
}
