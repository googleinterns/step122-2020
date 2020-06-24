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

/** Servlet responsible for adding new members to a family. */
@WebServlet("/new-member")
public class NewMemberServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserService userService = UserServiceFactory.getUserService();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userEmail = userService.getCurrentUser().getEmail();
        Query query = new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userEmail));
        PreparedQuery results = datastore.prepare(query);
        Entity userInfoEntity = results.asSingleEntity();
        if (userInfoEntity == null) {
            System.out.println("You do not belong to a family yet!");
            return;
        }

        String newMemberEmail = request.getParameter("new-member-email");
        long updatedTimestamp = System.currentTimeMillis();

        long familyID = (long) userInfoEntity.getProperty("familyID");

        Key familyEntityKey = KeyFactory.createKey("Family", familyID);

        try {
            Entity familyEntity = datastore.get(familyEntityKey);
            ArrayList<String> memberEmails = (ArrayList<String>) familyEntity.getProperty("memberEmails");
            memberEmails.add(newMemberEmail);

            familyEntity.setProperty("memberEmails", memberEmails);
            familyEntity.setProperty("timestamp", updatedTimestamp);

            datastore.put(familyEntity);

            Entity newUserInfoEntity = new Entity("UserInfo", newMemberEmail);
            newUserInfoEntity.setProperty("email", newMemberEmail);
            newUserInfoEntity.setProperty("familyID", familyID);
            datastore.put(newUserInfoEntity);
        } catch (Exception e) {
            System.out.println("Family not found");
        }

        response.sendRedirect("/settings.html");
    }
}