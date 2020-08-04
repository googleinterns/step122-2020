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
import com.google.gson.Gson;
import com.google.sps.data.Photo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet takes photo album URL and displays it to the screen based on the users family */
@WebServlet("/new-photo")
public class NewPhotoServlet extends HttpServlet {
    private static final String PHOTO = "Photo";
    private static final String FAMILY_ID = "familyID";
    @Override

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {   
        UserService userService = UserServiceFactory.getUserService();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userEmail = userService.getCurrentUser().getEmail();

        // if user isn't associated with family don't allow them to submit a photo album url
        Query query = new Query("UserInfo")
            .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userEmail));
        PreparedQuery results = datastore.prepare(query);
        Entity userInfoEntity = results.asSingleEntity();
        if (userInfoEntity == null) {
            response.sendRedirect("/photos.html");
            return;
        }   

        String link = request.getParameter("albumLink");
        Entity photoEntity = new Entity(PHOTO);

        // turn familyID into a key so the user's family can be accessed
        long familyID = (long) userInfoEntity.getProperty(FAMILY_ID);
        Key familyEntityKey = KeyFactory.createKey("Family", familyID);
        Entity familyEntity;
        try {
            familyEntity = datastore.get(familyEntityKey);
        } catch (EntityNotFoundException e) {
            return;
        }

        // adds url to datastore
        if( link != null && !link.equals("")) {
            photoEntity.setProperty(PHOTO, link);
            datastore.put(photoEntity);
        }     

        response.sendRedirect("/photos.html");
    }
}