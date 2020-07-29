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
import com.google.api.services.calendar.model.Acl;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.AclRule.Scope;
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
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity userInfoEntity = Utils.getCurrentUserEntity();
    if (userInfoEntity == null) {
        ErrorHandlingUtils.setError(HttpServletResponse.SC_BAD_REQUEST,
            "You must belong to a family to use this function", response);
        return;
    }

    Entity familyEntity = null;
    
    try {
        familyEntity = Utils.getCurrentFamilyEntity(userInfoEntity);
    } catch(EntityNotFoundException e) {
        ErrorHandlingUtils.setError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Family data was not found - please refresh and try again", response);
        return;
    }

    String memberToDelete = request.getParameter("member-to-delete");

    // Remove the member from the list and update datastore
    ArrayList<String> memberEmails = (ArrayList<String>) familyEntity.getProperty("memberEmails");
    if(!memberEmails.contains(memberToDelete)) {
        ErrorHandlingUtils.setError(HttpServletResponse.SC_BAD_REQUEST,
            "This person does not belong to your family!", response);
        return;
    }

    GroceryUtils.removeMember(memberToDelete);

    // Revoke calendar access for the user
    String calendarID = (String) familyEntity.getProperty("calendarID");

    if(calendarID != null && !memberToDelete.equals(UserServiceFactory.getUserService().getCurrentUser().getEmail())) {
        Calendar calendarService = Utils.loadCalendarClient();
        // Iterate over a list of access rules
        Acl acl = calendarService.acl().list(calendarID).execute();

        for (AclRule rule : acl.getItems()) {
            if(rule.getScope().getValue().equals(memberToDelete)) {
                calendarService.acl().delete(calendarID, rule.getId()).execute();
            }
        }
    }

    memberEmails.remove(memberToDelete);

    familyEntity.setProperty("memberEmails", memberEmails);
    datastore.put(familyEntity);

    removeMemberFromCalendar(memberToDelete, familyEntity);
    removeUserInfo(memberToDelete, datastore, userInfoEntity);

    response.sendRedirect("/settings.html");
  }

  // Revoke calendar access for a user
  private void removeMemberFromCalendar(String memberToDelete, Entity familyEntity) throws IOException {
    String calendarID = (String) familyEntity.getProperty("calendarID");

    String currentUserEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    if (calendarID != null && !memberToDelete.equals(currentUserEmail)) {
        Calendar calendarService = Utils.loadCalendarClient();
        // Iterate over a list of access rules
        Acl acl = calendarService.acl().list(calendarID).execute();

        for (AclRule rule : acl.getItems()) {
            if(rule.getScope().getValue().equals(memberToDelete)) {
                calendarService.acl().delete(calendarID, rule.getId()).execute();
            }
        }
    }
  }
  
  // Delete the user info entity of the removed member from datastore
  private void removeUserInfo(String memberToDelete, DatastoreService datastore, Entity userInfoEntity) throws IOException {
    Query query = new Query("UserInfo")
        .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, memberToDelete));
    PreparedQuery results = datastore.prepare(query);
    userInfoEntity = results.asSingleEntity();
    datastore.delete(userInfoEntity.getKey());
  }
}
