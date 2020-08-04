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
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for deleting a family and all associated data */
@WebServlet("/delete-family")
public class DeleteFamilyServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

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

    String calendarID = (String) currentFamilyEntity.getProperty(CALENDAR_ID_PROPERTY);
    long familyID = (long) userInfoEntity.getProperty("familyID");

    // Delete family calendar
    if (calendarID != null) {
        Calendar calendarService = Utils.loadCalendarClient();
        calendarService.calendars().delete(calendarID).execute();
    }

    removeDataTypeForFamily("UserInfo", familyID);
    removeDataTypeForFamily("Grocery", familyID);
    removeDataTypeForFamily("Photo", familyID);

    datastore.delete(familyEntity.getKey());

  }

  // Delete the all data of one type corresponding to a family
  private void removeDataTypeForFamily(String kind, long familyID) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query(kind)
        .setFilter(new Query.FilterPredicate("familyID", Query.FilterOperator.EQUAL, familyID));
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable()) {
        datastore.delete(entity.getKey());
    }
  }
}