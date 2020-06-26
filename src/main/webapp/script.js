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

function openFamilyForm() {
    document.getElementById("createFamilyForm").style.visibility = "visible";
}

function closeFamilyForm() {
  document.getElementById("createFamilyForm").style.visibility = "hidden";
}

function openNewMemberForm() {
    document.getElementById("newMemberForm").style.visibility = "visible";
}

function closeNewMemberForm() {
  document.getElementById("newMemberForm").style.visibility = "hidden";
}

function openRemoveMemberForm() {
    document.getElementById("removeMemberForm").style.visibility = "visible";
}

function closeRemoveMemberForm() {
  document.getElementById("removeMemberForm").style.visibility = "hidden";
}

function loadFamilyMembers() {
  fetch('/family').then(response => response.json()).then((memberEmails) => {
    const familyElement = document.getElementById('family-container');
    const familyHeader = document.createElement("HEADER");
    familyHeader.innerText = "Current Family Members: ";
    familyElement.appendChild(familyHeader);
    memberEmails.forEach((memberEmail) => {
      const memberListElement = document.createElement('li');
      memberListElement.innerText = memberEmail;
      familyElement.appendChild(memberListElement);
    })
  });
}

function userLogin() {
  fetch('/login').then(response => response.text())
  .then((message) => {
    document.getElementById('login-container').innerHTML = message;
  });
  
}
