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

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.importpatientfromws.api.impl.ImportPatientFromWebServiceImpl;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class BehaviorTest {

    private ImportPatientFromWebService service;
    private String json;
    private SimpleDateFormat dateFormat;

    @Before
    public void setUp() throws Exception {
        json = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("patient.json"), "UTF-8");
        service = new ImportPatientFromWebServiceImpl();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    @Test
    public void testParsingPatient() throws Exception {
        PatientIdentifierType zlEmrId = new PatientIdentifierType();
        Map<String, PatientIdentifierType> identifierTypes = new HashMap<String, PatientIdentifierType>();
        identifierTypes.put("a541af1e-105c-40bf-b345-ba1fd6a59b85", zlEmrId);

        Location lacolline = new Location();
        Map<String, Location> locationMap = new HashMap<String, Location>();
        locationMap.put("23e7bb0d-51f9-4d5f-b34b-2fbbfeea1960", lacolline);

        PersonAttributeType telephoneNumber = new PersonAttributeType();
        Map<String, PersonAttributeType> attributeTypeMap = new HashMap<String, PersonAttributeType>();
        attributeTypeMap.put("340d04c4-0370-102d-b0e3-001ec94a0cc1", telephoneNumber);

        Patient actual = service.toPatient(json, identifierTypes, locationMap, attributeTypeMap);

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

}
