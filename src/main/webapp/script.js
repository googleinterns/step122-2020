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
    console.log("Close family");
  document.getElementById("createFamilyForm").style.visibility = "hidden";
}

function openNewMemberForm() {
    console.log("new member");
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
  fetch('/family').then(response => response.json()).then((family) => {
    const familyElement = document.getElementById('family-container');
    const familyHeader = document.createElement("HEADER");
    familyHeader.innerText = "Current Family Members in " + family.name + ":";
    familyElement.appendChild(familyHeader);
    family.members.forEach((memberEmail) => {
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

function loadGrocery() {
    // fetches json list of groceries
    fetch('/grocery-list').then(response => handleErrors(response)).then((response) => 
        response.json()).then((groceries) => {
            
    const groceryListElement = document.getElementById('grocery-list-container');  
    groceries.forEach((grocery) => {
        groceryListElement.appendChild(createGroceryElement(grocery));
    })
    }).catch(error => alert(error.message)); 

}

function createGroceryElement(grocery){
  const groceryElement = document.createElement('li');
  groceryElement.className = 'task';

  // If assigned email is empty then only show the item else show the item and the assigned email
  const titleElement = document.createElement('span');
  if(!grocery.email) {
    titleElement.innerText = grocery.item;
  } else {
    titleElement.innerText = grocery.item + " assigned to: " + grocery.email;
  }

  if(grocery.complete === true) {
    groceryElement.className = 'taskComplete';
  }

  // only creates button for items assigned to user or no one
  if (isEditableGrocery(grocery)) {
    const deleteButtonElement = document.createElement('button');
    deleteButtonElement.innerText = 'Delete';
    deleteButtonElement.addEventListener('click', () => {
      deleteGrocery(grocery);
      // Remove the task from the DOM.
      groceryElement.remove();
    });

    const completeButtonElement = document.createElement('button');
    completeButtonElement.innerText = 'Complete';
    completeButtonElement.addEventListener('click', () => {
      groceryElement.className = 'taskComplete';
      completeButtonElement.remove();
      completeGrocery(grocery);
    }); 

    if(isGroceryComplete(grocery)) {
      completeButtonElement.remove();
      groceryElement.appendChild(titleElement);
      groceryElement.appendChild(deleteButtonElement);
      return groceryElement;
    } 
    groceryElement.appendChild(titleElement);
    groceryElement.appendChild(deleteButtonElement);
    groceryElement.appendChild(completeButtonElement);
    return groceryElement;
    }
  groceryElement.appendChild(titleElement);
  return groceryElement;
    }

/** Fetches tasks from the server and adds them to the DOM. */
function loadTasks() {
  fetch('/list-tasks').then(response => response.json()).then((tasks) => {
    const taskListElement = document.getElementById('task-list');
    tasks.forEach((task) => {
      taskListElement.appendChild(createTaskElement(task));
    })
  });
}
 
/** Creates an element that represents a task, including its delete button. */
function createTaskElement(task) {
  const currentUser = getCurrentUser();

  const taskElement = document.createElement('li');
  taskElement.className = 'task';
 
  const titleElement = document.createElement('span');
  titleElement.innerText = task.title;
      
  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteTask(task);
 
    // Remove the task from the DOM.
    taskElement.remove();
    
  });
  taskElement.appendChild(titleElement);
  taskElement.appendChild(deleteButtonElement);
  return taskElement;
}
 
/** Tells the server to delete the task. */
function deleteTask(task) {
  const params = new URLSearchParams();
  params.append('id', task.id);
  fetch('/delete-task', {method: 'POST', body: params});
}

/** Tells the server to delete the grocery. */
function deleteGrocery(grocery) {
  const params = new URLSearchParams();
  params.append('id', grocery.id);  
  fetch('/delete-grocery', {method: 'POST', body: params});
}


/** Tells the server to delete the grocery. */
function completeGrocery(grocery) {
  const params = new URLSearchParams();
  params.append('id', grocery.id);  
  fetch('/complete-grocery', {method: 'POST', body: params});
}

/** Fetches tasks from the server and adds them to the DOM. */
function loadTasks() {
  fetch('/list-tasks').then(response => response.json()).then((tasks) => {
    const taskListElement = document.getElementById('task-list');
    tasks.forEach((task) => {
      taskListElement.appendChild(createTaskElement(task));
    })
  });
}


function isGroceryComplete(grocery) {
  return grocery.complete;
}

function isEditableGrocery(grocery) {
  return grocery.userMatch || !grocery.email; 
}

function insertCalendar() {
    const calElement = document.getElementById('caldiv');

    fetch('/calendar').then((response) => handleErrors(response)).then((response) => response.text()).then((calSrc) => {
        if(!calSrc.trim()) {
            return;
        }
        var calFrame = document.createElement('iframe');
        calFrame.setAttribute('src', calSrc);
        calFrame.setAttribute('style', 'border: 0'); 
        calFrame.setAttribute('width', '800'); 
        calFrame.setAttribute('height', '600'); 
        calFrame.setAttribute('frameborder', '0'); 
        calFrame.setAttribute('scrolling', 'no');
        calElement.appendChild(calFrame);
    });
}

function createCalendar() {
    fetch(new Request('/create-calendar', {method: 'POST'})).then(() => {
        insertCalendar();
    });
}

function createGrocery() {
   // const groceryForm = document.getElementById('groceryForm');
   // const groceryData = new FormData(groceryForm);
    //const params = URLSearchParams();
    fetch(new Request('/grocery-list', {method: 'POST', body: groceryData})).then(() => {
        loadGrocery();
    });
}

/** Tells the server to delete the grocery. */
function deleteGrocery(grocery) {
  const params = new URLSearchParams();
  params.append('id', grocery.id);  
  fetch('/delete-grocery', {method: 'POST', body: params});
}

function handleErrors(response) {
    if (!response.ok) {
        return response.clone().text().then((errorMsg) => {
            console.log(errorMsg);
            throw new Error(errorMsg);
        });
    }
    return response;
}
