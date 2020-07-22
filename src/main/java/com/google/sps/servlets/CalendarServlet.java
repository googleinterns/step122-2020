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
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
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
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** 
 * Servlet responsible for returning the link to the shared family calendar
*/
public class CalendarServlet extends HttpServlet {
  
  private static final String CALENDAR_ID_PROPERTY = "calendarID";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    UserService userService = UserServiceFactory.getUserService();
    if(!userService.isUserLoggedIn()) {
        ErrorHandlingUtils.setError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to use the calendar function", response);
        return;
    }
    
    Entity userInfoEntity = Utils.getCurrentUserEntity();

    // If current user is not in a family, they cannot add a member
    if (userInfoEntity == null) {
        ErrorHandlingUtils.setError(HttpServletResponse.SC_BAD_REQUEST, "You must belong to a family to use the calendar function", response);
        return;
    }

    Entity familyEntity = null;
    try {
        familyEntity = Utils.getCurrentFamilyEntity(userInfoEntity);
    } catch(EntityNotFoundException e) {
        ErrorHandlingUtils.setError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Family data was not found - please refresh and try again", response);
        return;
    }
    
    String calendarID = (String) familyEntity.getProperty(CALENDAR_ID_PROPERTY);

    if (calendarID == null) {
        response.setContentType("application/text");
        response.getWriter().println("");
        return;
    }

    Calendar calendarService = Utils.loadCalendarClient();

    CalendarListEntry calendarEntry;
    try {
        calendarEntry = calendarService.calendarList().get(calendarID).execute();
    } catch (GoogleJsonResponseException e) {
        // Create a new calendar list entry
        CalendarListEntry calendarListEntry = new CalendarListEntry();
        calendarListEntry.setId(calendarID);

        // Insert the new calendar list entry
        calendarEntry = calendarService.calendarList().insert(calendarListEntry).execute();
    }
    
    response.setContentType("application/text");
    response.getWriter().println("https://calendar.google.com/calendar/embed?src=" + calendarEntry.getId() + "&output=embed");
   
  }
}
