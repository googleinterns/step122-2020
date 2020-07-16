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
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.googleapis.batch.*;
import com.google.api.client.googleapis.json.GoogleJsonErrorContainer;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.Calendar.Acl.Insert;
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
@WebServlet("/create-calendar")
public class CreateCalendarServlet extends AbstractAppEngineAuthorizationCodeServlet {

  private static final String CALENDAR_ID_PROPERTY = "calendarID";
  private static final String MEMBER_EMAILS_PROPERTY = "memberEmails";

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
          
    Calendar calendarService = Utils.loadCalendarClient();

    Entity currentFamilyEntity = null;
    
    try {
        currentFamilyEntity = Utils.getCurrentFamilyEntity(Utils.getCurrentUserEntity());
    } catch(EntityNotFoundException e) {
        System.out.println("Family entity was not found");
        response.setContentType("application/text");
        response.getWriter().println("");
        return;
    }

    // Create a new calendar
    com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
    calendar.setSummary((String) currentFamilyEntity.getProperty("name") + "'s Calendar");

    // Insert the new calendar
    com.google.api.services.calendar.model.Calendar createdCalendar = calendarService.calendars().insert(calendar).execute();

    currentFamilyEntity.setProperty(CALENDAR_ID_PROPERTY, createdCalendar.getId());
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(currentFamilyEntity);

    ArrayList<String> memberEmails = (ArrayList<String>) currentFamilyEntity.getProperty(MEMBER_EMAILS_PROPERTY);

    String currentUserEmail = UserServiceFactory.getUserService().getCurrentUser().getEmail();
    BatchRequest batch = calendarService.batch();
    
    for (String memberEmail : memberEmails) {
        // Create access rule with associated scope
        if (memberEmail.equals(currentUserEmail)) {
            continue;
        } 
        
        AclRule rule = new AclRule();
        Scope scope = new Scope();
        scope.setType("user").setValue(memberEmail);
        rule.setScope(scope).setRole("owner");

        // Insert new access rule
        Insert insertRequest = calendarService.acl().insert(createdCalendar.getId(), rule);

        batch.queue(insertRequest.buildHttpRequest(), Calendar.class, GoogleJsonErrorContainer.class, 
          new BatchCallback<Calendar, GoogleJsonErrorContainer>() {

            public void onSuccess(Calendar calendar, HttpHeaders responseHeaders) {
                log("Added ACL rule");
            }

            public void onFailure(GoogleJsonErrorContainer e, HttpHeaders responseHeaders) {
                log(e.getError().getMessage());
            }
        }); // Throws IOException
    }

    batch.execute(); // Throws IOException
   
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
