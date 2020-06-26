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
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for deleting members from a family. */
@WebServlet("/delete-member")
public class DeleteMemberServlet extends HttpServlet {

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
        System.out.println("You do not belong to a family currently");
        return;
    }

    String memberToDelete = request.getParameter("member-to-delete");

    long familyID = (long) userInfoEntity.getProperty("familyID");

    Key familyEntityKey = KeyFactory.createKey("Family", familyID);

    try {
        Entity familyEntity = datastore.get(familyEntityKey);
        ArrayList<String> memberEmails = (ArrayList<String>) familyEntity.getProperty("memberEmails");
        if(!memberEmails.contains(memberToDelete)) {
            System.out.println("This member does not belong to your family");
            return;
        }

        memberEmails.remove(memberToDelete);

        familyEntity.setProperty("memberEmails", memberEmails);
        datastore.put(familyEntity);

        query = new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, memberToDelete));
        results = datastore.prepare(query);
        userInfoEntity = results.asSingleEntity();
        datastore.delete(userInfoEntity.getKey());
        response.sendRedirect("/settings.html");
        
    } catch (Exception e) {
        System.out.println("Family not found");
    }
  }
}