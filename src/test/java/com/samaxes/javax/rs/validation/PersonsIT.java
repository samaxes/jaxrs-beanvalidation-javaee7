/*
 * Integrating Bean Validation with JAX-RS in Java EE 7
 * https://github.com/samaxes/jaxrs-beanvalidation-javaee7
 *
 * Copyright (c) 2013 samaxes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samaxes.javax.rs.validation;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonGenerator;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class PersonsIT {

    private static final Logger LOGGER = Logger.getLogger(PersonsIT.class.getName());

    @Deployment(testable = false)
    public static WebArchive createDeployment() throws IOException {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, "jaxrs-beanvalidation-javaee7.war")
                .addPackage("com.samaxes.javax.rs.validation")
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

        LOGGER.info(war.toString(Formatters.VERBOSE));

        return war;
    }

    @Test
    @InSequence(10)
    public void shouldReturnAllPersons(@ArquillianResource URL baseURL) {
        // Client client = ClientBuilder.newClient();
        Client client = ClientBuilder.newBuilder()
                .register(JsonProcessingFeature.class)
                .property(JsonGenerator.PRETTY_PRINTING, true)
                .build();
        Response response = client.target(baseURL + "r/persons")
                .request(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt")
                .get();
        response.bufferEntity();

        logResponse("shouldReturnAllPersons", response, JsonArray.class);
        Assert.assertEquals(Collections.emptyList(), response.readEntity(new GenericType<Collection<Person>>() {}));
    }

    @Test
    @InSequence(20)
    public void shouldReturnAValidationErrorWhenGettingAPerson(@ArquillianResource URL baseURL) {
        Client client = ClientBuilder.newBuilder()
                .register(JsonProcessingFeature.class)
                .property(JsonGenerator.PRETTY_PRINTING, true)
                .build();
        Response response = client.target(baseURL + "r/persons/{id}")
                .resolveTemplate("id", "test")
                .request(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt")
                .get();
        response.bufferEntity();

        logResponse("shouldReturnAValidationErrorWhenGettingAPerson", response, JsonObject.class);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    @InSequence(30)
    public void shouldReturnAnEmptyPerson(@ArquillianResource URL baseURL) {
        Client client = ClientBuilder.newBuilder()
                .register(JsonProcessingFeature.class)
                .property(JsonGenerator.PRETTY_PRINTING, true)
                .build();
        Response response = client.target(baseURL + "r/persons/{id}")
                .resolveTemplate("id", "10")
                .request(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt")
                .get();
        response.bufferEntity();

        logResponse("shouldReturnAnEmptyPerson", response, JsonObject.class);
        Assert.assertEquals(null, response.readEntity(Person.class));
    }

    @Test
    @InSequence(40)
    public void shouldReturnAValidationErrorWhenCreatingAPerson(@ArquillianResource URL baseURL) {
        Form form = new Form();

        Client client = ClientBuilder.newBuilder()
                .register(JsonProcessingFeature.class)
                .property(JsonGenerator.PRETTY_PRINTING, true)
                .build();
        Response response = client.target(baseURL + "r/persons/create")
                .request(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt")
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
        response.bufferEntity();

        logResponse("shouldReturnAValidationErrorWhenCreatingAPerson", response, JsonObject.class);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    @InSequence(50)
    public void shouldReturnACreatedPerson(@ArquillianResource URL baseURL) {
        Person person = new Person();
        person.setId(20);
        person.setName("sam");
        Form form = new Form();
        form.param("id", String.valueOf(person.getId()));
        form.param("name", person.getName());

        Client client = ClientBuilder.newBuilder()
                .register(JsonProcessingFeature.class)
                .property(JsonGenerator.PRETTY_PRINTING, true)
                .build();
        Response response = client.target(baseURL + "r/persons/create")
                .request(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt")
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));
        response.bufferEntity();

        logResponse("shouldReturnACreatedPerson", response, JsonObject.class);
        Assert.assertEquals(person, response.readEntity(Person.class));
    }

    private void logResponse(String method, Response response, Class<? extends JsonValue> type) {
        StringBuilder builder = new StringBuilder("\n\n" + method + "\n");
        builder.append("Response: " + response + "\n");
        if (MediaType.APPLICATION_JSON_TYPE.equals(response.getMediaType())) {
            builder.append("Entity: " + response.readEntity(type) + "\n");
        }
        LOGGER.info(builder.toString());
    }
}
