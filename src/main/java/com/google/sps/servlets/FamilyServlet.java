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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for creating new families. */
@WebServlet("/family")
public class FamilyServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userEmail = userService.getCurrentUser().getEmail();
        Query query = new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userEmail));
        PreparedQuery results = datastore.prepare(query);
        Entity entity = results.asSingleEntity();
        if (entity != null) {
            System.out.println("You already belong to a family");
            return;
        }
        
        String familyName = request.getParameter("family-name");
        long createdTimestamp = System.currentTimeMillis();

        ArrayList<String> memberEmails = new ArrayList(Arrays.asList(userEmail)); 

        Entity familyEntity = new Entity("Family");
        familyEntity.setProperty("name", familyName);
        familyEntity.setProperty("memberEmails", memberEmails);
        familyEntity.setProperty("createdTimestamp", createdTimestamp);   
        datastore.put(familyEntity);

        long familyID = familyEntity.getKey().getId();

        Entity userInfoEntity = new Entity("UserInfo", userEmail);
        userInfoEntity.setProperty("email", userEmail);
        userInfoEntity.setProperty("familyID", familyID);
        datastore.put(userInfoEntity);

        response.sendRedirect("/settings.html");
    }
}