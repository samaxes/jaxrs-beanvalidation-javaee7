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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("persons")
@Produces(MediaType.APPLICATION_JSON)
public class Persons {

    private static final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<String, Person>();

    @GET
    public Collection<Person> getAll() {
        return persons.values();
    }

    @GET
    @Path("{id}")
    public Person getPerson(
            @PathParam("id")
            @NotNull(message = "The id must not be null")
            @Pattern(regexp = "[0-9]+", message = "The id must be a valid number")
            String id) {
        return persons.get(id);
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createPerson(
            @FormParam("id")
            @NotNull(message = "{person.id.notnull}")
            @Pattern(regexp = "[0-9]+", message = "{person.id.pattern}")
            String id,
            @FormParam("name")
            @Size(min = 2, max = 50, message = "{person.name.size}")
            String name) {
        Person person = new Person();
        person.setId(Integer.valueOf(id));
        person.setName(name);
        persons.put(id, person);
        return Response.status(Response.Status.CREATED).entity(person).build();
    }
}
