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
 
public class RemoveFromGrocery {
 
  public void removeMember(String removedMember) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    Query removedMemberQuery = new Query(GROCERY)
      .setFilter(new Query.FilterPredicate(“memberEmail”, Query.FilterOperator.EQUAL, removedMember)); 

    PreparedQuery results = datastore.prepare(removedMemberQuery);

    for (Entity memberEntity : results.asIterable()) {
        datastore.delete(memberEntity.getKey());
    }
}
 
}

