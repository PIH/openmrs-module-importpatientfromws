/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.importpatientfromws.api;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.OpenmrsService;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
 * <p/>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(ImportPatientFromWebService.class).someMethod();
 * </code>
 *
 * @see org.openmrs.api.context.Context
 */
public interface ImportPatientFromWebService extends OpenmrsService {

    Patient toPatient(String json, Map<String, PatientIdentifierType> identifierTypesByUuid, Map<String, Location> locationsByUuid, Map<String, PersonAttributeType> attributeTypesByUuid) throws IOException;

    List<Patient> searchRemoteServer(String serverName, String name, String gender, Date birthdate) throws IOException;

    void registerRemoteServer(String serverName, RemoteServerConfiguration remoteServerConfiguration);

}