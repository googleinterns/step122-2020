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
        String email = request.getParameter("memberEmail");
        long familyID = Long.parseLong(request.getParameter("familyID"));
        long timestamp = System.currentTimeMillis();

        Key familyEntityKey = KeyFactory.createKey("Family", familyID);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        try {
            Entity familyEntity = datastore.get(familyEntityKey);
            ArrayList<String> memberEmails = (ArrayList<String>) familyEntity.getProperty("memberEmails");
            memberEmails.add(email);

            familyEntity.setProperty("memberEmails", memberEmails);
        } catch (Exception e) {
            System.out.println("Error");
        }

        response.sendRedirect("/settings.html");
    }
}