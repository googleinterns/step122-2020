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


/** Servlet responsible for creating new tasks. */
@WebServlet("/grocery-list")
public class GroceryServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request,response);  
        response.sendRedirect("/grocery.html");
    }
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        // adds item to datastore
        String grocery = request.getParameter("groceryItem");
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity groceryEntity = new Entity("grocery");
        if(grocery != null && !grocery.equals("")) {
            groceryEntity.setProperty("grocery", grocery);
            datastore.put(groceryEntity);
        }

        // creates arraylist and starts query
        ArrayList<String> groceryList = new ArrayList<String>();
        Query query = new Query("grocery");
        PreparedQuery results = datastore.prepare(query);
        
        //loads entities into arraylist to be printed
        for (Entity entity : results.asIterable()) {
            String groceryItem = (String) entity.getProperty("grocery");
            groceryList.add(groceryItem);
        }
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(groceryList));
        }
}
