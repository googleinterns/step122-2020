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

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.Calendar.Acl.Insert;
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
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity userInfoEntity = Utils.getCurrentUserEntity();

        // If current user is not in a family, they cannot add a member
        if (userInfoEntity == null) {
            System.out.println("You do not belong to a family yet!");
            return;
        }

        Entity familyEntity = null;
    
        try {
            familyEntity = Utils.getCurrentFamilyEntity(userInfoEntity);
        } catch(EntityNotFoundException e) {
            System.out.println("Family entity was not found");
            response.setContentType("application/text");
            response.getWriter().println("");
            return;
        }

        // Fetch the new member email to add from the request
        String newMemberEmail = request.getParameter("new-member-email");
        long updatedTimestamp = System.currentTimeMillis();

        // Add the new member email to the family's list and update datastore
        ArrayList<String> memberEmails = (ArrayList<String>) familyEntity.getProperty("memberEmails");
        memberEmails.add(newMemberEmail);

        familyEntity.setProperty("memberEmails", memberEmails);
        familyEntity.setProperty("timestamp", updatedTimestamp);

        datastore.put(familyEntity);

        // Adds the new user to the family calendar
        String calendarID = (String) familyEntity.getProperty("calendarID");

        if(calendarID != null) {
            Utils.createUserAclRequest(calendarID, newMemberEmail, "user", "owner").execute();
        }

        long familyID = familyEntity.getKey().getId();

        // Create a user info entity for the newly added member
        Entity newUserInfoEntity = new Entity("UserInfo", newMemberEmail);
        newUserInfoEntity.setProperty("email", newMemberEmail);
        newUserInfoEntity.setProperty("familyID", familyID);
        datastore.put(newUserInfoEntity);

        response.sendRedirect("/settings.html");
    }
}
