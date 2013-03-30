/*
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

import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Ignore
public class LiveWebServiceTest extends BaseModuleContextSensitiveTest {

    @Test
    public void testLiveWebservice() throws Exception {
        PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierType(2);
        Map<String, PatientIdentifierType> identifierTypes = new HashMap<String, PatientIdentifierType>();
        identifierTypes.put("a541af1e-105c-40bf-b345-ba1fd6a59b85", patientIdentifierType);

        Location xanadu = Context.getLocationService().getLocation(2);
        Map<String, Location> locationMap = new HashMap<String, Location>();
        locationMap.put("23e7bb0d-51f9-4d5f-b34b-2fbbfeea1960", xanadu);

        PersonAttributeType birthplace = Context.getPersonService().getPersonAttributeType(2);
        Map<String, PersonAttributeType> attributeTypeMap = new HashMap<String, PersonAttributeType>();
        attributeTypeMap.put("340d04c4-0370-102d-b0e3-001ec94a0cc1", birthplace);

        RemoteServerConfiguration remoteServerConfiguration = new RemoteServerConfiguration();
        remoteServerConfiguration.setUrl("REPLACE_ME");
        remoteServerConfiguration.setUsername("REPLACE_ME");
        remoteServerConfiguration.setPassword("REPLACE_ME");
        remoteServerConfiguration.setIdentifierTypeMap(identifierTypes);
        remoteServerConfiguration.setLocationMap(locationMap);
        remoteServerConfiguration.setAttributeTypeMap(attributeTypeMap);

        ImportPatientFromWebService service = Context.getService(ImportPatientFromWebService.class);
        service.registerRemoteServer("testing", remoteServerConfiguration);
        List<Patient> patients = service.searchRemoteServer("testing", "ellen", "F", null);

        System.out.println("=== Found " + patients.size() + " patients ===");
        for (Patient patient : patients) {
            System.out.println(patient.getPatientIdentifier() + " - " + patient.getPersonName());
        }

    }
}
