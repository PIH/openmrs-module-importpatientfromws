package org.openmrs.module.importpatientfromws;

import org.openmrs.Patient;

/**
 */
public class RemotePatient extends Patient {

    private String remoteServer;
    private String remoteUuid;

    public String getRemoteServer() {
        return remoteServer;
    }

    public void setRemoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
    }

    public String getRemoteUuid() {
        return remoteUuid;
    }

    public void setRemoteUuid(String remoteUuid) {
        this.remoteUuid = remoteUuid;
    }
}
