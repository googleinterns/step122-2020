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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet responsible for deleting groceries. */
@WebServlet("/delete-grocery")
public class DeleteGroceryServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("deleteGroceryServlet");
    UserService userService = UserServiceFactory.getUserService();
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String userEmail = userService.getCurrentUser().getEmail();
    long id = Long.parseLong(request.getParameter("id"));
    Key groceryEntityKey = KeyFactory.createKey("Grocery", id);

    Entity groceryEntity;
    try {
        groceryEntity = datastore.get(groceryEntityKey);
    } catch (EntityNotFoundException e) {
        System.out.println("Family not found");
        return;
    }
    // deletes key if user email is assigned to them or no one
    String member = (String) groceryEntity.getProperty("assignEmail");
    if(member.equals(" ")) {
        datastore.delete(groceryEntityKey);
    } else if (member.equals(userEmail)) {
        datastore.delete(groceryEntityKey);
    } else {
        return;
    }
  }
}