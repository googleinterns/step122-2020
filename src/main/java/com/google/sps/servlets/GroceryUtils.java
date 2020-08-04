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
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/**
* This helper class removes member that were previously assigned to a grocery item after 
*  being deleted from a family
*/
class GroceryUtils {
  static void removeMember(String removedMember) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    // create query of grocery items assigned to removed member
    Query removedMemberQuery = new Query("Grocery")
      .setFilter(new Query.FilterPredicate("assignEmail", Query.FilterOperator.EQUAL, removedMember)); 
    PreparedQuery results = datastore.prepare(removedMemberQuery);
    
    for (Entity groceryEntity : results.asIterable()) {
        groceryEntity.setProperty("assignEmail", null);
        datastore.put(groceryEntity);
    }
  }
}

