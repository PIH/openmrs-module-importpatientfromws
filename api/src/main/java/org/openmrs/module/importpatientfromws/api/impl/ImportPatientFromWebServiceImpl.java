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
package org.openmrs.module.importpatientfromws.api.impl;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.importpatientfromws.api.ImportPatientFromWebService;
import org.openmrs.module.importpatientfromws.api.db.ImportPatientFromWebServiceDAO;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * It is a default implementation of {@link org.openmrs.module.importpatientfromws.api.ImportPatientFromWebService}.
 */
public class ImportPatientFromWebServiceImpl extends BaseOpenmrsService implements ImportPatientFromWebService {

    protected final Log log = LogFactory.getLog(this.getClass());

    private ImportPatientFromWebServiceDAO dao;

    /**
     * @param dao the dao to set
     */
    public void setDao(ImportPatientFromWebServiceDAO dao) {
        this.dao = dao;
    }

    @Override
    public Patient toPatient(String jsonString, Map<String, PatientIdentifierType> identifierTypesByUuid, Map<String, Location> locationsByUuid, Map<String, PersonAttributeType> attributeTypesByUuid) throws IOException {
        JsonNode json = new ObjectMapper().readValue(jsonString, JsonNode.class);
        JsonNode person = json.get("person");
        if (person == null) {
            throw new IllegalArgumentException("json must contain a \"person\" field");
        }

        Patient patient = new Patient();
        patient.setGender(person.get("gender").getTextValue());
        patient.setBirthdate(parseDate(person.get("birthdate").getTextValue()));

        for (JsonNode id : json.get("identifiers")) {
            if (id.get("voided").getBooleanValue()) {
                continue;
            }
            String idTypeUuid = id.get("identifierType").get("uuid").getTextValue();
            PatientIdentifierType idType = identifierTypesByUuid.get(idTypeUuid);
            if (idType == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping unmapped identifier type: " + idTypeUuid);
                }
                continue;
            }

            String identifier = id.get("identifier").getTextValue();

            String idLocationUuid = id.get("location").get("uuid").getTextValue();
            Location location = locationsByUuid.get(idLocationUuid);
            if (idType == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping unmapped location: " + idLocationUuid);
                }
                continue;
            }

            patient.addIdentifier(new PatientIdentifier(identifier, idType, location));
        }

        for (JsonNode nameJson : person.get("names")) {
            if (nameJson.get("voided").getBooleanValue()) {
                continue;
            }
            PersonName name = new PersonName();
            copyStringProperty(name, nameJson, "prefix");
            copyStringProperty(name, nameJson, "givenName");
            copyStringProperty(name, nameJson, "middleName");
            copyStringProperty(name, nameJson, "familyNamePrefix");
            copyStringProperty(name, nameJson, "familyName");
            copyStringProperty(name, nameJson, "familyName2");
            copyStringProperty(name, nameJson, "familyNameSuffix");
            copyStringProperty(name, nameJson, "degree");
            patient.addName(name);
        }

        for (JsonNode addressJson : person.get("addresses")) {
            if (addressJson.get("voided").getBooleanValue()) {
                continue;
            }
            PersonAddress address = new PersonAddress();
            copyStringProperty(address, addressJson, "address1");
            copyStringProperty(address, addressJson, "address2");
            copyStringProperty(address, addressJson, "address3");
            copyStringProperty(address, addressJson, "address4");
            copyStringProperty(address, addressJson, "address5");
            copyStringProperty(address, addressJson, "address6");
            copyStringProperty(address, addressJson, "cityVillage");
            copyStringProperty(address, addressJson, "countyDistrict");
            copyStringProperty(address, addressJson, "stateProvince");
            copyStringProperty(address, addressJson, "country");
            copyStringProperty(address, addressJson, "postalCode");
            copyStringProperty(address, addressJson, "latitude");
            copyStringProperty(address, addressJson, "longitude");
            copyDateProperty(address, addressJson, "startDate");
            copyDateProperty(address, addressJson, "endDate");
            patient.addAddress(address);
        }

        for (JsonNode attributeJson : person.get("attributes")) {
            if (attributeJson.get("voided").getBooleanValue()) {
                continue;
            }
            String attrTypeUuid = attributeJson.get("attributeType").get("uuid").getTextValue();
            PersonAttributeType personAttributeType = attributeTypesByUuid.get(attrTypeUuid);
            if (personAttributeType == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Skipping unmapped attribute type: " + attrTypeUuid);
                }
                continue;
            }
            String value = attributeJson.get("value").getTextValue();
            if (StringUtils.isNotBlank(value)) {
                PersonAttribute personAttribute = new PersonAttribute(personAttributeType, value);
                patient.addAttribute(personAttribute);
            }
        }

        return patient;
    }

    private void copyDateProperty(Object ontoBean, JsonNode fromJson, String field) {
        String asText;
        try {
            asText = fromJson.get(field).getTextValue();
        } catch (Exception ex) {
            // skip fields not contained in the json
            return;
        }
        if (StringUtils.isBlank(asText)) {
            return;
        }
        Date date = parseDate(asText);
        try {
            PropertyUtils.setProperty(ontoBean, field, date);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private void copyStringProperty(Object ontoBean, JsonNode fromJson, String field) {
        String asText;
        try {
            asText = fromJson.get(field).getTextValue();
        } catch (Exception ex) {
            // skip fields not contained in the json
            return;
        }
        try {
            PropertyUtils.setProperty(ontoBean, field, asText);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private Date parseDate(String date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Badly formatted date: " + date);
        }
    }

}