// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Servlet responsible for creating families and returning their info. 
 * GET returns current user's family information, POST creates a family.
*/
@WebServlet("/family")
public class FamilyServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Query datastore with the current users email to see if they are in a family
        String userEmail = userService.getCurrentUser().getEmail();
        Query query = new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userEmail));
        PreparedQuery results = datastore.prepare(query);
        Entity userInfoEntity = results.asSingleEntity();

        // If there is no user info entity they are not in a family
        if (userInfoEntity == null) {
            return;
        }

        // Retrieve the family id from the user info and fetch their family entity from datastore
        long familyID = (long) userInfoEntity.getProperty("familyID");

        Key familyEntityKey = KeyFactory.createKey("Family", familyID);
        
        Entity familyEntity;

        try {
            familyEntity = datastore.get(familyEntityKey);
        } catch (EntityNotFoundException e) {
            System.out.println("Family not found");
            return;
        }

        // Fetch member emails belonging to the family and return in json format
        ArrayList<String> memberEmails = (ArrayList<String>) familyEntity.getProperty("memberEmails");

        Gson gson = new Gson();

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(memberEmails));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // Query datastore with the current users email to see if they are in a family
        String userEmail = userService.getCurrentUser().getEmail();
        Query query = new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userEmail));
        PreparedQuery results = datastore.prepare(query);
        Entity entity = results.asSingleEntity();

        // If the user already belongs to a family, they cannot create a new one
        if (entity != null) {
            System.out.println("You already belong to a family");
            return;
        }
        
        // Retrieve the family name from the request
        String familyName = request.getParameter("family-name");
        long createdTimestamp = System.currentTimeMillis();

        ArrayList<String> memberEmails = new ArrayList(Arrays.asList(userEmail)); 

        // Set the creator as the only member of the family and add family to datastore
        Entity familyEntity = new Entity("Family");
        familyEntity.setProperty("name", familyName);
        familyEntity.setProperty("memberEmails", memberEmails);
        familyEntity.setProperty("createdTimestamp", createdTimestamp);   
        datastore.put(familyEntity);

        long familyID = familyEntity.getKey().getId();

        // Create a user entity and add family ID to track the family the user is in
        Entity userInfoEntity = new Entity("UserInfo", userEmail);
        userInfoEntity.setProperty("email", userEmail);
        userInfoEntity.setProperty("familyID", familyID);
        datastore.put(userInfoEntity);

        response.sendRedirect("/settings.html");
    }
}