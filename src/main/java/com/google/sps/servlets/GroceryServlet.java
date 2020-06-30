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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet takes grocery list and displays it to the screen based on the users family*/
@WebServlet("/grocery-list")
public class GroceryServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {      
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = userService.getCurrentUser().getEmail();

    // if user isn't associated with family don't allow them to create a list
    Query query = new Query("UserInfo")
        .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userEmail));
    PreparedQuery results = datastore.prepare(query);
    Entity userInfoEntity = results.asSingleEntity();
    if (userInfoEntity == null) {
        response.sendRedirect("/grocery.html");
        return;
    }
    String assignGrocery = request.getParameter("assignGrocery");
    String noneAssigned = " ";
    Entity groceryEntity = new Entity("Grocery");
   
    // turn familyID into a key so the users family can be accessed
    long familyID = (long) userInfoEntity.getProperty("familyID");
    Key familyEntityKey = KeyFactory.createKey("Family", familyID);
    Entity familyEntity;
    try {
        familyEntity = datastore.get(familyEntityKey);
    } catch (EntityNotFoundException e) {
        System.out.println("Family not found");
        return;
    }

    // checks if the given email matches a email in the family
    ArrayList<String> memberEmails = (ArrayList<String>) familyEntity.getProperty("memberEmails");
    for(int i = 0; i < memberEmails.size(); i++) {
        System.out.println("Cuerrnt email is : " + memberEmails.get(i));
        if(assignGrocery.equals(memberEmails.get(i))) {
            groceryEntity.setProperty("assignEmail" , memberEmails.get(i));
            System.out.println("emails match");
            break;
        } else {
            groceryEntity.setProperty("assignEmail", noneAssigned);
        }
    }
    
    // adds item to datastore
    String grocery = request.getParameter("groceryItem");
    if( grocery != null && !grocery.equals("")) {
        groceryEntity.setProperty("grocery", grocery);
        groceryEntity.setProperty("familyID", familyID);
        datastore.put(groceryEntity);
    }
               
    doGet(request,response);  
    response.sendRedirect("/grocery.html");
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();

    // checks if user is a part of a family and returns an error if they aren't
    String userEmail = userService.getCurrentUser().getEmail();
    Query query = new Query("UserInfo")
        .setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, userEmail));
    PreparedQuery results = datastore.prepare(query);
    Entity userInfoEntity = results.asSingleEntity();
    if (userInfoEntity == null) {
        response.setContentType("text/html;");
        response.getWriter().println("You do not belong to a family yet!");
    return;
    }

    // creates arraylist and starts query
    ArrayList<String> groceryList = new ArrayList<String>();
    Query groceryQuery = new Query("Grocery");
    PreparedQuery grocery = datastore.prepare(groceryQuery);
    long familyID = (long) userInfoEntity.getProperty("familyID");
    groceryList = checkGroceries(familyID, grocery);   

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(groceryList));
    }

    // returns items from the query that match the users familyID
    private ArrayList<String> checkGroceries(long familyID, PreparedQuery grocery) {
    ArrayList<String> groceryList = new ArrayList<String>();
    for (Entity entity : grocery.asIterable()) {
        String groceryOutput;
        long groceryID = (long) entity.getProperty("familyID");
        if(groceryID == familyID) {
            String groceryItem = (String) entity.getProperty("grocery");
            String memberEmail = (String) entity.getProperty("assignEmail");
            groceryOutput = groceryItem + " assigned to: " + memberEmail;
            groceryList.add(groceryOutput);
        }
    }
    return groceryList;
    }
}
