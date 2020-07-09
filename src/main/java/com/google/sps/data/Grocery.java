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

package com.google.sps.data;



/** A family in the database */
public final class Grocery {

  private final String item;
  private final long id;
 // private final long timestamp;
  private final String email;
  private final boolean userMatch;

  public Grocery(String email, long id, String item, boolean userMatch) {
    this.email = email;
    this.id = id;
    this.item = item;
    this.userMatch = userMatch;
  }

  public String getEmail(String email) {
      return email;
  }


  public boolean getUserMatch(boolean userMatch) {
      return userMatch;
  }


  public long setId(long id) {
      return id;
  }

    public String setItem(String item) {
      return item;
  }

  public String toString() {
    return item + " assigned to: " + email;
  }
}