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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.importpatientfromws.RemotePatient;
import org.openmrs.module.importpatientfromws.api.impl.ImportPatientFromWebServiceImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class,WebResource.Builder.class})
public class BehaviorTest {

    PatientService mockPatientService;
    private ImportPatientFromWebService service;
    private SimpleDateFormat dateFormat;

    private RemoteServerConfiguration remoteServerConfiguration;
    private PatientIdentifierType zlEmrId;
    private Location lacolline;
    private PersonAttributeType telephoneNumber;
    private String searchResponse;

    @Before
    public void setUp() throws Exception {
        mockPatientService = PowerMockito.mock(PatientService.class);
        ImportPatientFromWebServiceImpl importService = new ImportPatientFromWebServiceImpl(new MockClient());
        importService.setPatientService(mockPatientService);
        this.service = importService;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        zlEmrId = new PatientIdentifierType();
        Map<String, PatientIdentifierType> identifierTypes = new HashMap<String, PatientIdentifierType>();
        identifierTypes.put("a541af1e-105c-40bf-b345-ba1fd6a59b85", zlEmrId);

        lacolline = new Location();
        Map<String, Location> locationMap = new HashMap<String, Location>();
        locationMap.put("23e7bb0d-51f9-4d5f-b34b-2fbbfeea1960", lacolline);

        telephoneNumber = new PersonAttributeType();
        Map<String, PersonAttributeType> attributeTypeMap = new HashMap<String, PersonAttributeType>();
        attributeTypeMap.put("340d04c4-0370-102d-b0e3-001ec94a0cc1", telephoneNumber);

        //when(mockPatientService.getPatients(null, "", Arrays.asList(zlEmrId), true)).thenReturn(Collections.singletonList(new Patient()));
        remoteServerConfiguration = new RemoteServerConfiguration("http://google.com", "user", "pass", identifierTypes, locationMap, attributeTypeMap);
    }

    @Test
    public void testParsingPatient() throws Exception {
        String patientJson = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("patient.json"), "UTF-8");

        RemotePatient remotePatient = service.toPatient(patientJson, remoteServerConfiguration.getIdentifierTypeMap(),
                remoteServerConfiguration.getLocationMap(), remoteServerConfiguration.getAttributeTypeMap());
        assertNotNull(remotePatient);
        Patient actual = remotePatient.getPatient();
        assertThat(actual.getGender(), is("F"));
        assertThat(actual.getBirthdate(), is(dateFormat.parse("1969-09-20T00:00:00.000-0400")));
        assertThat(actual.getBirthdateEstimated(), is(false));

        assertThat(actual.getActiveIdentifiers().size(), is(1));
        assertThat(actual.getActiveIdentifiers().get(0).getIdentifierType(), is(zlEmrId));
        assertThat(actual.getActiveIdentifiers().get(0).getIdentifier(), is("2ALH69"));
        assertThat(actual.getActiveIdentifiers().get(0).getLocation(), is(lacolline));

        assertThat(actual.getNames().size(), is(1));
        assertThat(actual.getPersonName().getGivenName(), is("Ellen"));
        assertThat(actual.getPersonName().getMiddleName(), nullValue());
        assertThat(actual.getPersonName().getFamilyName(), is("Ball"));
        assertThat(actual.getPersonName().getFamilyName2(), nullValue());

        assertThat(actual.getAddresses().size(), is(1));
        assertThat(actual.getPersonAddress().getAddress1(), is("Cange"));
        assertThat(actual.getPersonAddress().getAddress2(), is("hill"));
        assertThat(actual.getPersonAddress().getAddress3(), is("3\u00e8me La Hoye"));
        assertThat(actual.getPersonAddress().getAddress4(), nullValue());
        assertThat(actual.getPersonAddress().getAddress5(), nullValue());
        assertThat(actual.getPersonAddress().getAddress6(), nullValue());
        assertThat(actual.getPersonAddress().getCityVillage(), is("Lascahobas"));
        assertThat(actual.getPersonAddress().getStateProvince(), is("Centre"));
        assertThat(actual.getPersonAddress().getCountry(), is("Haiti"));
        assertThat(actual.getPersonAddress().getPostalCode(), nullValue());
        assertThat(actual.getPersonAddress().getCountyDistrict(), nullValue());
        assertThat(actual.getPersonAddress().getStartDate(), nullValue());
        assertThat(actual.getPersonAddress().getEndDate(), nullValue());
        assertThat(actual.getPersonAddress().getLatitude(), nullValue());
        assertThat(actual.getPersonAddress().getLongitude(), nullValue());

        assertThat(actual.getAttributes().size(), is(1));
        assertThat(actual.getActiveAttributes().get(0).getAttributeType(), is(telephoneNumber));
        assertThat(actual.getActiveAttributes().get(0).getValue(), is("389389389"));
    }

    @Test
    public void testFetchingPatients() throws Exception {
        searchResponse = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("query-results.json"), "UTF-8");
        service.registerRemoteServer("lacolline", remoteServerConfiguration);
        List<RemotePatient> results = service.searchRemoteServer("lacolline", "Ellen Ball", "F", null, null);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getPatient().getPersonName().toString(), is("Ellen Ball"));
    }


    @Test
    public void testFetchingPatientsById() throws Exception {
        searchResponse = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("query-results.json"), "UTF-8");
        service.registerRemoteServer("lacolline", remoteServerConfiguration);
        List<RemotePatient> results = service.searchRemoteServer("lacolline", "2ALH69", null);
        assertThat(results.size(), is(1));
        assertThat(results.get(0).getPatient().getPersonName().toString(), is("Ellen Ball"));
    }

    @Test
    public void testGetRemoteServers() throws Exception {
        service.registerRemoteServer("lacolline", remoteServerConfiguration);
        Map<String, RemoteServerConfiguration> actual = service.getRemoteServers();
        assertThat(actual.size(), is(1));
        RemoteServerConfiguration lacollineRemote = actual.get("lacolline");
        assertThat(lacollineRemote.getUrl(), notNullValue());
        assertThat(lacollineRemote.getUsername(), notNullValue());
        assertThat(lacollineRemote.getIdentifierTypeMap(), notNullValue());
        assertThat(lacollineRemote.getLocationMap(), notNullValue());
        assertThat(lacollineRemote.getAttributeTypeMap(), notNullValue());
        assertThat(lacollineRemote.getPassword(), nullValue());
    }

    private class MockClient extends Client {
        @Override
        public WebResource resource(String u) {
            WebResource.Builder builder = PowerMockito.mock(WebResource.Builder.class);
            PowerMockito.when(builder.get(String.class)).thenReturn(searchResponse);

            WebResource webResource = mock(WebResource.class);
            when(webResource.path(anyString())).thenReturn(webResource);
            when(webResource.queryParam(anyString(), anyString())).thenReturn(webResource);
            when(webResource.accept(any(MediaType.class))).thenReturn(builder);

            return webResource;
        }
    }

}
