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
import com.google.sps.data.Grocery;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;


/** Servlet takes grocery list and displays it to the screen based on the users family */
@WebServlet("/grocery-list")
public class GroceryServlet extends HttpServlet {
  private static final String GROCERY = "Grocery";
  private static final String FAMILY_ID = "familyID";
  private static final String COMPLETE = "Complete";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException { 
    Logger log = Logger.getLogger(GroceryServlet.class.getName());
    log.info("Your information log message.");    

    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail;
    try {
    userEmail = userService.getCurrentUser().getEmail();
    } catch (NullPointerException e) {
        response.setContentType("application/text");
        response.getWriter().println("You must Sign in before using this function");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }

    // if user isn't associated with family don't allow them to create a list
    Query query = new Query("UserInfo")
        .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userEmail));
    PreparedQuery results = datastore.prepare(query);
    Entity userInfoEntity = results.asSingleEntity();
    if (userInfoEntity == null) {
        response.setContentType("application/text");
        response.getWriter().println("You must belong to a family to use the grocery function");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
    }

    String assignGrocery = request.getParameter("assignGrocery");
    String noneAssigned = null;
    Entity groceryEntity = new Entity(GROCERY);
   
    // turn familyID into a key so the users family can be accessed
    long familyID = (long) userInfoEntity.getProperty(FAMILY_ID);
    Key familyEntityKey = KeyFactory.createKey("Family", familyID);
    Entity familyEntity;
    try {
        familyEntity = datastore.get(familyEntityKey);
    } catch (EntityNotFoundException e) {
        response.setContentType("application/text");
        response.getWriter().println("Family data was not found - please refresh and try again");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
    }

    // checks if the given email matches a email in the family
    if (assignGrocery == null ||assignGrocery.equals("")) {
        groceryEntity.setProperty("assignEmail", noneAssigned);
    } else {
        ArrayList<String> memberEmails = (ArrayList<String>) familyEntity.getProperty("memberEmails");
        for(int i = 0; i < memberEmails.size(); i++) {
            if(assignGrocery.equals(memberEmails.get(i))) {
                groceryEntity.setProperty("assignEmail" , memberEmails.get(i));
                break;
            } else if(i == memberEmails.size() - 1) {
                System.out.println("User not found");
                response.setContentType("application/text");
                response.getWriter().println("You can only assigned members in your family");
                response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                return;
            }
        }
    }

    // adds item to datastore
    boolean complete = false;
    String grocery = request.getParameter("groceryItem");
    if( grocery != null && !grocery.equals("")) {
        groceryEntity.setProperty(GROCERY, grocery);
        groceryEntity.setProperty(FAMILY_ID, familyID);
        groceryEntity.setProperty(COMPLETE, complete);
        datastore.put(groceryEntity);
    }     
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    String userEmail;
     try {
    userEmail = userService.getCurrentUser().getEmail();
    } catch (NullPointerException e) {
        response.setContentType("application/text");
        response.getWriter().println("You must Sign in before using this function");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }
    // checks if user is a part of a family and returns an error if they aren't
    Query query = new Query("UserInfo")
        .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userEmail));
    PreparedQuery results = datastore.prepare(query);
    Entity userInfoEntity = results.asSingleEntity();
    if (userInfoEntity == null) {
        response.setContentType("application/text");
        response.getWriter().println("You must belong to a family to use the grocery function");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return;
    }

    // creates arraylist and starts query
    ArrayList<Grocery> groceryList = new ArrayList<>();
    long familyID = (long) userInfoEntity.getProperty(FAMILY_ID);
    try {
    Query groceryQuery = new Query(GROCERY)
      .setFilter(new Query.FilterPredicate(FAMILY_ID, Query.FilterOperator.EQUAL, familyID));   
    PreparedQuery familyGrocery= datastore.prepare(groceryQuery);
    groceryList = fetchGroceries(familyGrocery, userEmail);   
    } catch(Exception e) {
        response.setContentType("application/text");
        response.getWriter().println("Something went wrong.");
        response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        return;
    }
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(groceryList));
    }

  // returns items from the query that match the users familyID in the Grocery object    
  private ArrayList<Grocery> fetchGroceries(PreparedQuery familyGrocery, String userEmail) {
    boolean isUserAssigned;
    Boolean complete;
    long timestamp = System.currentTimeMillis();
    ArrayList<Grocery> groceryList = new ArrayList<>();
    for (Entity entity : familyGrocery.asIterable()) {
        String groceryItem = (String) entity.getProperty(GROCERY);
        String assignEmail = (String) entity.getProperty("assignEmail");
        complete = (Boolean) entity.getProperty(COMPLETE);
        long id = entity.getKey().getId();     
        if (userEmail.equals(assignEmail)) {
            isUserAssigned = true;
        } else {
            isUserAssigned = false;
        }
        Grocery grocery = new Grocery(assignEmail, id, timestamp, groceryItem, isUserAssigned, complete);
        groceryList.add(grocery);
    }
    return groceryList;
  }
}
