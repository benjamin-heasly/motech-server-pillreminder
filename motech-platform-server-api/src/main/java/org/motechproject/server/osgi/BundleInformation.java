package org.motechproject.server.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class BundleInformation {
    private enum State {        
        UNINSTALLED(1),
        INSTALLED(2),
        RESOLVED(4),
        STARTING(8),
        STOPPING(16),
        ACTIVE(32),
        UNKNOWN(0);

        int stateId;

        State(int stateId) {
            this.stateId = stateId;
        }
        
        public static State fromInt(int stateId) {
            for (State state : values()) {
                if (stateId == state.stateId) {
                    return state;
                }
            }
            return UNKNOWN;
        }
    }
    
    private long bundleId;
    private Version version;
    private String symbolicName;
    private String location;
    private State state;

    public BundleInformation(Bundle bundle) {
        this.bundleId = bundle.getBundleId();
        this.version = bundle.getVersion();
        this.symbolicName = bundle.getSymbolicName();
        this.location = bundle.getLocation();
        this.state = State.fromInt(bundle.getState());
    }

    public long getBundleId() {
        return bundleId;
    }

    public Version getVersion() {
        return version;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getLocation() {
        return location;
    }

    public State getState() {
        return state;
    }
}