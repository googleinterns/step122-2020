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
    document.getElementById("createFamilyForm").style.display = "block";
}

function closeFamilyForm() {
  document.getElementById("createFamilyForm").style.display = "none";
}

function userLogin() {
  fetch('/login').then(response => response.text())
  .then((message) => {
    document.getElementById('login-container').innerHTML = message;
  });
}

/*function loadGrocery() {
  fetch('/grocery-list').then(response => response.text())
  .then((message) => {
    document.getElementById('grocery-list-container');
  });
}*/


function loadGrocery() {
    // fetches json list of groceries
    fetch('/grocery-list').then(response => response.json()).then((groceries) => {
      const groceryListElement = document.getElementById('grocery-list-container');
      
      groceries.forEach((grocery) => {
        groceryListElement.appendChild(createGroceryElement(grocery));
    })
  });
}

function createGroceryElement(grocery){
      const taskElement = document.createElement('li');
  taskElement.className = 'task';

  const titleElement = document.createElement('span');
  titleElement.innerText = task.title;

    // Remove the task from the DOM.
    taskElement.remove();

  taskElement.appendChild(titleElement);
  return taskElement;
}