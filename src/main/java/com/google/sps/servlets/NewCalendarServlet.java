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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.AclRule;
import com.google.api.services.calendar.model.AclRule.Scope;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** 
 * Servlet responsible for creating a shared family calendar
*/
@WebServlet("/new-calendar")
public class NewCalendarServlet extends AbstractAppEngineAuthorizationCodeServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
          
    Calendar calendarService = Utils.loadCalendarClient();

    // Insert the new calendar
    com.google.api.services.calendar.model.Calendar createdCalendar = calendarService.calendars().insert(calendar).execute();

    Entity currentFamilyEntity = Utils.getCurrentFamilyEntity(Utils.getCurrentUserEntity());

    // Create a new calendar
    com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
    calendar.setSummary((String) currentFamilyEntity.getProperty("name") + "'s Calendar");

    currentFamilyEntity.setProperty("calendarID", createdCalendar.getId());
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(currentFamilyEntity);

    ArrayList<String> memberEmails = (ArrayList<String>) currentFamilyEntity.getProperty("memberEmails");

    UserService userService = UserServiceFactory.getUserService();
    for (String memberEmail : memberEmails) {
        // Create access rule with associated scope
        if (memberEmail.equals(userService.getCurrentUser().getEmail())) {
            continue;
        } 
        
        AclRule rule = new AclRule();
        Scope scope = new Scope();
        scope.setType("user").setValue(memberEmail);
        rule.setScope(scope).setRole("owner");

        // Insert new access rule
        AclRule createdRule = calendarService.acl().insert(createdCalendar.getId(), rule).execute();
        System.out.println(createdRule.getId());
    }
   
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    return Utils.getRedirectUri(req);
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return Utils.newFlow();
  }
}
